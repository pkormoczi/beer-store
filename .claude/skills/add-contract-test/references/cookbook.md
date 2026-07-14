# Cookbook — templates and failure modes for `add-contract-test`

Copy-paste starting points for `SKILL.md`'s procedure. Model everything on the real
`customer`/`catalog` contracts (`beer-store-contract/src/test/resources/contracts/`),
not on these templates in isolation — they're distilled from those files but the
files are the source of truth if the two ever disagree.

## §6 target matrix (docs/TESTING_STRATEGY.md) — have vs. should-have

| Module | Should have (§6) | Have today |
|---|---|---|
| `customer` | search-by-name, get-by-id, create | all 3 — `contracts/customer/customer.yml` |
| `product` | `GET /catalog` one filter+sort combo, `GET /catalog/{id}`, one 404 | only the filter+sort browse case — `contracts/catalog/catalog.yml` |
| `order` | `POST /orders`, `GET /orders/{id}`, one 404 | none |

Re-check this against the actual folders/files before trusting it — it will drift as
contracts get added.

## Contract YAML templates

All contracts in this repo are **YAML** (not Groovy), one `---`-separated document per
scenario, in `<resource>.yml` alongside a same-named `<resource>.json` fixture.

### List/browse GET (filter + sort) — modelled on `catalog.yml`

```yaml
---
name: shouldReturnMatching<Resources>When<Scenario>
description: |
  Represents a successful scenario of browsing/filtering <resource>
  ```
  given:
      <preconditions>
  when:
      <the request>
  then:
      <the expected result>
  ```
request:
  method: GET
  url: /<resource>
  queryParameters:
    "<param>": "<value>"
response:
  status: 200
  headers:
    Content-Type: "application/json"
  bodyFromFile: <resource>.json
  matchers:
    body:
      - path: $[0].id
        type: by_regex
        regexType: as_integer
        predefined: number
```

### Get-by-id (200) — modelled on `customer.yml`'s `shouldReturnCustomerWhenSearchById`

```yaml
---
name: shouldReturn<Resource>WhenGetById
description: |
  ```
  given:
      an existing <resource> with a known id
  when:
      get <resource> by id
  then:
      return the <resource> data
  ```
request:
  method: GET
  url: /<resource>/1
response:
  status: 200
  headers:
    Content-Type: "application/json"
  bodyFromFile: <resource>.json
  matchers:
    body:
      - path: $.id
        type: by_regex
        regexType: as_integer
        predefined: number
```

Note the URL is a **literal path** (`/customers/1`, not a `urlPath` regex) — this repo
disambiguates overlapping routes by distinct literal URLs/query params, not by
`priority:`. Follow that pattern unless two contracts would genuinely collide.

### Not-found (404) — new pattern, no existing example yet in this repo

```yaml
---
name: shouldReturn404When<Resource>NotFound
description: |
  ```
  given:
      no <resource> exists with the given id
  when:
      get <resource> by an id that was never seeded
  then:
      return 404 Not Found
  ```
request:
  method: GET
  url: /<resource>/999
response:
  status: 404
  headers:
    Content-Type: "application/json"
```

Use an id that the seeding step in `ContractTestBase` deliberately never inserts
(e.g. `999`) — after `TRUNCATE ... RESTART IDENTITY`, seeded rows start at `1`, so any
id above the seeded count is safely "not found" as long as the seed data doesn't grow
past it later. If the response body shape matters (RFC 9457 Problem Details via
`platform.rest.CommonRestExceptionHandler` / the module's own
`*RestExceptionHandler`), add a `bodyFromFile` + matchers for the fields that are
stable (never match a full stack-trace/timestamp field).

### Create (POST, returns id) — modelled on `customer.yml`'s `shouldReturnIdWhenCreateNewCustomer`

```yaml
---
name: shouldReturnIdWhenCreate<Resource>
description: |
  ```
  given:
      valid <resource> data
  when:
      create a new <resource>
  then:
      return the created <resource> with an assigned id
  ```
request:
  method: POST
  url: /<resource>
  headers:
    "Content-Type": "application/json"
  bodyFromFile: <resource>.json
  matchers:
    body:
      - path: $.id
        type: by_regex
        value: '.*'
response:
  status: 201
  headers:
    Content-Type: "application/json"
  bodyFromFile: <resource>.json
  matchers:
    body:
      - path: $.id
        type: by_regex
        regexType: as_integer
        predefined: number
```

The request-side matcher loosens `$.id` to `by_regex value: '.*'` so the stub accepts
a request body that carries (or omits) an id; the response-side matcher pins it as a
real assigned integer.

## Fixture JSON shapes

- **Single object** (customer-style — one entity per contract):
  ```json
  {
    "id": 1,
    "...": "..."
  }
  ```
- **JSON array** (catalog-style — a list endpoint):
  ```json
  [
    { "id": 1, "...": "..." },
    { "id": 2, "...": "..." }
  ]
  ```
- Beer/catalog fixtures **must** include `"availability"` (e.g. `"IN_STOCK"`) even
  though the field is `readOnly` in the OpenAPI schema — the fixture represents the
  server's response, and `availability` is part of that response shape.
- Matcher recipe for any server-assigned id: `by_regex` / `regexType: as_integer` /
  `predefined: number`, on `$.id` for an object or `$[n].id` per array element — never
  pin the literal id value from the fixture in a `matchers` block (the fixture value
  is just what gets echoed back by `bodyFromFile`, not a pinned assertion).

## `ContractDataReader` recipe

`beer-store-application/src/test/java/dev/ronin/demo/beerstore/contract/ContractDataReader.java`
already has the generic primitives — add one small resource-specific method:

