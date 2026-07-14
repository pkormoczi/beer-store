---
name: add-contract-test
description: This skill should be used when the user asks to "add a contract test",
  "write a Spring Cloud Contract test", "cover this endpoint with a contract", "add
  SCC coverage for X", "which endpoints still need contract tests", or in Hungarian
  "írj/adj hozzá contract/szerződéses tesztet", "SCC teszt", "fedd le szerződéssel
  a végpontot", "milyen végpontoknak nincs még contract tesztje". Audits which REST
  endpoints still lack Spring Cloud Contract coverage against the target matrix in
  docs/TESTING_STRATEGY.md §6, then walks through adding one end-to-end: the contract
  + fixture in beer-store-contract, ContractDataReader + seeding in beer-store-application,
  and the stub-runner test in beer-store-client.
version: 0.1.0
---

# Add contract test

Add Spring Cloud Contract (SCC) coverage for a REST endpoint, on both the **producer**
(`beer-store-contract` + the generated `*IT` suites in `beer-store-application`) and
**consumer** (`beer-store-client`, stub-runner) sides. Follow the existing
`customer`/`catalog` patterns exactly rather than inventing new ones.

## Role & guardrails (docs/TESTING_STRATEGY.md §6, ADR-22)

Contract tests are **narrow and cheap**: they pin the published wire contract (JSON
shape, HTTP status, field types) and confirm a real consumer can still parse it. They
are explicitly **not** where business behavior is verified — that's a module test's
job. Keep contract bodies minimal (only the fields the wire format needs) and keep
per-module coverage to **one happy path + one error case**; the deep filter/sort/
validation matrix belongs in module tests, never here. SOAP (`CustomerWs`) is out of
scope for SCC — its tooling is HTTP/JSON-oriented.

Read `docs/TESTING_STRATEGY.md` §6 before adding a contract if it's been a while —
it's the operative strategy doc; this skill is the mechanical how-to for what it
prescribes.

## Step 0 — Gap audit (run this first)

Compare what exists against the §6 target matrix, and report a have/missing table
before proposing what to add next:

| Module | Target (§6) | Producer contract exists? | Consumer test exists? |
|---|---|---|---|
| `customer` | search-by-name, get-by-id, create | ✅ all 3 (`contracts/customer/customer.yml`) | ✅ `CustomerControllerTest` |
| `product` | `GET /catalog` (one filter+sort combo), `GET /catalog/{id}`, one 404 | ⚠️ only the catalog-browse case (`contracts/catalog/catalog.yml`) — `GET /catalog/{id}` and the 404 are missing | ❌ no catalog consumer test |
| `order` | `POST /orders`, `GET /orders/{id}`, one 404 | ❌ no `contracts/order/` folder at all | ❌ no order consumer test |

To re-derive this instead of trusting the table above (it will go stale):
- Producer contracts: list folders under `beer-store-contract/src/test/resources/contracts/**`.
- Consumer tests: list classes under `beer-store-client/src/test/java/dev/ronin/demo/client/**`.
- Target: re-read `docs/TESTING_STRATEGY.md` §6's "Scope" table.

Propose the highest-value gap to close next (as of this writing: `order` has *zero*
coverage, which is the biggest gap) and confirm with the user before proceeding if it
wasn't already named.

## Mental model

```
beer-store-contract/src/test/resources/contracts/<resource>/{<resource>.yml,<resource>.json}
        │  (SCC plugin, <extensions>true</extensions> — packages contracts → stubs,
        │   runs no tests itself: skipTestOnly=true, mavenTestSkip=true)
        ▼
   beer-store-contract-<version>-stubs.jar
        │
        ├──▶ beer-store-application: spring-cloud-contract-maven-plugin generateTests,
        │    contractsMode=LOCAL, TWO executions:
        │      - EXPLICIT → contract.explicit, extends ContractTestBase — auto-discovers
        │        EVERY contract folder, no <includedFiles> filter. Default choice.
        │      - MOCKMVC  → contract.mockmvc, extends ContractTestBaseMockMvc — restricted
        │        to <includedFiles>**/customer/**</includedFiles>; its standaloneSetup only
        │        wires CustomerController. Customer-only showcase — leave it alone unless
        │        you're deliberately extending that showcase.
        │    nameSuffixForTests=IT → runs under failsafe on Testcontainers Postgres.
        │
        └──▶ beer-store-client: @AutoConfigureStubRunner(ids = "...:+:stubs") auto-discovers
             the same stub mappings — no stub-id list to maintain. The WebClient client +
             *Dto models regenerate from the OpenAPI spec automatically.
```

