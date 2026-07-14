# Testing strategy

Operative rules for every test written in this repository. `CLAUDE.md` describes what exists;
`ADR.MD` describes why; this file prescribes **where a given behavior gets tested, and where it
must not**. Entries follow the repo convention: **✅ Standing** (apply now) / **🔜 To implement**
(target state, migration pending). When a rule here conflicts with an existing test, the existing
test is the one that changes (rewrite at the right level, or delete).

Guiding shape: a **honeycomb, not a pyramid** — few solitary unit tests, a dominant middle layer of
module-scoped tests, a thin integrated top. Two principles govern everything below:

1. **Test where the logic lives.** A behavior is asserted at exactly one level — the level closest
   to where it is implemented. Everything below or beside that level is a GIVEN (setup or a test
   double), never an assertion target.
2. **No duplicate coverage — ever.** If a branch is naturally covered by a contract or integration
   test, do not add a unit test for it. If it is a small class's special/edge behavior, cover it
   with a unit test only and do not drag it through an IT or contract test.

## 1. Test levels and their exclusive responsibilities ✅

| Level | Harness / naming | Runs under | Owns (asserts) | Must NOT assert |
|---|---|---|---|---|
| **Domain unit** | plain JUnit + AssertJ, `*Test` | surefire (`mvn test`) | aggregate invariants, factory/behavior methods, state transitions, value objects (`Money`), edge cases of small pure classes | anything requiring Spring, persistence, or another class's orchestration |
| **Module test** | `@ApplicationModuleTest`, `*IT`, bootstraps one module; own persistence real (Testcontainers Postgres); neighbor modules doubled at the boundary | failsafe (`mvn verify`) | the module's behavior through its `*Management` api: orchestration + mapping + persistence round-trip; **event reactions** via the Modulith `Scenario` API | domain invariants (level above re-asserting them = duplicate coverage); another module's internal state |
| **Saga IT** | `@SpringBootTest` extends `IntegrationTest`, `*IT`, lives only in the designated saga/e2e test package | failsafe | that a cross-module event chain **closes**: terminal state + completed event publications for one happy path (and, later, one compensation path) per saga | any single module's branch logic; intermediate hops of the choreography |
| **Contract test** | Spring Cloud Contract, generated from `beer-store-contract` | failsafe 🔜 (see §3) | the external REST/SOAP wire contract (status codes, payload shapes); consumer side verified in `beer-store-client` against the generated WireMock stubs | business behavior behind the endpoint (module test's job) |
| **Architecture test** | ArchUnit + Modulith `verify()`, `architecture/` package | surefire | structure: boundaries, naming, layering, and the enforcement rules in §7 | — |

Level-selection heuristic when in doubt: *"can the branch be reached cheaply from the module's api
or the wire contract?"* → yes: test it there, no unit test. *"Is it an edge case of one small
class?"* → unit test only, nothing above.

## 2. Where domain invariants are tested ✅

Exclusively in plain unit tests on the aggregates/value objects (`OrderTest`, `CustomerTest`,
`BeerTest`, `MoneyTest` — this investment stays). Aggregates are self-validating immutable records
(ADR-05), so the compact constructor is the cheapest, closest test site. Module tests and saga ITs
*rely on* invariants holding but never re-assert them.

Corollary — **solitary Mockito tests of the thin aggregate services are retired** 🔜: `OrdersTest`
(mocks `OrderRepository` + `ApplicationEventPublisher`) is deleted once the `order` module test
exists; a thin orchestrator's only observable behavior is its wiring, and the module test exercises
exactly that wiring against real persistence. A solitary service unit test is permitted again only
if a service ever grows genuinely branchy coordination logic — and that is a smell worth a design
discussion first.

## 3. Build mapping — the hard line ✅ / 🔜

**surefire (`mvn test`) = no Spring context, no Docker. Ever.** Everything that boots a Spring
context is named `*IT` and runs under failsafe against Testcontainers Postgres.

Consequences:
- `@ApplicationModuleTest` classes are `*IT` (they are `@SpringBootTest`-meta-annotated — the
  existing `TestingRulesTest.springBootTestShouldBeNamedIT` rule already implies this and stops
  being `allowEmptyShould` once the first module test lands).
- 🔜 The generated contract suites (`contract.explicit` / `contract.mockmvc`) currently run under
  surefire on H2 — they move to failsafe (`spring-cloud-contract-maven-plugin` test-name suffix
  config) and onto Testcontainers Postgres; `ContractTestBase`/`ContractTestBaseMockMvc` import
  `TestcontainersConfig`.
- 🔜 With that, **H2 disappears from the test tree entirely** (it remains only for local
  `spring-boot:run`). Rationale: the JDBC event publication registry is itself a persistence
  feature — verifying event behavior on a database we don't run in production is a green-but-lying
  suite.

## 4. Test doubles under hexagonal ports ✅

The **only legitimate in-process mock seam is another module's boundary.** Concretely, per level:

| Collaborator | Domain unit | Module test | Saga IT |
|---|---|---|---|
| own aggregates / value objects | real | real | real |
| own `*Repository` port + persistence | n/a | **real** (Testcontainers) | real |
| other module (via own ACL port, e.g. `CustomerLookup`, `BeerLookup`) | n/a | **doubled** (`@MockitoBean` on the ACL port) | real |
| Modulith event registry | n/a | real (`Scenario`) | real |
| external systems | — none exist today; when they do (see §8), stub at the outbound adapter (WireMock), never above it | | |

Rules:
- Prefer mocking the consuming module's **own ACL port** over the foreign `*Management` facade —
  the test then doesn't even compile against another module's surface.
- Never mock domain aggregates, value objects, or your own repository/persistence inside a module
  test.
- `beer-store-client` keeps consuming the contract module's generated WireMock stubs — that *is*
  its external-boundary double, and it stays contract-derived, never hand-written.

## 5. Events and event testing ✅ / 🔜

- **Placement:** a cross-module event is an immutable record in the **emitting module's `api`**
  (e.g. `order.api.event.OrderPlacedEvent`), exposed through the module's named interface. No
  central shared event package. Module-internal events (today's `OrderPlaced`) stay in
  `domain/event` and are invisible outside.
- **Contract:** inside the monolith the compiler + Modulith `verify()` *are* the event contract.
  No event contract tests while consumers are in-JVM (trigger to revisit in §8).
- **Reaction testing (the module-test workhorse):** a module's reaction to another module's event
  is verified in its own module test via `Scenario`:
  `scenario.publish(new OrderPlacedEvent(...)).andWaitForStateChange(...)` — through the real
  registry, real transaction boundaries, real async dispatch. Calling the
  `@ApplicationModuleListener` method directly is allowed only as an *additional* unit test when
  the listener body itself is branchy — never as the only reaction test.
- **Chain testing:** each saga gets exactly one happy-path saga IT now, asserting terminal state +
  completed publications only. Compensation-path saga ITs are added per saga **when its
  compensation logic is designed and implemented** — not before.
- **Idempotency:** deliberately out of scope. At-least-once redelivery tolerance and its test
  pattern get decided together with the idempotency mechanism itself, later. Nothing here should
  be read as a substitute.

## 6. Coverage policy ✅

No per-level thresholds. One aggregated Jacoco figure — surefire **and** failsafe execution data
merged — feeds SonarCloud, where the single quality gate is **80%**. Coverage is an output of
following §1's level-selection rules, never a target that shapes individual tests.
🔜 Verify the CI Jacoco report actually merges failsafe exec data once the test migration shifts
weight toward failsafe.

## 7. Enforcement — ArchUnit additions 🔜

To be added to `TestingRulesTest`/`ArchitectureSupport` (rules > review comments, per ADR-18):

1. Cross-module event listeners use `@ApplicationModuleListener` — never bare `@EventListener` /
   `@TransactionalEventListener`.
2. Event types referenced across modules are records residing in the emitting module's `api`.
3. Full-context `@SpringBootTest` (non-abstract, other than the contract bases) is allowed **only**
   in the designated saga/e2e package — everything else is `@ApplicationModuleTest`, a slice, or
   plain JUnit.
4. `@ApplicationModuleTest` classes are named `*IT` and reside in the package of the module they
   bootstrap.
5. `@MockitoBean` may target only types in `..api..` or `..application.port.out..` — no doubling
   of another module's internals or one's own domain.
6. `*Test` classes (surefire) must not depend on `org.springframework.modulith.test` or boot any
   Spring context (extends the existing `springBootTestShouldBeNamedIT`).

## 8. Deferred decisions and their triggers ✅

| Deferred item | Trigger to decide/implement |
|---|---|
| Idempotent-consumer test pattern | when idempotency is introduced (mechanism decision first: per-listener dedup vs central component) |
| Compensation-path saga ITs | per saga, when its failure branches + compensating actions are designed |
| Event/message contract tests + Testcontainers Kafka | when the `payment` module is extracted into the separate microservice communicating over Kafka: that boundary gets consumer-driven messaging contract tests, the Kafka ACL adapter gets broker-backed integration tests, and saga ITs double `payment` at its port |
| External-service stubbing (WireMock at outbound adapters) | when the first real third-party dependency appears |

## 9. Forbidden anti-patterns ✅

Each ban names the level that owns the concern instead:

1. **Asserting another module's internal state** in a module test — other modules are GIVEN
   (double at the boundary); their behavior is owned by *their* module test.
2. **Re-asserting domain invariants above the unit level** — owned by domain unit tests.
3. **Mocking domain aggregates or value objects** — construct real ones; they're records.
4. **`Thread.sleep` / naked polling for event effects** — owned by the `Scenario` API.
5. **Direct listener-method invocation as the only reaction test** — bypasses the registry,
   transactions, and async dispatch; owned by the module test.
6. **H2 anywhere under `src/test`** — owned by Testcontainers Postgres (§3).
7. **Seeding through another module's repository or tables** — set up state via the owning
   module's api, or the test's own module fixtures. (The contract bases' direct `CustomerData`
   seeding is the known 🔜 exception until their migration in §3.)
8. **Asserting on log output, or interaction counts (`verify(times(1))`) where a state assertion
   is possible** — assert outcomes, not conversations.
9. **Full-context `@SpringBootTest` for anything answerable at module level** — owned by
   `@ApplicationModuleTest`.
10. **Asserting intermediate hops in a saga IT** — couples the test to choreography order; assert
    terminal state + completed publications only. Per-hop behavior is owned by each module test.

## 10. Migration of existing tests 🔜

| Existing | Fate |
|---|---|
| `OrderTest`, `CustomerTest`, `BeerTest`, `MoneyTest` | keep — canonical domain unit tests |
| `OrdersTest` (Mockito service test) | delete once the `order` module test covers the wiring (§2) |
| `OrdersIT`, `CustomersIT`, `CatalogIT` (full-context) | re-scope into `@ApplicationModuleTest` module tests; keep at most the saga-relevant flow as a saga IT |
| `CustomerJpaRepositoryIT` | keep — persistence slice against Postgres |
| `ContractTestBase(-MockMvc)` + generated suites | move to failsafe + Testcontainers Postgres (§3) |
| `architecture/` suite | keep; extend per §7 |
| `RequestResponseLoggingFilterTest`, `ContractDataReaderTest` | keep — plain unit tests of small classes (§1 heuristic) |