```java
// Single-object fixture, reusing a production mapper (preferred when one exists):
public OrderView readOrderData() {
    return orderMapper.toView(read("order/order", OrderDto.class));
}

// JSON-array fixture with no production dto->view mapper (read-only resource):
public List<BeerView> readCatalogData() {
    return readList("catalog/catalog", BeerDto.class).stream().map(this::toView).toList();
}
```

Notes:
- The classpath glob (`classpath*:/**/contracts/**/%s.json`) already matches any new
  `<resource>/<resource>.json` — no change needed there.
- Uses Jackson 3 (`tools.jackson.databind.ObjectMapper`), not `com.fasterxml.jackson`.
- `ContractDataReaderTest.java` in the same package is a good template to copy when
  adding a test for the new `read<Resource>Data()` method — it documents expected
  fixture values and the "missing fixture fails loudly" behavior.

## `ContractTestBase` seed snippet

`beer-store-application/src/test/java/dev/ronin/demo/beerstore/contract/ContractTestBase.java`,
inside the existing `@BeforeAll setup()`:

```java
// Respect FK order: order needs customer + beer seeded first.
// beer_order_line has an FK back to beer_order, so CASCADE covers it in one statement.
jdbcTemplate.execute("TRUNCATE TABLE customer RESTART IDENTITY CASCADE");
jdbcTemplate.execute("TRUNCATE TABLE beer RESTART IDENTITY CASCADE");
jdbcTemplate.execute("TRUNCATE TABLE beer_order RESTART IDENTITY CASCADE");

customerManagement.registerCustomer(...);          // existing
for (BeerView beer : reader.readCatalogData()) {    // existing
    beerManagement.createBeer(...);
}
orderManagement.placeOrder(new PlaceOrder(...));    // new — through the production port
```

Why through the application-service port and not raw SQL: it guarantees the seeded
row is produced by the exact same mapping/validation logic the real endpoint uses, so
the seeded data matches the contract's fixture body precisely. Why not
`@Transactional`: the base's `@BeforeAll` runs outside any test transaction on
purpose — the server under test (`RANDOM_PORT`) serves the HTTP request in its own
transaction and needs the seed data already committed.

## Consumer test skeleton

`beer-store-client/src/test/java/dev/ronin/demo/client/` — mirror
`CustomerControllerTest.java`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(
    stubsMode = StubRunnerProperties.StubsMode.CLASSPATH,
    ids = "dev.ronin.demo:beer-store-contract:+:stubs",
    mappingsOutputFolder = "target/stubs")
class OrderControllerTest {

    @StubRunnerPort("beer-store-contract")
    int port;

    private WebTestClient testClient;

    @BeforeEach
    void setUp() {
        testClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port).build();
    }

    @Test
    void testGetOrderById() {
        OrderDto body = testClient.get().uri("/orders/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange().expectStatus().isOk()
            .expectBody(OrderDto.class).returnResult().getResponseBody();
        assertThat(body.getId()).isEqualTo(1L);
    }
}
```

This repo's existing tests call the URI directly through `WebTestClient` and only
reuse the generated `*Dto` for (de)serialization — they do **not** drive the
generated `<Resource>Api` WebClient interface. That's a valid *optional* extension
(point an `ApiClient`/`OrderApi` at `http://localhost:{port}` and call
`orderApi.getOrderById(1L)` reactively) but not required to match the existing style.

## MockMvc-widening recipe (rare — only if deliberately extending the showcase)

1. In `ContractTestBaseMockMvc`, add `@MockitoSpyBean` for the new controller and pass
   it into `RestAssuredMockMvc.standaloneSetup(customerController, newController)`.
2. Seed the new resource's table in the same `@BeforeAll setup()`.
3. Widen `beer-store-application/pom.xml`'s `generate-tests-mockmvc` execution:
   ```xml
   <includedFiles>
     <includedFile>**/customer/**</includedFile>
     <includedFile>**/<resource>/**</includedFile>
   </includedFiles>
   ```
Skip all of this for a normal new resource — EXPLICIT already covers it with zero POM
changes.

## Failure-modes table

| Symptom | Cause | Fix |
|---|---|---|
| `IllegalStateException: Expected exactly one contract fixture matching ...` | Fixture not on the test classpath yet, or two files match the glob | Rebuild `beer-store-contract` (`mvn clean install`) before `beer-store-application`; check for a duplicate/misnamed fixture |
| Generated `*IT` doesn't exist for the new resource | Contract folder wasn't picked up | Confirm the folder lives under `beer-store-contract/src/test/resources/contracts/<resource>/` and that `beer-store-contract` was rebuilt; EXPLICIT has no `<includedFiles>` filter so this is almost always a stale build |
| Contract test gets wrong/missing id in response | Seed order vs. `RESTART IDENTITY` mismatch | Ensure the truncate+seed order in `ContractTestBase` matches the id the contract pins, and that FK-dependent tables (e.g. `order` needing `customer`/`beer`) are seeded in dependency order |
| 404 contract unexpectedly returns 200 | The "not found" id happens to be seeded | Pick an id clearly outside the seeded range (e.g. `999`), or truncate that table entirely for the 404 case |
| MockMvc-generated test 404s for a non-`customer` resource | `ContractTestBaseMockMvc.standaloneSetup` only wires `CustomerController` | Either restrict the MOCKMVC execution's `<includedFiles>` to exclude the new resource (default/no change needed) or follow the MockMvc-widening recipe above |
| Consumer (`beer-store-client`) test 404s even though the contract looks correct | `beer-store-contract` wasn't reinstalled, so the reactor still resolves the old `-stubs` jar | `cd beer-store-contract && mvn clean install`, then rerun the client test |
| Consumer test written before the producer contract exists | Wrong order | Always add/verify the producer contract + producer `*IT` first, then the consumer test |