## Producer procedure

1. **Author the contract + fixture** in `beer-store-contract/src/test/resources/contracts/<resource>/`:
   - `<resource>.yml` — one or more `---`-separated YAML documents (SCC's own format,
     not Groovy — every existing contract in this repo is YAML). Each has `name`
     (camelCase `shouldXxx...` — becomes the generated test method / stub mapping
     name), a Gherkin-style `description`, `request` (method/url/queryParameters/
     headers/bodyFromFile), `response` (status/headers/bodyFromFile/matchers).
   - `<resource>.json` — the fixture the contract's `bodyFromFile` points at, in the
     **same folder**. Single object (customer) or JSON array (catalog) depending on
     the endpoint shape.
   - **Convention: never pin a server-assigned `id` literally.** Match it instead:
     ```yaml
     matchers:
       body:
         - path: $.id            # or $[0].id for an array element
           type: by_regex
           regexType: as_integer
           predefined: number
     ```
   - Beer/catalog fixtures must include `availability` (the field is `readOnly` on the
     `Beer` OpenAPI schema but still required in the fixture body).
   - Copy-paste templates (list-GET, get-by-id 200, 404, create-POST) are in
     `references/cookbook.md`.

2. **Extend `ContractDataReader`**
   (`beer-store-application/src/test/java/dev/ronin/demo/beerstore/contract/ContractDataReader.java`):
   add a `read<Resource>Data()` method built on the existing generic `read(fileName,
   type)` / `readList(fileName, elementType)` primitives — do **not** change the
   classpath glob (`classpath*:/**/contracts/**/%s.json`), it already matches any new
   fixture. Reuse a production `*Mapper` if one exists for the dto→view mapping
   (`readCustomerData()` reuses `CustomerMapperImpl`); otherwise write a small local
   `toView`/`toCommand` mapping the way `readCatalogData()` does (there's no
   production dto→view mapper for read-only resources).

