# Testing strategy

This is a showcase, so the goal isn't "maximum coverage" but a **deliberately chosen shape**
that's worth demonstrating. That shape is the **honeycomb** (the model popularized by Spotify),
not the classic pyramid: a thin unit layer at the bottom, a thick integration layer carrying
most of the behavioral weight, and a thin "integrated"/contract layer at the top. This document
records *why* that shape was chosen, what belongs in each layer today, where the gaps are, and
how Allure should present the result. It is the single source of truth for the strategy; a short
pointer from `CLAUDE.md`/`ADR.md` back to this file is a deliberately deferred follow-up (see
§6), not done as part of this document.

## 0. Honeycomb, not pyramid — what that means here

The honeycomb has three bands:

- **Integrated (top, thin):** does the published API contract still hold, end-to-end, including
  for an external consumer? Expensive per-test, so keep this band small and representative.
- **Integration (middle, thick — the default):** does a use case work correctly through the real
  stack (REST → application service → domain → real database)? This is where **most** tests
  should live.
- **Implementation-detail / unit (bottom, thin):** does one self-contained piece of decision logic
  or computation behave correctly in isolation? Used *only* where a real dependency would add
  cost without adding confidence.

**Important distinction for this codebase:** the ArchUnit + Spring Modulith suite in
`architecture/` (`ModularityTests`, `DomainArchitectureTest`, `LayeredArchitectureTest`, etc.) is
**not** the pyramid's unit base. It's a separate, cheap, static **guardrail pillar** — it checks
structure (package placement, naming, module boundaries), never behavior, and runs in the same
`mvn test` phase as unit tests only because that's fast, not because it's architecturally a "unit
test." Don't let the number of classes in `architecture/` create the illusion of a pyramid; it's
orthogonal to the honeycomb entirely.

Given that framing, beer-store's behavioral test suite is **already honeycomb-shaped in
intent** — the `*IT` classes carry the real assertions, and unit tests are reserved for
self-contained domain logic (`CustomerTest`, `OrderTest`, `BeerTest`, `MoneyTest`). This document
formalizes that shape and gives concrete guidance for where new tests should land as the app
grows: **integration tests scale with every new use case, unit tests stay targeted, contract
tests guard only the published API seams.**

## 1. Spring Cloud Contract — the API-seam guard

**Role:** SCC's job is narrow and cheap — does the *published wire contract* (JSON shape, HTTP
status, field types) still hold, and does it still satisfy a real consumer
(`beer-store-client`, via WireMock/stub-runner)? It is explicitly **not** where deep business
behavior is verified — that's the integration layer's job (§2). Today, the entire contract
suite is `beer-store-contract/src/test/resources/contracts/customer/customer.yml` (3 contracts:
search-by-name, get-by-id, create), wired through `ContractTestBase`/`ContractTestBaseMockMvc` +
`ContractDataReader` (fixture-driven seeding via `customer.json`, read through the real
`CustomerMapper` so the seeded row matches the contract body exactly), and generated **twice**
— once in EXPLICIT mode (real HTTP via RestAssured) and once in MOCKMVC mode — into
`contract.explicit`/`contract.mockmvc`.

**Recommendation — extend coverage, consolidate the duplicated mode:**

- **Extend contracts to every published REST module**, not just `customer`:
  - `product` (`catalog`): one contract for `GET /catalog` (browse, a representative
    filter+sort combination) + one for `GET /catalog/{id}` (found) + one 404 case.
  - `order`: one contract for `POST /orders` (create, happy path) + one for `GET /orders/{id}`
    + one 404 case.
  - One representative happy path + one error case per module is enough — SCC is a seam guard,
    not where the full behavioral matrix lives (that's §2's job).
- **Consolidate EXPLICIT vs. MOCKMVC.** Today every module that gets a contract would otherwise
  generate the *same* 3 tests twice, which is pure Allure-report noise with no added confidence.
  Make **EXPLICIT** (real HTTP, `ContractTestBase`) the default for every module. Keep
  **MOCKMVC** only for `customer`, purely as a showcase that both generation modes exist and
  work — not because customer's contract needs double coverage.
