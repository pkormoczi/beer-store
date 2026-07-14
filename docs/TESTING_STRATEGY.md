# Testing strategy

Operative rules for every test written in this repository. `CLAUDE.md` describes what exists;
`docs/ADR.MD` records why (**ADR-22** is this strategy's decision record); this file prescribes **where a
given behavior gets tested — and where it must not be**. Every rule below applies immediately to all
new tests. Existing tests carry no legacy protection (§13): when one conflicts with a rule here, the
test is the thing that changes — rewritten at the right level, or deleted.

## 0. Guiding principles — honeycomb, not pyramid

The suite's target shape is the **honeycomb** (the model popularized by Spotify's microservice
testing article), not the classic pyramid: a thin unit band at the bottom, a **dominant middle band
of module-scoped tests**, and a thin integrated/contract band at the top. The pyramid's
"many unit tests, heavy mocking" base fits layered monoliths with few entry points and buried logic.
This app is the opposite: every feature is published on an API (a well-defined testing surface),
domain logic is concentrated in small self-validating aggregates (ADR-05), and the remaining code is
orchestration + mapping + persistence — exactly the kind of behavior that only means something when
exercised through the real stack, not through mock choreography.

Five principles govern everything below:

1. **Test where the logic lives.** A behavior is asserted at exactly one level — the level closest
   to where it is implemented. Everything below or beside that level is a GIVEN (setup or a test
   double), never an assertion target.
2. **No duplicate coverage — ever.** If a branch is naturally covered by a module or contract test,
   do not add a unit test for it. If it is a small class's edge behavior, cover it with a unit test
   only and do not drag it through a module test.
3. **Another module's — or another system's — behavior is always a GIVEN, never an assertion
   target.** We test what *our* code does with a collaborator's answer, never whether the
   collaborator answers correctly; that is owned by the collaborator's own tests (and, at published
   seams, by the contract layer).
4. **Assert outcomes, not conversations.** Prefer state assertions over interaction assertions;
   `verify(times(1))` is a smell wherever a state check is possible.
5. **The `architecture/` suite is not part of the honeycomb.** ArchUnit + Spring Modulith
   `verify()` form a separate, cheap, static **guardrail pillar** — they check structure (package
   placement, naming, module boundaries), never behavior. They run under `mvn test` because that is
   fast, not because they are unit tests.

## 1. Test levels and their exclusive responsibilities