3. **Seed the new resource in `ContractTestBase`**
   (`beer-store-application/src/test/java/dev/ronin/demo/beerstore/contract/ContractTestBase.java`,
   the EXPLICIT base — default target unless you're extending the MockMvc showcase):
   - `jdbcTemplate.execute("TRUNCATE TABLE <table> RESTART IDENTITY CASCADE")` before
     inserting, so the seeded row gets the deterministic id (`1`, `2`, …) the contract
     pins. **Respect FK/seed order** — e.g. an `order` contract needs `customer` and
     `beer` truncated+seeded *before* placing the order through them.
   - Seed through the **production application-service port**
     (`CustomerManagement.registerCustomer`, `BeerManagement.createBeer`,
     `OrderManagement.placeOrder`, …), never raw SQL inserts — this is what guarantees
     the seeded row matches the contract body exactly (same mappers the app uses).
   - Keep it **non-`@Transactional`**: the base's `@BeforeAll` runs outside any test
     transaction because the server serves the real HTTP request in its own
     transaction — seed data must be committed first.
   - A `404`/not-found contract needs the opposite: an id that is **not** seeded (or a
     table truncated with nothing inserted for that id) — see the cookbook's 404
     template.

4. **POM (`beer-store-application/pom.xml`) — usually no change.** The EXPLICIT
   `generateTests` execution has no `<includedFiles>` filter, so a new
   `contracts/<resource>/` folder is auto-discovered and emits
   `<Resource>IT extends ContractTestBase` on the next build. Only touch the POM to:
   - add a **third** `generateTests` execution if you're introducing a genuinely new
     test mode (own `<basePackageForTests>`/`<testMode>`/`<baseClassForTests>` — this
     repo's substitute for `baseClassMappings`), or
   - widen the MOCKMVC execution's `<includedFiles>**/customer/**</includedFiles>` —
     only if you're deliberately extending that showcase (see step 4a below); this
     also requires wiring the new controller into
     `ContractTestBaseMockMvc.standaloneSetup(...)` and seeding its table there too.

5. **Rebuild `beer-store-contract` first**, then the application module:
   ```
   cd beer-store-contract && mvn clean install
   ```
   so the `-stubs` jar actually carries the new fixture before
   `beer-store-application` unpacks it onto the test classpath
   (`unpack-stubs` → `target/test-classes`). `ContractDataReader.resolveFixture`
   throws `IllegalStateException` if the classpath glob doesn't resolve to exactly
   one file — a missing or duplicated fixture fails loudly rather than silently
   passing.

### 4a. (Rare) Extending the MockMvc showcase

`ContractTestBaseMockMvc` only demonstrates that MOCKMVC generation mode works; it's
deliberately scoped to `customer` (`docs/TESTING_STRATEGY.md` §6: "never as double
coverage for other modules"). Only widen it if the user explicitly wants a second
MOCKMVC example. Otherwise EXPLICIT alone is correct and sufficient for every new
resource.

## Consumer procedure (`beer-store-client`)

Nothing to configure — this is the pleasant surprise worth calling out to the user:

- The generated WebClient client (`<Resource>Api`) and `*Dto` models regenerate
  automatically from `beer-store-contract.yaml` on every build
  (`skipIfSpecIsUnchanged=true`). If the endpoint is already in the OpenAPI spec, the
  client method already exists — you never hand-write it.
- `@AutoConfigureStubRunner(ids = "dev.ronin.demo:beer-store-contract:+:stubs")` pulls
  the **whole** stubs jar — there is no per-contract stub-id list to extend. A new
  producer contract becomes available on the same WireMock instance automatically.

The only real work: **add a `@Test`** to the matching domain test class, or create a
new one mirroring
`beer-store-client/src/test/java/dev/ronin/demo/client/CustomerControllerTest.java` —
`@SpringBootTest(webEnvironment = NONE)` + `@AutoConfigureStubRunner(stubsMode =
CLASSPATH, ...)` + `@StubRunnerPort("beer-store-contract") int port` +
`WebTestClient.bindToServer().baseUrl(...)`. Skeleton in `references/cookbook.md`.

**Order matters**: the producer contract must exist first (step 1 above) — a consumer
test against a URL with no matching stub just gets a WireMock 404, which is a
misleading failure if you write the consumer test first.

## Build & verify

```
cd beer-store-contract && mvn clean install
mvn -pl beer-store-application verify -Dit.test=<Resource>IT
mvn -pl beer-store-client test
```

Run the full reactor build (`mvn -Dmaven.test.skip=true clean install` then `mvn
verify`) before considering the change done, since the generated `*IT` name depends
on the contract `name:` you chose.

## Report & commit

Report which files changed and why (contract+fixture / ContractDataReader / seeding /
consumer test), and whether the gap-audit table changed as a result. This repo commits
directly to `master` in small, separate commits — no feature branch or PR (see repo
memory: producer-side contract+seeding can be one commit, the consumer test another,
if they land in the same session).

## Gotchas checklist

- Fixture not found → rebuild `beer-store-contract` before `beer-store-application`.
- Seeded id doesn't match the contract's pinned id → check `RESTART IDENTITY` ran and
  seed order matches insertion order.
- New resource silently missing from generated tests → confirm you're relying on the
  EXPLICIT execution (no filter) and not accidentally scoped by MOCKMVC's
  `<includedFiles>`.
- Consumer test 404s even though the producer contract looks right → confirm
  `beer-store-contract` was rebuilt/reinstalled so the reactor picks up the new stub.
- FK violation seeding an `order` fixture → seed `customer`/`beer` first, in that
  order, before placing the order.

Full templates, JSON shapes, and a failure-mode table: `references/cookbook.md`.