- Keep the `ContractDataReader` fixture-seeding pattern; keep contract bodies minimal (only the
  fields the wire format actually needs to pin).
- **SOAP is out of scope for SCC** — Spring Cloud Contract's HTTP-oriented tooling doesn't fit
  the `CustomerWs` SOAP endpoint. That endpoint's contract is either a dedicated IT (§2) or a
  documented, currently-open gap — not something to force into SCC.

*(This is a recommendation for future work — the contract suite itself is not being extended as
part of this document.)*

## 2. Integration tests / `@SpringBootTest` — the honeycomb's thick middle

**Role:** the **default** way to verify a use case — exercised end-to-end through the real stack
(REST/application service → domain → a real Postgres via Testcontainers). This is where most new
behavioral coverage should go as the app grows. Mechanically: any non-`@DataJpaTest` `*IT` class
extends `base/IntegrationTest.java` (activates the `test` profile, imports
`TestcontainersConfig` → `@ServiceConnection PostgreSQLContainer`), runs under failsafe
(`mvn verify`), against real Postgres with Liquibase enabled and `ddl-auto: validate` — so
migrations are actually exercised, unlike local `spring-boot:run` (H2, `ddl-auto: create`,
Liquibase off).

**A refinement worth calling out: HTTP vs. in-process.** Today's ITs (e.g. `CatalogIT`,
`OrdersIT`) call the controller **directly as a Java method**
(`catalogController.browseBeers(...)`), not over the HTTP wire. That means request
deserialization, the real HTTP status code, and the `*RestExceptionHandler` → HTTP-response
mapping are **not** exercised by these tests today. Recommended division of labor, so this isn't
duplicated:

- **SCC (§1)** covers the *wire* — shape, status code, exception → HTTP mapping — for the
  representative happy-path/error cases per module.
- **IT** covers the *deep behavioral matrix* — filter/sort combinations, state machine
  transitions, cross-module effects — and it's fine for these to call the controller in-process,
  because the wire itself is already guarded by the contract layer. `CatalogIT` is the strongest
  existing example of this pattern (9+ scenarios: name/style/abv-range/price-range/availability
  filtering, price/name sorting, get-by-id, 404).

**New recommendation — `@ApplicationModuleTest` (Spring Modulith), closing ADR-19's gap:** a
module-scoped integration test that bootstraps a single module (e.g. `order`) while its ACL
ports (`CustomerLookup`, `BeerLookup`) are replaced with test doubles at the boundary. This gives
precise failure localization (a regression at the module boundary fails a boundary-scoped test,
not a broad `*IT`) and is the natural place to verify Modulith's event-publication registry —
does `OrderPlacedEventListener` actually consume `OrderPlaced` via
`@ApplicationModuleListener`? No test exercises that today; a full `*IT` only proves the whole
context wires up, not that the module boundary and event flow are individually correct. This is
squarely "integration" on the honeycomb, just at module granularity instead of full-application
granularity — a good showcase addition once ADR-19 is revisited.

**Priority backlog (documented gaps, not implemented here):**

| Gap | Why it matters |
|---|---|
| `order` REST: status transitions, `cancelOrder`, `getOrder`/`getOrders` | Only `create` has IT coverage today (`OrdersIT`); the state machine (`OrderStatus.canTransitionTo`) is unit-tested but never driven through the REST adapter |
| `customer` REST: update/delete/list | Only exercised via the application service in `CustomersIT`, never through `CustomerController` |
| SOAP `CustomerWs` (`getCustomer`) | **Zero tests** — no IT, no contract; the generated JAXB types and the SOAP endpoint are entirely unverified |
| Real `WebSecurityConfig` | Every test runs under `test` profile's permit-all `TestSecurityConfig`; Basic-auth and `/services/**` role enforcement are never exercised |
| Modulith event publication/consumption | `OrdersTest` mocks `ApplicationEventPublisher`; no test verifies `OrderPlacedEventListener` actually receives the event end-to-end |

