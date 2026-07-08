## Beer Store

[![CI](https://github.com/pkormoczi/beer-store/actions/workflows/ci.yml/badge.svg)](https://github.com/pkormoczi/beer-store/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pkormoczi_beer-store&metric=alert_status)](https://sonarcloud.io/project/overview?id=pkormoczi_beer-store)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=pkormoczi_beer-store&metric=coverage)](https://sonarcloud.io/project/overview?id=pkormoczi_beer-store)
[![Docker Image Size](https://img.shields.io/docker/image-size/pkormoczi/beer-store/latest)](https://hub.docker.com/r/pkormoczi/beer-store)

Beer Store is a simple proof of concept application focusing on the following features:

### Architecture & domain
 - [x] Multi module Maven build with Maven wrapper
 - [x] Spring Boot
 - [x] Domain Driven Design considerations
 - [x] Spring Framework with full java config
 - [x] Spring Data with Hibernate
 - [x] Database migrations with Liquibase
 - [x] MapStruct as adapter
 - [ ] Transaction management refinement
 - [ ] Events, with Spring application events
 - [ ] Spring Modulith (module boundaries, event publication, generated module docs)
 - [ ] Virtual threads (`spring.threads.virtual.enabled=true`, with a before/after load comparison)

### API layer
 - [x] SOAP Endpoints
 - [x] REST Controllers
 - [x] Swagger / OpenAPI, contract-first code generation
 - [x] Spring Security (REST + SOAP, basic authentication with roles)
 - [x] Spring Boot Actuator
 - [x] Exception and error handling and documentation in the REST Controllers
 - [ ] RFC 9457 Problem Details error responses (`spring.mvc.problemdetails.enabled=true`)
 - [ ] OAuth2/JWT resource server (alongside/instead of basic auth)
 - [ ] Async calls via SOAP

### Testing & quality
 - [x] Unit tests
 - [x] Integration tests
 - [x] Architecture tests (ArchUnit) enforcing DDD layering and naming conventions
 - [x] Test coverage tool
 - [x] Allure reports
 - [x] Spring Cloud Contracts
 - [x] Testcontainers: integration tests against a real Postgres (`@ServiceConnection`) instead of H2, exercising the Liquibase migrations
 - [ ] Dockerized E2E tests
 - [ ] DBRider

### CI/CD & DevOps
 - [x] GitHub Actions CI pipeline (build/test/coverage, SonarCloud, Allure + Trivy reports on GitHub Pages, Docker Hub publish)
 - [x] Static code analysis (SonarCloud)
 - [x] Container image vulnerability scanning (Trivy, report published to GitHub Pages)
 - [ ] Dependency vulnerability scanning (OWASP dependency-check)
 - [x] Layered OCI image creation for the application itself (Cloud Native Buildpacks, published to Docker Hub on master)
 - [ ] GraalVM native image build (Buildpacks `-Pnative`, optional CI job)
 - [ ] SBOM exposure via Actuator (`/actuator/sbom`, from the Buildpacks-generated SBOM)
 - [ ] Tag-based release flow (`-Drevision=$TAG`, semver-tagged Docker images instead of SHA+latest only)
 - [ ] Observability - metrics/tracing (Micrometer, OpenTelemetry, local Grafana/Prometheus/Tempo compose stack)
 - [ ] Profiling: local, integration test, docker, prod
 - [ ] Kubernetes deployment manifests / Helm chart, with actuator + liveness/readiness probes moved to a separate management port

### Misc
 - [ ] License

## CI/CD pipeline (GitHub Actions)

The `.github/workflows/ci.yml` workflow runs on every push to `master` and on pull requests targeting it:

1. Build `beer-store-contract` first (installed to the local repo, since dependent modules resolve its `-stubs` classifier from there), then the full reactor.
2. Run unit (Surefire) and integration (Failsafe) tests in one pass, with Jacoco coverage covering both. Test and coverage reports are uploaded as workflow artifacts.
3. Generate an [Allure](https://allurereport.org/) test report (request/response attachments on the RestAssured-based contract tests via `allure-rest-assured`).
4. Run [SonarCloud](https://sonarcloud.io/) static analysis (on `master` pushes and on same-repo pull requests) — coverage, code smells, and the quality gate. Dashboard: **https://sonarcloud.io/project/overview?id=pkormoczi_beer-store**.
5. On `master` pushes only:
  - Build a layered OCI image with Cloud Native Buildpacks (`spring-boot:build-image`, no Dockerfile).
  - Scan the image with [Trivy](https://trivy.dev/) (CRITICAL/HIGH, report-only — a scanner hiccup or a known base-image CVE doesn't block the pipeline).
  - Publish both the Allure and Trivy reports to **GitHub Pages**, behind a small landing page linking to each (`/allure/`, `/trivy/`).
  - Push the image to Docker Hub (tagged with the commit SHA and `latest`).

Live reports (updated on every `master` push): **https://pkormoczi.github.io/beer-store/**

## DEV notes

### Running locally
- `beer-store-contract` is a separate artifact and must be built first: `cd beer-store-contract && mvn clean install`.
- Then run the app: `./mvnw spring-boot:run` (from `beer-store-application`, or from the repo root with `-pl beer-store-application`).
- Locally the app uses the bundled `application.yml` as-is — H2 (file-based, `ddl-auto: create`), Liquibase disabled — no external configuration needed for a plain local run.
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`
- SOAP endpoint: `http://localhost:8080/services/customerService.wsdl` with user `user` and password `password`
- H2 console: `http://localhost:8080/console` (enabled via `spring.h2.console.enabled` in `application.yml`)

### Running locally against Postgres instead of H2
`application.yml` defaults to H2, but already carries a commented-out Postgres block with the values below — useful when you want the local run to match what the `*IT` tests and the real deployment use (Postgres + Liquibase, `ddl-auto: validate`, see `CLAUDE.md`).

1. Start a Postgres container (same image version the `*IT` tests run against via Testcontainers, `postgres:17-alpine`):
   ```
   docker run --name beer-store-postgres -p 5432:5432 \
     -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=postgres \
     -d postgres:17-alpine
   ```
2. Point the app at it — either uncomment the Postgres block in `application.yml` (and comment out the H2 one above it), or override on the command line without touching the file:
   ```
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/postgres --spring.datasource.driverClassName=org.postgresql.Driver --spring.datasource.username=user --spring.datasource.password=password --spring.liquibase.enabled=true --spring.jpa.hibernate.ddl-auto=validate"
   ```

### Building & running a container image
There's no Dockerfile in this repo — images are built with [Cloud Native Buildpacks](https://buildpacks.io/) via the `spring-boot-maven-plugin`'s `build-image` goal, the same mechanism the CI pipeline uses.

**Option 1 — pull the image CI publishes on every push to `master`:**
```
docker pull docker.io/pkormoczi/beer-store:latest
docker run -p 8080:8080 docker.io/pkormoczi/beer-store:latest
```

**Option 2 — build it locally:**
```
cd beer-store-contract && mvn clean install
./mvnw -pl beer-store-application spring-boot:build-image
docker run -p 8080:8080 beer-store-application:DEVELOP-SNAPSHOT
```
Without an explicit `imageName`, Buildpacks tags the image `docker.io/library/<artifactId>:<version>` (`beer-store-application:DEVELOP-SNAPSHOT` here, since that's the project's current `revision`). CI overrides this with `-Dspring-boot.build-image.imageName=docker.io/<dockerhub-user>/beer-store:<sha>` to publish under the name in Option 1.

Either way, the image starts up exactly like a plain local run (H2, no Liquibase) unless you override it — see below.

### External configuration
For a container run that actually matches the `*IT` test/production setup (Postgres, Liquibase enabled, schema validated instead of auto-created — see `CLAUDE.md`), start a Postgres container as shown above, then supply the overridden properties to the running container.

**Preferred: mount the `config/` directory.** Spring Boot auto-loads `application.properties`/`.yml` from a `file:./config/` directory next to the process's working directory — for a Paketo-built image that's `/workspace`, so mounting the repo's `config/` there is enough on its own, no per-property flags on the `docker run` command line at all:
```
docker run -p 8080:8080 -v "$(pwd)/config:/workspace/config" docker.io/pkormoczi/beer-store:latest
```
`config/application.properties` already carries the Postgres override values (datasource URL/credentials, `spring.liquibase.enabled=true`, `spring.jpa.hibernate.ddl-auto=validate`) — edit that file to add or change properties; the `docker run` command itself never needs to change as the property set grows, and there's nothing to keep in sync here in the README.

**Alternative: environment variables**, useful for a one-off override without touching the config file. `host.docker.internal` resolves to the host from inside a container on Docker Desktop (Windows/Mac); on plain Linux Docker Engine, put both containers on a shared user-defined network and use the Postgres container's name instead:
```
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/postgres \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=password \
  docker.io/pkormoczi/beer-store:latest
```


## Architecture decisions & evolution roadmap

This is a showcase, not a production system, so several decisions below are deliberately staged:
the simplest thing that demonstrates the pattern ships first, and the next increment is left as an
explicit, documented next step rather than guessed at. Each entry is a lightweight ADR — **Problem
· Decision · Why · Cost · Evolve later** — tagged **✅ Implemented** (a standing decision, not
expected to change) or **🔜 Gap** (the next planned increment, not yet built). Entries are numbered
for cross-reference. Several trace back to a more detailed target design explored in
`spring-modulith-webshop-showcase-plan_v3.md` (referenced below as "plan §n").

### A. Module boundaries & inter-module communication

**ADR-01 · Synchronous in-process facade calls, with an anti-corruption port on the caller** ✅
- **Problem:** `order` depends on both `customer` and `product`. How should it call them?
- **Decision:** `order` calls the other modules' public facades (`CustomerManagement`,
  `BeerManagement`) synchronously, in-process — but never directly: it goes through outbound
  ports it owns itself (`CustomerLookup`, `BeerLookup` in `order/application/port/out/`),
  each implemented by a package-private adapter (`CustomerLookupAdapter`, `BeerLookupAdapter`)
  that calls the facade and translates the result into `order`'s own types (`BeerView` →
  `BeerSnapshot`).
- **Why:** it's a strongly-consistent modular monolith — one JVM, one transaction, one database —
  so a plain in-process call is the honest representation of that; the port+adapter layer is kept
  specifically because it does real semantic translation and because `order` never imports another
  module's `api` types directly, so a facade shape change can't leak into `order`'s domain.
- **Cost:** compile-time coupling between modules (a facade signature change can break a caller at
  build time, not runtime); one extra port+adapter pair per cross-module dependency.
- **Evolve later:** keep this in-process for any use that must stay inside the placing transaction;
  if/when this ever became a distributed system, the adapter is exactly the seam where a remote
  client would replace the direct facade call — the port interface itself wouldn't need to change.
  Use asynchronous events (see D) only for genuinely non-critical side effects, not for this.

**ADR-02 · One public facade per module** ✅
- **Problem:** how many entry points should each module expose?
- **Decision:** exactly one — `CustomerManagement`, `BeerManagement`, `OrderManagement`
  — each the module's sole `api`-level facade interface.
- **Why:** avoids speculative interface-per-use-case splitting before there's a real reason to.
- **Cost:** a facade can grow large as a module's use cases accumulate.
- **Evolve later:** split into targeted interfaces only once a facade actually grows too large or
  serves genuinely distinct consumer groups (plan §7) — not preemptively.

**ADR-03 · Plain `Long` identifiers everywhere, not context-local ID types** 🔜
- **Problem:** should each module have its own `CustomerId`/`ProductId`/`OrderId` value type, even
  when referring to the same conceptual entity from another module's perspective?
- **Decision:** every module currently uses a plain `Long` for all identifiers.
- **Why:** keeps the showcase's domain types focused on richer invariants (see B) rather than
  spending ceremony on ID wrapping; simpler to read end-to-end.
- **Cost:** weaker type safety (a `Long` can be silently passed as the wrong kind of ID); doesn't
  demonstrate bounded-context ID isolation, which the target design considers a deliberately
  verbose but valuable teaching point.
- **Evolve later:** introduce per-context ID value objects (`order.domain.model.CustomerId`
  distinct from `customer.domain.model.CustomerId`, etc.) per plan §12.2.

**ADR-04 · `platform` is intentionally not a Spring Modulith module** ✅
- **Problem:** `platform` (security, REST, observability, OpenAPI/SOAP wiring) sits as a top-level
  package alongside the business modules — should Modulith treat it as one of them?
- **Decision:** no. `spring.modulith.detection-strategy=explicitly-annotated` means only packages
  carrying `@ApplicationModule` are ever considered modules, and `platform` deliberately carries
  none; its own two boundary invariants (no dependency back onto a business module; `openapi`/`ws`
  never accessed from outside) are instead plain ArchUnit rules in `PlatformBoundaryTest`.
- **Why:** `platform` is a cross-cutting technical foundation, not a bounded context — Modulith
  should only validate the actual business domains (`customer`/`product`/`order`/`shared`).
- **Cost:** two parallel boundary-checking mechanisms (Modulith `verify()` for business modules,
  ArchUnit for `platform`) instead of one.
- **Evolve later:** settled — no further action planned.

### B. Domain modeling & business rules

**ADR-05 · Rich, self-validating aggregates instead of anemic records** ✅
- **Problem:** where do invariants (non-blank name, non-null price, legal state transitions, …)
  live?
- **Decision:** `Customer`/`Beer`/`Order` are self-validating immutable records with intention-
  revealing factory/behavior methods (`Customer.register/.suspend/.activate`, `Beer.create`,
  `Order.place/.transitionTo/.cancel`); the `*Service` classes implementing each module's
  `*Management` facade are thin orchestrators with almost no business logic of their own.
- **Why:** invariants that live in the aggregate can never be forgotten by a caller; it also makes
  `Customers`/`Beers`/`Orders` easy to read as pure orchestration.
- **Cost:** any invariant has to be expressible in a compact constructor or a behavior method — no
  shortcuts via a service-layer `if` check.
- **Evolve later:** none planned; this is a standing constraint enforced by ArchUnit
  (`repositoriesShouldHaveOnlyAccessedByServices`, `adaptersShouldBeNamedAdapter`).

**ADR-06 · Order placement checks customer existence, not ordering eligibility** 🔜
- **Problem:** should `order` be allowed to place an order for a `SUSPENDED` customer?
- **Decision (current):** `Orders.placeOrder` only calls
  `customerLookup.assertCustomerExists(customerId)` — it does not consult `CustomerStatus` at all.
- **Why (as-is):** `customer.suspend()`/`.activate()` were added as use cases in their own right,
  ahead of wiring their effect into `order` — a staged rollout, not an oversight that was meant to
  ship this way permanently.
- **Cost:** a real behavioral gap — a suspended customer can currently place an order successfully
  today.
- **Evolve later:** the eligibility decision belongs to `customer`, not `order` (plan §9.2) — add a
  `customer.api.query` type such as `CheckOrderingEligibility` returning an eligibility flag plus a
  stable reason enum (not free text), and call it from `Orders.placeOrder` instead of
  `assertCustomerExists`.

**ADR-07 · `product` has no stock/inventory concept** 🔜
- **Problem:** can an order be placed for a beer that's out of stock?
- **Decision (current):** no — there is no stock/inventory field on `Beer`/`BeerJpaEntity` at all;
  order placement only checks that the referenced beer IDs exist.
- **Why (as-is):** keeps the current showcase scope on price/name snapshotting (see plan §11)
  without also having to solve concurrent stock allocation yet.
- **Cost:** an order can never fail due to unavailability, which is unrealistic for a webshop.
- **Evolve later:** add a stock field and a single atomic `allocateForOrder(...)` operation (load →
  check status → check stock → decrement → return a price/name snapshot, all in one call — never
  a separate lookup-then-reserve pair, which would race) per plan §10. Immediate allocation first;
  a true reservation lifecycle (`ProductReservation`, expiry, confirm/release) is a later,
  optional milestone (plan §10.4), not a prerequisite.

### C. Persistence & data ownership

**ADR-08 · Separate domain model and JPA entity in every module** ✅
- **Problem:** should the domain aggregate double as the `@Entity`?
- **Decision:** no — every module has both a `domain.model` record and a distinct
  `*JpaEntity`, mapped by a dedicated `*PersistenceMapper`.
- **Why:** keeps the domain free of JPA/Hibernate concerns (no lazy-loading proxies, no
  annotation coupling) and makes the persistence technology swappable in principle.
- **Cost:** mapper code and some field duplication between the two models; mapper round-trips
  need their own tests.
- **Evolve later:** none planned as a rule — this is a deliberate, standing showcase choice, not
  something every aggregate must justify individually. A genuinely trivial future aggregate could
  reasonably share one model instead, case by case.

**ADR-09 · No cross-module foreign keys** ✅
- **Problem:** `OrderJpaEntity.customerId` and `OrderLineJpaEntity.beerId` reference rows owned by
  other modules (`BEER_ORDER`/`BEER_ORDER_LINE` are order's own tables) — should the database
  enforce that with a real FK?
- **Decision:** no — both are plain columns with no FK relation to the `customer`/`product` tables;
  only the intra-aggregate FK (`OrderLineJpaEntity.order` → `BEER_ORDER`) exists.
- **Why:** referential validity across modules is enforced at the module-API boundary instead
  (`CustomerLookup`/`BeerLookup` at order-placement time), which keeps the database schema itself
  free of cross-module coupling.
- **Cost:** gives up database-level referential integrity between modules; a bug bypassing the
  module API could in principle write an order referencing a nonexistent customer/beer.
- **Evolve later:** none planned — this is a deliberate, standing trade-off (plan §18.2), not a gap.

**ADR-10 · No optimistic locking anywhere** 🔜
- **Problem:** two concurrent requests reading and then writing the same row (most relevantly, a
  future stock decrement) can silently lose an update.
- **Decision (current):** no `@Version` field exists on any `*JpaEntity`.
- **Why (as-is):** there is currently no mutable, concurrently-contended numeric field (stock
  doesn't exist yet — ADR-07) that would make the gap observable.
- **Cost:** once stock exists, concurrent allocation requests could over-allocate without a
  concurrency guard.
- **Evolve later:** add `@Version` to the product stock entity once ADR-07 lands; map
  `OptimisticLockingFailureException` → a stable business exception → HTTP 409, with a narrowly
  bounded retry (1–2 attempts, reload, only on lock conflict — never on validation/insufficient-
  stock failures). An atomic `UPDATE … WHERE available_quantity >= :qty` is worth demonstrating
  later as an explicit trade-off comparison, not a replacement (plan §16).

**ADR-11 · Migrations are versioned but not module-scoped** 🔜
- **Problem:** `database/changelog/` is organized by release version (`1.0.0/`, `1.1.0/`), not by
  owning module.
- **Decision (current):** all modules' changelogs share the same version-numbered folders.
- **Why (as-is):** simplest structure for a small, single-team showcase.
- **Cost:** minor — harder to see at a glance which module owns which migration as the schema
  grows.
- **Evolve later:** optionally reorganize into per-module folders (`db/migration/customer`,
  `.../product`, `.../order`, …) once migration ownership needs to be visible at a glance (plan
  §18.3); low priority.

### D. Events & asynchronous side effects

**ADR-12 · A single internal event, consumed only within its own module** 🔜
- **Problem:** what happens after an order is successfully placed?
- **Decision (current):** `order` publishes one event, `OrderPlaced`
  (`order/domain/event/`), consumed only by `OrderPlacedEventListener` inside `order`
  itself (via `@ApplicationModuleListener`), which just logs. `spring-modulith-starter-jdbc` is
  wired in and the `EVENT_PUBLICATION` registry table exists, but nothing exercises it in a way
  that demonstrates its reliability guarantees.
- **Why (as-is):** no other module currently needs to react to an order being placed, so a
  cross-module public event would have no real consumer yet — adding one speculatively would be
  exactly the kind of premature abstraction the rest of this codebase deliberately avoids.
- **Cost:** the event-publication registry infrastructure is present but has nothing to show for
  itself; the domain-event vs. public-application-event distinction isn't demonstrated at all.
- **Evolve later:** add a `notification` module (`notification :: api` dependency direction:
  `notification` depends on `order`, never the reverse) consuming a new, self-contained
  `order.api.OrderPlacedEvent` (order id, customer id, an email snapshot, line/money details — no
  REST/persistence types, unlike the internal `OrderPlaced`) via `@ApplicationModuleListener`.
  This is also where the JDBC event-publication registry would start pulling its weight: republish
  on failure, idempotent consumption, cleanup/retention (plan §24–26).

### E. Command handling & consistency

**ADR-13 · One transaction spans the whole `placeOrder` use case** ✅
- **Problem:** should customer/product lookups, order creation, and event publication be one
  atomic unit, or separate steps that could partially fail?
- **Decision:** `Orders.placeOrder` is a single `@Transactional` method covering the customer
  existence check, the beer lookups, `Order.place(...)`, the save, and the event publish.
- **Why:** this is a strongly-consistent modular monolith by design — a single JVM and database
  make local ACID transactions the natural fit, and there's no reason to give that up
  prematurely for a hypothetical future service split.
- **Cost:** if this application were later split into services, this transaction boundary would
  need a full redesign (distributed consistency, retries, compensation, idempotency, event
  delivery semantics) — the modular structure reduces that migration's blast radius but doesn't
  make it mechanical.
- **Evolve later:** none planned while this stays a single deployable.

**ADR-14 · No idempotent order placement** 🔜
- **Problem:** a client retry (e.g. after a timeout) can currently create a duplicate order.
- **Decision (current):** `PlaceOrder` has no idempotency key; nothing deduplicates retried
  requests.
- **Why (as-is):** the base flow (ADR-13) had to exist first; idempotency is naturally the next
  increment on top of it, not a parallel concern.
- **Cost:** a naive retry-on-timeout client can double-order.
- **Evolve later:** accept an `Idempotency-Key` header, persist `(customer_id, idempotency_key,
  request_hash, order_id, status)` under a `UNIQUE(customer_id, idempotency_key)` constraint; same
  key + same request replays the prior result, same key + different request is a 409 conflict
  (plan §27).

### F. API surface & error handling

**ADR-15 · Error handling split between a common technical handler and per-module business handlers** ✅
- **Problem:** where do exceptions get mapped to HTTP responses?
- **Decision:** `platform.rest.CommonRestExceptionHandler` handles only generic/cross-cutting cases
  (`IllegalArgumentException`, validation errors, `AuthorizationException`); each business module
  maps its own domain exceptions in its own `adapter.in.rest.*RestExceptionHandler`.
  `platform` never imports a business module's exception types.
- **Why:** keeps `platform` genuinely generic — it can't couple to business-specific exception
  types even if someone tried, since it doesn't depend on any business module.
- **Cost:** one exception handler per module instead of a single central switch statement.
- **Evolve later:** adopt RFC 9457 Problem Details responses (already tracked as an unchecked item
  in the feature checklist above) — this would replace `ErrorDetails` but not the split itself.

**ADR-16 · `product` has no inbound REST adapter** 🔜
- **Problem:** beers can only be created programmatically (e.g. by tests) via
  `BeerManagement.createBeer(...)` — there's no `/beers` endpoint.
- **Decision (current):** `product` has no `adapter/in` package at all.
- **Why (as-is):** no consumer has needed one yet; the same posture applies to `customer`'s already-
  implemented but unwired `suspendCustomer`/`activateCustomer` methods.
- **Cost:** the `product` module can't be exercised from outside the JVM at all today.
- **Evolve later:** add REST endpoints once there's a real need to; remember the contract-first
  flow — start in `beer-store-contract`, `mvn clean install` there, then implement the generated
  interface in `product`.

**ADR-17 · `OrderStatus` is fulfillment-oriented, not payment-oriented** ✅
- **Problem:** what does an order's lifecycle actually track?
- **Decision:** `NEW → PROCESSING → SHIPPED → DELIVERED / CANCELLED` — a physical-fulfillment state
  machine, encoded as legal-transition logic on the enum itself (`OrderStatus.canTransitionTo`).
- **Why:** matches this showcase's chosen scope (a beer store shipping physical goods) rather than
  a payment/checkout flow.
- **Cost:** none — noted here only because a payment-oriented flow (place/confirm/pay) is a
  reasonable alternative design also worth knowing about, not because this one is wrong.
- **Evolve later:** none planned; would only change if the showcase's scope shifted toward payment
  processing.

### G. Testing

**ADR-18 · Module boundaries enforced by Spring Modulith + a dedicated ArchUnit suite** ✅
- **Problem:** how are the architectural rules above actually kept true over time, instead of just
  documented?
- **Decision:** `ModularityTests` runs `ApplicationModules.of(BeerStoreApplication.class).verify()`
  for the business modules; `AdapterArchitectureTest`/`ApplicationArchitectureTest`/
  `DomainArchitectureTest`/`PlatformArchitectureTest`/`PlatformBoundaryTest`/`GeneralRulesTest`/
  `TestingRulesTest` cover everything Modulith doesn't (layering, naming, `platform`'s own
  boundaries, test-class conventions) — all run under `mvn test`.
- **Why:** naming/layering conventions that aren't enforced by a failing test tend to erode; this
  makes `mvn test` the single source of truth for "does the code still match the documented rules."
- **Cost:** a legitimate new pattern occasionally requires updating an ArchUnit rule alongside the
  code, not just the code.
- **Evolve later:** none planned — this is the standing mechanism, not itself a gap.

**ADR-19 · No module-level integration test** 🔜
- **Problem:** the ArchUnit suite verifies static structure, but nothing exercises `order` end-to-
  end as a Spring Modulith module in isolation (with its dependencies mocked/stubbed at the module
  boundary).
- **Decision (current):** no `@ApplicationModuleTest`-annotated test exists.
- **Why (as-is):** the existing `*IT` integration tests already exercise the full application
  context, which has covered this so far.
- **Cost:** less precise failure localization when a cross-module interaction regresses — a full
  `*IT` failure doesn't pinpoint the module boundary as clearly as a scoped module test would.
- **Evolve later:** add an `@ApplicationModuleTest` for `order` (plan §29.5), deciding explicitly
  whether it bootstraps `customer`/`product` directly or replaces them with test doubles at the
  facade.

### H. Deployment & infrastructure

**ADR-20 · No Kubernetes manifests; actuator shares the application's port** 🔜
- **Problem:** the app is currently only packaged and run as a single Docker image (see "Building &
  running a container image" below) — there's no Kubernetes deployment story, and Spring Boot
  Actuator's endpoints, including the health-check groups Kubernetes probes would use, are exposed
  on the same port (8080) as application traffic.
- **Decision (current):** no K8s manifests or Helm chart exist. `application.yml` already turns on
  the Kubernetes-shaped health groups (`management.health.livenessstate.enabled`/
  `.readinessstate.enabled`, `management.endpoint.health.probes.enabled`) — so `/actuator/health/liveness`
  and `/actuator/health/readiness` already work correctly today — but `management.server.port` is
  unset, so they're only reachable on 8080, the same port that serves REST/SOAP traffic.
- **Why (as-is):** Kubernetes was never the deployment target so far — CI publishes a single image
  to Docker Hub and stops there; no cluster to deploy manifests into has existed yet.
- **Cost:** deployed as-is to Kubernetes today, a slow or overloaded application (thread pool
  exhaustion, a long GC pause, a stuck downstream call) would make the liveness/readiness probes
  fail on the same connection pool as real traffic — the kubelet could then kill/restart a pod that
  isn't actually broken, just momentarily busy, adding restart churn on top of an already-degraded
  situation instead of shedding load.
- **Evolve later:** add a Kubernetes `Deployment`/`Service`/`ConfigMap` set (or a Helm chart) for
  repeatable deployment; set `management.server.port` to a separate port so actuator (health,
  probes, metrics) is reachable independently of application traffic, and point the `Deployment`'s
  `livenessProbe`/`readinessProbe` at `/actuator/health/liveness`/`/actuator/health/readiness` on
  that management port instead of 8080.

### Suggested sequencing

If picking these up, the highest-value order is: **(1)** the `notification` module + public
`OrderPlacedEvent` [ADR-12] — the biggest missing showcase piece, and it finally gives the already-
wired event-publication registry something to demonstrate; **(2)** product stock + `allocateForOrder`
+ optimistic locking [ADR-07, ADR-10] — the concurrency-handling showcase, currently entirely absent;
**(3)** wiring customer ordering-eligibility into `placeOrder` [ADR-06] — small, and it fixes a real
behavioral gap (a suspended customer can order today); **(4)** idempotent order placement [ADR-14].
The rest are lower-priority or already-settled decisions with no planned change.