| Level | Harness / naming | Runs under | Owns (asserts) | Must NOT assert |
|---|---|---|---|---|
| **Domain unit** | plain JUnit + AssertJ, `*Test` | surefire (`mvn test`) | aggregate invariants, factory/behavior methods, state transitions, value objects (`Money`), edge cases of small pure classes | anything requiring Spring, persistence, or another class's orchestration |
| **Module test — the default middle band** | `@ApplicationModuleTest`, `*IT`, bootstraps one module; own persistence real (Testcontainers Postgres); neighbor modules doubled at the boundary (§4) | failsafe (`mvn verify`) | the module's behavior through its `*Management` api: orchestration + mapping + persistence round-trip; **event reactions** via the Modulith `Scenario` API (§5) | domain invariants (re-asserting them = duplicate coverage); another module's internal state |
| **Saga / e2e IT** | `@SpringBootTest` extends `IntegrationTest`, `*IT`, lives **only** in the designated saga/e2e test package | failsafe | that a cross-module chain **closes**: terminal state + completed event publications for one happy path (and, later, one compensation path) per saga | any single module's branch logic; intermediate hops of the choreography |
| **Contract test** | Spring Cloud Contract, generated from `beer-store-contract` (§6) | failsafe | the published REST wire contract (status codes, payload shapes); consumer side verified in `beer-store-client` against the generated WireMock stubs | business behavior behind the endpoint (the module test's job) |
| **Architecture test** | ArchUnit + Modulith `verify()`, `architecture/` package | surefire | structure: boundaries, naming, layering, and the enforcement rules in §10 | — |

### When in doubt — which level owns what

| What you're verifying | Level |
|---|---|
| An aggregate's invariants, state machine, or calculation | domain unit |
| A complex pure algorithm or technical utility (e.g. a servlet filter, a fixture reader) | plain/Mockito unit |
| A use case through a module's `*Management` api, including its persistence round-trip | **module test — the default** |
| A module's reaction to another module's event | module test (`Scenario`) |
| A cross-module event chain reaching its terminal state | saga IT (one per saga) |
| The published wire format (JSON shape, HTTP status, consumer compatibility) | contract test |
| Real security configuration (auth, roles) | dedicated security IT (§7) |
| Thin orchestrator glue / pure delegation | **module test** — never a mock-only unit test |
| Package placement, naming, module boundaries, layering | architecture test |

**Heuristic:** *"can the branch be reached cheaply from the module's api or the wire contract?"* →
yes: test it there, no unit test. *"Is it an edge case of one small class?"* → unit test only,
nothing above. Default to a module test; drop to a unit test only when the logic is self-contained
and branch-heavy enough that a real dependency adds cost without adding confidence; reach for a
contract test only at a published API seam; reach for a saga IT only for a cross-module chain.

## 2. Domain invariants — unit tests only

Aggregate and value-object behavior (`Order`, `Customer`, `Beer`, `Money`, `OrderStatus`
transitions) is tested exclusively in plain unit tests. Aggregates are self-validating immutable
records (ADR-05), so the compact constructor and the intention-revealing factory/behavior methods
are the cheapest, closest test site. Module tests and saga ITs *rely on* invariants holding but
never re-assert them.

Corollary — **no solitary Mockito tests of the thin aggregate services** (`Customers`/`Beers`/
`Orders`). A thin orchestrator's only observable behavior is its wiring, and the module test
exercises exactly that wiring against real persistence and a real event registry. A mock-heavy
service unit test verifies the conversation, not the outcome — it duplicates the module test while
proving less. A solitary service unit test is permitted only if a service ever grows genuinely
branchy coordination logic — and that is a smell worth a design discussion first.

**Rule of thumb:** *unit-test where there's a decision or a computation — not where there's
delegation.*

## 3. Build mapping — the hard line

**surefire (`mvn test`) = no Spring context, no Docker. Ever.** Everything that boots a Spring
context is named `*IT` and runs under failsafe (`mvn verify`) against Testcontainers Postgres
(`base/TestcontainersConfig.java`, `@ServiceConnection PostgreSQLContainer`), with Liquibase enabled
and `ddl-auto: validate` — so migrations are actually exercised on the same database engine
production runs.

Consequences:
- `@ApplicationModuleTest` classes are `*IT` (they are `@SpringBootTest`-meta-annotated — the
  existing `TestingRulesTest.springBootTestShouldBeNamedIT` rule already implies this, and its
  `allowEmptyShould(true)` marker is removed once the first module test lands).
- The generated contract suites also run under failsafe on Testcontainers Postgres
  (`spring-cloud-contract-maven-plugin` test-name suffix configuration;
  `ContractTestBase`/`ContractTestBaseMockMvc` import `TestcontainersConfig`).
- **H2 does not appear anywhere under `src/test`.** It remains only for local `spring-boot:run`.
  Rationale: the Modulith JDBC event publication registry is itself a persistence feature —
  verifying event behavior on a database we don't run in production is a green-but-lying suite.

## 4. Test doubles under hexagonal ports

The **only legitimate in-process mock seam is another module's boundary.** Concretely, per level:

| Collaborator | Domain unit | Module test | Saga IT |
|---|---|---|---|
| own aggregates / value objects | real | real | real |
| own `*Repository` port + persistence | n/a | **real** (Testcontainers) | real |
| other module (via own ACL port, e.g. `CustomerLookup`, `BeerLookup`) | n/a | **doubled** (`@MockitoBean` on the ACL port) | real |
| Modulith event registry | n/a | real (`Scenario`) | real |
| external systems | — none exist today; when they do (§11), stub at the outbound adapter (WireMock), never above it | | |

Rules:
- Double the consuming module's **own ACL port**, never the foreign `*Management` facade — the test
  then doesn't even compile against another module's surface.
- Never mock domain aggregates, value objects, or your own repository/persistence inside a module
  test — construct real ones; they're records.
- `beer-store-client` consumes the contract module's generated WireMock stubs — that *is* its
  external-boundary double, and it stays contract-derived, never hand-written.

## 5. Events and event testing

- **Placement:** a cross-module event is an immutable record in the **emitting module's `api`**
  (e.g. `order.api.event.OrderPlacedEvent`), exposed through the module's named interface. No
  central shared event package. Module-internal events (today's `OrderPlaced`) stay in
  `domain/event` and are invisible outside.
- **Contract:** inside the monolith the compiler + Modulith `verify()` *are* the event contract.
  No event contract tests while consumers are in-JVM (revisit trigger in §11).
- **Reaction testing (the module-test workhorse):** a module's reaction to an event is verified in
  its own module test via the Modulith `Scenario` API —
  `scenario.publish(new OrderPlacedEvent(...)).andWaitForStateChange(...)` — through the real
  registry, real transaction boundaries, real async dispatch. Calling the
  `@ApplicationModuleListener` method directly is allowed only as an *additional* unit test when the
  listener body itself is branchy — never as the only reaction test.
- **Chain testing:** each saga gets exactly one happy-path saga IT, asserting terminal state +
  completed event publications only. Compensation-path saga ITs are added per saga **when its
  compensation logic is designed and implemented** — not before.
- **Idempotency:** deliberately out of scope. At-least-once redelivery tolerance and its test
  pattern are decided together with the idempotency mechanism itself (§11).

## 6. Contract tests — Spring Cloud Contract at the published REST seams

**Role:** narrow and cheap — does the *published wire contract* (JSON shape, HTTP status, field
types) still hold, and does it still satisfy a real consumer (`beer-store-client`, via
WireMock/stub-runner)? It is explicitly **not** where business behavior is verified — that is the
module test's job. Contract bodies stay minimal: only the fields the wire format actually needs to
pin.

**Scope — every published REST module, one happy path + one error case each:**

| Module | Contracts |
|---|---|
| `customer` | search-by-name, get-by-id, create (the existing three) |
| `product` | `GET /catalog` (one representative filter+sort combination), `GET /catalog/{id}`, one 404 |
| `order` | `POST /orders` (happy path), `GET /orders/{id}`, one 404 |

The deep behavioral matrix (filter/sort combinations, state transitions, validation branches) never
lives here — it belongs to module tests. Because generation is contract-first, extending coverage
always starts in `beer-store-contract` (contracts under `src/test/resources/contracts/**`), followed
by `mvn clean install` in that module.

**Generation mode:** **EXPLICIT** (real HTTP via RestAssured, `ContractTestBase`) is the default for
every module — it exercises the full wire: request deserialization, real status codes, and the
`*RestExceptionHandler` → HTTP-response mapping. **MOCKMVC** (`ContractTestBaseMockMvc`) is kept
**only for `customer`**, purely as a showcase that both generation modes exist and work — never as
double coverage for other modules.

**Seeding:** the `ContractDataReader` fixture-seeding pattern stays — the test base seeds data read
from the contract's own JSON fixtures through the real mapper, so the seeded row matches the
contract body exactly.

**SOAP is out of scope for SCC** — Spring Cloud Contract's HTTP/JSON-oriented tooling doesn't fit
the `CustomerWs` endpoint. SOAP coverage is a documented deferred item (§11), not something to force
into this layer.

## 7. Security tests — one dedicated package, real configuration

All module tests, saga ITs, and contract tests run under the `test` profile's permit-all
`TestSecurityConfig` — security must not be re-asserted in every functional test. Instead, a
**small, dedicated security IT package** runs against the **real `WebSecurityConfig`** and owns,
once, the cross-cutting behavior:

- unauthenticated requests to protected paths are rejected (Basic-auth challenge),
- `/services/**` role enforcement holds for the SOAP surface,
- deliberately public paths (e.g. Swagger UI, actuator health) stay reachable.

This is the only place where the real security filter chain is exercised; everywhere else, security
is a GIVEN. New security rules (new roles, new protected paths) extend this package, never the
functional suites.

## 8. Allure reporting taxonomy — mandatory for new tests

Every new test carries the taxonomy below, so the Allure report's Behaviors view mirrors the module
structure and the honeycomb bands are directly filterable:

- **`@Epic`** = bounded context/module — `"Customer"`, `"Product Catalog"`, `"Order"` (platform
  concerns: `"Platform"`).
- **`@Feature`** = use case — e.g. `"Browse catalog"`, `"Place order"`, `"Order lifecycle"`.
- **`@Story`** = the specific scenario within that feature.
- **`@Severity`** — triage by impact at a glance: a `placeOrder` happy path is `CRITICAL`; a single
  filter edge case is `MINOR`.
- **`@Tag` for the level** — `unit` / `module` / `saga` / `contract` / `architecture`, plus
  `contract-explicit` / `contract-mockmvc` to disambiguate the two generation modes.
- **`@DisplayName`** on every test (human-readable scenario name); add **`@Description`** where the
  *intent* isn't obvious from the name alone.
- **Given/When/Then structure as `Allure.step(...)`** (or `@Step` methods), so the report shows the
  scenario's structure, not just a pass/fail leaf. Attach `AllureRestAssured` to every
  RestAssured-based test (both contract bases included), so request/response bodies appear as
  attachments consistently.
- **`categories.json`** classifies failures: *Product defects* (assertion failures), *Test defects*
  (setup/broken tests), *Infrastructure problems* (Testcontainers/DB startup), *Contract mismatch*
  (SCC verification failures).
- **`environment.properties`** stamps the report with build/runtime metadata (Java, Spring Boot,
  Postgres versions, active profile, git SHA) so a report is self-describing.
- **Reduce boilerplate with composed meta-annotations** (e.g. a `@CustomerEpic` bundling
  `@Epic("Customer")`) once the same labels repeat across a module's test classes.
- **Trend/history is deliberately out of scope** — CI redeploys a fresh report per run; adding
  history is a possible future enhancement (§11), not part of this strategy.

## 9. Coverage policy

No per-level thresholds. One aggregated Jacoco figure — surefire **and** failsafe execution data
merged — feeds SonarCloud, where the single quality gate is **80%**. Coverage is an output of
following §1's level-selection rules, never a target that shapes individual tests. As the suite's
weight shifts toward failsafe, verify the CI Jacoco report actually merges failsafe exec data.

## 10. Enforcement — ArchUnit additions

Rules > review comments (ADR-18). To be added to `TestingRulesTest`/`ArchitectureSupport` alongside
the first tests that make them non-empty:

1. Cross-module event listeners use `@ApplicationModuleListener` — never bare `@EventListener` /
   `@TransactionalEventListener`.
2. Event types referenced across modules are records residing in the emitting module's `api`.
3. Full-context `@SpringBootTest` (non-abstract, other than the contract bases) is allowed **only**
   in the designated saga/e2e package and the security package (§7) — everything else is
   `@ApplicationModuleTest`, a slice, or plain JUnit.
4. `@ApplicationModuleTest` classes are named `*IT` and reside in the package of the module they
   bootstrap.
5. `@MockitoBean` may target only types in `..api..` or `..application.port.out..` — no doubling of
   another module's internals or one's own domain.
6. `*Test` classes (surefire) must not depend on `org.springframework.modulith.test` or boot any
   Spring context (extends the existing `springBootTestShouldBeNamedIT`, whose
   `allowEmptyShould(true)` is removed once the first module test lands).

## 11. Configuration contracts & deferred decisions

**Configuration is a contract too.** The application has a contract with its environment through its
configuration: URLs must be reachable, credentials valid, schemas accessible. The standing principle
is that this contract is verified as close to the application as possible — startup-time config
validation and custom health indicators exposed on the actuator, rather than hoping a deploy-time
smoke test catches it. Implementation is trigger-based, like everything below.

| Deferred item | Trigger to decide/implement |
|---|---|
| Config validation / custom health indicators | when the first environment-specific external dependency (real DB URL, queue, third party) appears in a deployed environment |
| Idempotent-consumer test pattern | when idempotency is introduced (mechanism decision first: per-listener dedup vs central component) |
| Compensation-path saga ITs | per saga, when its failure branches + compensating actions are designed |
| Event/message contract tests + Testcontainers Kafka | when a module (e.g. `payment`) is extracted into a separate service communicating over a broker: that boundary gets consumer-driven messaging contracts, the broker ACL adapter gets broker-backed integration tests, and saga ITs double it at its port |
| External-service stubbing (WireMock at outbound adapters) | when the first real third-party dependency appears |
| SOAP `CustomerWs` coverage (dedicated IT through the generated JAXB types) | when the SOAP surface next changes, or before any consumer depends on it in production |
| Allure trend/history on the published report | when flaky-test/pass-rate trends become worth tracking across runs |

## 12. Forbidden anti-patterns

Each ban names the level that owns the concern instead:

1. **Asserting another module's internal state** in a module test — other modules are GIVEN (double
   at the boundary); their behavior is owned by *their* module test.
2. **Re-asserting domain invariants above the unit level** — owned by domain unit tests (§2).
3. **Mocking domain aggregates or value objects** — construct real ones; they're records (§4).
4. **Mock-heavy unit tests of a thin orchestrator service** — its wiring is owned by the module
   test, against real persistence (§2).
5. **`Thread.sleep` / naked polling for event effects** — owned by the `Scenario` API (§5).
6. **Direct listener-method invocation as the only reaction test** — bypasses the registry,
   transactions, and async dispatch; owned by the module test (§5).
7. **H2 anywhere under `src/test`** — owned by Testcontainers Postgres (§3).
8. **Seeding through another module's repository or tables** — set up state via the owning module's
   api, or the test's own module fixtures. (The contract bases' direct `CustomerData` seeding is the
   documented exception, scoped to the contract layer's fixture pattern in §6.)
9. **Asserting on log output, or interaction counts (`verify(times(1))`) where a state assertion is
   possible** — assert outcomes, not conversations (§0).
10. **Full-context `@SpringBootTest` for anything answerable at module level** — owned by
    `@ApplicationModuleTest` (§1, §10).
11. **Asserting intermediate hops in a saga IT** — couples the test to choreography order; assert
    terminal state + completed publications only. Per-hop behavior is owned by each module test.
12. **Deep behavioral scenarios in a contract test** — contracts pin the wire format only; the
    behavioral matrix is owned by module tests (§6).

## 13. Existing tests

The rules above are effective immediately and the existing suite carries no legacy protection: any
existing test may be deleted or rewritten at the correct level without a migration plan — nothing in
it is load-bearing for this strategy. Concretely: mock-heavy service unit tests are superseded by
module tests (§2); full-context `*IT` classes outside the saga/e2e package are re-scoped into module
tests or deleted (§1); the domain unit tests and the `architecture/` suite match this strategy
as-is and simply continue. When touching any existing test, apply this document — do not preserve
its old shape.