## 3. Unit tests — targeted, not the default

**Use a unit test where there's self-contained decision logic or computation** with enough
branches that driving it through a full IT would be wasteful and slow:

- **Rich aggregate invariants/state machines/calculations:** `Customer` (register/updateProfile
  /suspend/activate invariants), `Order` (place/transitionTo/cancel/totalAmount,
  `OrderStatus.canTransitionTo`, the `beerIds()` flattening of repeated line items), `Beer`
  (create/validation), `Money` (add/multiply/negative-rejection). These are exactly the kind of
  logic the honeycomb's thin bottom band exists for — **keep these as-is.**
- **Complex pure algorithms / technical utilities:** `RequestResponseLoggingFilter` (via
  `MockHttpServletRequest`/`Response` + a Logback `ListAppender`), `ContractDataReaderTest`. Fine
  as POJO/Mockito units — no Spring context needed, no database needed.

**Don't write a mock-heavy unit test for thin orchestrator glue.** `Customers`/`Beers`/`Orders`
(the `@Service` implementing each `*Management` port) are intentionally thin — by design
(ADR-05), invariants live in the aggregate, not the service. A unit test that mocks
`OrderRepository`/`BeerLookup`/`CustomerLookup`/`ApplicationEventPublisher` to verify that
`Orders.placeOrder` calls them in the right order is testing **wiring**, not **behavior** — and
it duplicates what an IT already proves with a real database and a real event registry.
`OrdersTest` is the concrete example: its happy-path and unknown-beer-rejection scenarios would
be better served by an IT (or the `@ApplicationModuleTest` from §2, which additionally exercises
the *real* event-publication registry instead of a mock). Where a unit test like this is kept, keep it **minimal** — only for a branch of logic
that's genuinely awkward to trigger from an IT (e.g. asserting the precise exception type on an
edge case), not as a full behavioral duplicate of the IT.

**Rule of thumb:** *unit-test where there's a decision or a computation — not where there's
delegation.*

## 4. Allure — more readable reports (without CI trend/history)

Today's Allure setup (`beer-store-application`, `allure-jupiter` 2.35.3 + `allure-rest-assured`,
results in `target/allure-results`) is wired but the report is effectively **flat**: the only
signal is `@DisplayName` plus two `@Step` annotations (both on the contract base classes'
`setup()`) and a single `AllureRestAssured` filter (on `ContractTestBase`, missing from
`ContractTestBaseMockMvc`). There's no `@Epic`/`@Feature`/`@Story`/`@Severity`, no
`categories.json`, no `environment.properties`. The following would materially improve
readability without touching CI:

- **Behavioral taxonomy** (drives Allure's "Behaviors" view):
  - `@Epic` = bounded context/module — `"Customer"`, `"Product Catalog"`, `"Order"`.
  - `@Feature` = use case — `"Browse catalog"`, `"Place order"`, `"Order lifecycle"`,
    `"Customer registration"`.
  - `@Story` = the specific scenario within that feature.
- **`@Severity`** (`BLOCKER`/`CRITICAL`/`NORMAL`/`MINOR`) — lets a failed run be triaged by
  impact at a glance. E.g. `placeOrder` happy path = `CRITICAL`; a single filter edge case in
  `CatalogIT` = `MINOR`.
- **`@Tag` for test *type*** (`unit` / `integration` / `contract` / `architecture`) — lets the
  Allure report be filtered by honeycomb band directly. For the contract suite specifically,
  `contract-explicit` / `contract-mockmvc` tags disambiguate the two generation modes now that
  §1 recommends consolidating them (only `customer` keeps both).
- **`@DisplayName` + `@Description`** — `@DisplayName` stays the human-readable test name (already
  used consistently, e.g. `CatalogIT`); add `@Description` where the *intent* isn't obvious from
  the name alone (why this scenario matters, not just what it does).
- **`@Step` / `Allure.step(...)`** — promote the existing `//Given //When //Then` comment blocks
  (used consistently across ITs, e.g. `CatalogIT`) into real Allure steps, so the report shows
  the scenario's structure, not just a pass/fail leaf. Also attach `AllureRestAssured` to
  `ContractTestBaseMockMvc` (currently missing) and to the RestAssured-based ITs, so
  request/response bodies show up as report attachments consistently, not only for the EXPLICIT
  contract suite.
- **`categories.json`** — classifies failures instead of leaving them as one undifferentiated
  bucket: e.g. *"Product defects"* (assertion failures), *"Test defects"* (setup/broken tests),
  *"Infrastructure problems"* (Testcontainers/DB startup failures), *"Contract mismatch"* (SCC
  verification failures).
- **`environment.properties`** — stamps the report header with build/runtime metadata (Java 25,
  Spring Boot 4.1.0, Postgres 17, active profile, git SHA) so a report is self-describing without
  cross-referencing the CI run.
- **Reduce annotation boilerplate** — once the taxonomy above is adopted, consider a small
  `AllureLabels` constants holder or composed meta-annotations (e.g. a `@CustomerEpic`
  meta-annotation bundling `@Epic("Customer")`) so every test class doesn't hand-roll the same
  three labels.
- **Trend/history is deliberately out of scope.** CI currently redeploys a fresh Allure report to
  GitHub Pages on every master run (`simple-elf/allure-report-action@v1.14` with only
  `allure_results`/`allure_report` inputs — no `gh_pages`/history input), so there's no
  flaky-test or pass-rate trend graph across runs. That's a conscious choice for now — adding
  history support (e.g. checking out prior report state, or switching to a `gh-pages`-branch
  publish model) is a possible future enhancement, not part of this document.

*(These are recommendations for future work — no Allure annotations, `categories.json`, or
`environment.properties` are being added as part of this document.)*

## 5. When to use which test type

| What you're verifying | Tool | Honeycomb band |
|---|---|---|
| DDD module boundaries, naming, layering, allowed dependencies | ArchUnit + Spring Modulith (`architecture/`) | static guardrail (`mvn test`), not part of the honeycomb |
| A use case end-to-end through one module (+ real Postgres) | `*IT` extends `IntegrationTest` | **integration — the default** |
| A cross-module interaction or event flow, in isolation | `@ApplicationModuleTest` | integration (module-scoped) |
| The published wire contract (JSON shape, status, consumer compatibility) | Spring Cloud Contract (producer + `beer-store-client`) | integrated / API seam |
| An aggregate's invariants, state machine, or calculation | POJO unit test (AssertJ) | unit — targeted |
| A complex pure algorithm or technical utility | POJO/Mockito unit test | unit — targeted |
| Thin orchestrator glue / pure delegation | **an IT** (not a mock-only unit test) | avoid testing this as an implementation detail |

**Heuristic:** *default to an integration test. Only drop to a unit test when the logic is
self-contained and branch-heavy enough that a real dependency adds cost without adding
confidence. Only reach for a contract test at a published API boundary. Never write a mock-only
unit test for wiring that an integration test already exercises.*

## 6. Where this strategy is documented

This file, `TESTING_STRATEGY.md`, is the single source of truth for the testing strategy today.

**Deferred follow-up (not done as part of this document):** once this strategy is settled,
distill it into two places that already document conventions for this codebase:

- A short `### Testing strategy` subsection in `CLAUDE.md`, under `## Architecture` and just
  before the existing `### Testing conventions (enforced by TestingRulesTest)` section — pointing
  back here rather than duplicating the content.
- An **ADR-22** in `ADR.md`'s `### G. Testing` section ("Honeycomb testing strategy, not the
  classic pyramid" or similar), in the file's existing `**ADR-NN · Title** ✅|🔜` format with
  `Problem` / `Decision` / `Why` / `Cost` / `Evolve later` bullets — and, since §2 above proposes
  a concrete resolution for it, possibly revisiting **ADR-19** ("No module-level integration
  test") in light of the `@ApplicationModuleTest` recommendation.
