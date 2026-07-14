# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

For the *why* behind a design choice, see `ADR.md` (lightweight ADRs, tagged ✅ Implemented / 🔜 Gap).
This file is the operative reference: what exists, where it lives, and what rules keep it that way.

## Project

Beer Store is a Spring Boot proof-of-concept exploring Domain-Driven Design in a multi-module Maven build (Java 25, Spring Boot 4.1.0, `parent` groupId `dev.ronin.demo`). It exposes both REST and SOAP for the same domain and generates code from a shared API contract module.

Modules (in build order — `beer-store-contract` must be built first; the reactor resolves this order automatically from module dependencies even though the root `pom.xml`'s own `<modules>` list isn't sorted that way):
- **beer-store-contract** — the source of truth. Holds the OpenAPI spec (`beer-store-contract.yaml`), the SOAP XSD (`Customer.xsd`), and Spring Cloud Contract contracts (`src/test/resources/contracts/**`). Publishing this module also builds a `-stubs` jar consumed by other modules for contract/stub-runner tests.
- **beer-store-application** — the actual Spring Boot server. DDD-layered (see Architecture below). Generates REST server interfaces/models and SOAP JAXB classes from the contract module at build time.
- **beer-store-client** — a thin Spring Boot client app that consumes the same OpenAPI spec via the `webclient`/`java` generator, used to verify the contract from the consumer side (WireMock + stub-runner).

## Commands

**Local JDK note (this machine):** `JAVA_HOME` and `MAVEN_HOME` are already set correctly (JDK 25, IntelliJ-bundled Maven) — no need to override or search for either. Always invoke the `./mvnw` wrapper (present only at the repo root, not per-module), which self-downloads the pinned 3.9.16 distribution on first use regardless of `MAVEN_HOME`.

Always build the contract module first when working from a clean checkout or after changing the contract:
```
cd beer-store-contract && mvn clean install
```

Then, from the repo root:
- Build everything (skip tests): `mvn -Dmaven.test.skip=true clean install`
- Run unit + architecture tests: `mvn test`
- Run integration tests (`*IT` classes, via failsafe): `mvn verify`
- Run a single test class: `mvn -pl beer-store-application test -Dtest=OrdersTest`
- Run a single integration test: `mvn -pl beer-store-application verify -Dit.test=CustomersIT`
- Run the app locally: `mvnw spring-boot:run` (from `beer-store-application`, or root with `-pl`)
- Jacoco is wired in automatically via the `jacoco-maven-plugin` during the standard test/verify lifecycle.
- CI/CD (build, tests+coverage, SonarCloud, Buildpacks image build, Trivy scan, Allure/Trivy reports published to GitHub Pages, Docker Hub push) runs in GitHub Actions on every push/PR to `master` — see `.github/workflows/ci.yml` and the "CI/CD pipeline" section in `README.md`. Requires `SONAR_TOKEN`/`SONAR_ORGANIZATION`/`SONAR_PROJECT_KEY` and Docker Hub secrets in the repo, so it can't be reproduced 1:1 locally.

Endpoints when running locally (port 8080):
- Swagger UI: `http://localhost:8080/swagger-ui.html`, spec at `/v3/api-docs`
- SOAP: `http://localhost:8080/services/customerService.wsdl` (basic auth `user`/`password`)

## Architecture

### Contract-first code generation
Nothing in `beer-store-application`'s API surface is hand-written on the wire-format side:
- `beer-store-contract.yaml` → REST server interfaces/models generated into `dev.ronin.demo.beerstore.shared.api[.model]` via the `openapi-generator-maven-plugin` (`generate-producer` execution, `apiPackage`/`modelPackage` driven by the `package.shared` property). Controllers implement these generated interfaces. (`shared/api[.model]` is a `target/generated-sources` output, not a hand-written `src/main/java` package — it only exists after a build.)
- `Customer.xsd` → JAXB classes for the SOAP endpoint via `cxf-xjc-plugin`, generated into `dev.ronin.demo.beerstore.shared.contract.customerdata` (an explicit `packagename` xsdOption, since the XSD's own target namespace would otherwise drive the package name).
- Spring Cloud Contract contracts (defined in `beer-store-contract/src/test/resources/contracts/`, one folder per resource — `customer/`, `catalog/`) generate two parallel `*IT` test suites in `beer-store-application` at `generate-test-sources` time (`nameSuffixForTests=IT`, so they run under failsafe on Testcontainers Postgres, not surefire): `contract.explicit` (EXPLICIT mode, extends `ContractTestBase`, uses RestAssured against a random port — covers both `customer` and `catalog` contracts) and `contract.mockmvc` (MOCKMVC mode, extends `ContractTestBaseMockMvc`, scoped to `customer` only via `includedContracts` since that base only wires the customer controller — kept purely as a showcase that both generation modes work, per `TESTING_STRATEGY.md` §6). Both bases seed test rows read via `ContractDataReader` from the contracts' own JSON fixtures before running (`ContractTestBase` additionally seeds beers for the catalog contract). The `generateTests` executions resolve the contract module's `-stubs` classifier via `contractsMode=LOCAL` — a top-level `spring-cloud-contract-maven-plugin` `<configuration>` element, a sibling of (not nested inside) `<contractDependency>` — since the default `CLASSPATH` mode became unreliable under Spring Cloud Contract 5.0.x's plugin-realm classloading.
- `beer-store-client` generates a WebClient-based Java client from the *same* `beer-store-contract.yaml`, and verifies it against WireMock stubs published from the contract module's `-stubs` classifier.

Because of this pipeline, changes to the API almost always start in `beer-store-contract`, followed by `mvn clean install` in that module before the dependent modules will pick up regenerated sources.

### Spring Modulith module layout in beer-store-application
Package layout under `dev.ronin.demo.beerstore` is **module-first**, not layer-first, and each module explicitly separates its public contract from its implementation the way the Spring Modulith reference docs recommend:

```
<module>/
├── package-info.java        @ApplicationModule(allowedDependencies = {...})
├── api/                     @NamedInterface("api") — the module's *only* public surface
│   ├── ...Management.java    the facade interface(s) - the only type(s) at the api root
│   ├── command/               write-intent DTOs (no *Command suffix - the package says it)
│   ├── query/                 read-intent DTOs (no *Query suffix - the package says it)
│   ├── view/                  read-model DTOs, keep the *View suffix (e.g. CustomerView)
│   ├── type/                  simple value types shared by DTOs and the domain model
│   └── exception/              domain exceptions exposed on the api
├── domain/                  hidden by Spring Modulith's default rule (module subpackages are
│   ├── model/                internal unless carrying @NamedInterface, and only api/ does) —
│   └── event/                 never imported from another module; domain events with no
│                               cross-module listener live in event/ (e.g. OrderPlaced)
├── application/
│   ├── service/               the @Service implementing the module's *Management port
│   └── port/out/              outbound ports (*Repository, and order's own *Lookup ACL ports)
└── adapter/
    ├── in/rest/ (and /soap/ for customer)   inbound adapters
    └── out/persistence/jpa/                 outbound adapters (repository, adapter, mapper)
        └── entity/                            the *JpaEntity classes, one level deeper
        (and out/<other module>/ for order's ACL adapters)
```

`ModularityTests` (`architecture/ModularityTests.java`) calls `ApplicationModules.of(BeerStoreApplication.class).verify()` to enforce this — the primary boundary check now; it also writes PlantUML module diagrams to `target/spring-modulith-docs`. `platform` never becomes a Spring Modulith module in the first place (`spring.modulith.detection-strategy=explicitly-annotated` in `application.yml` means only `@ApplicationModule`-annotated packages count as modules at all, and `platform` deliberately carries none) — see **ADR-04** for the full rationale. The two boundary invariants Modulith would otherwise enforce for `platform` are instead plain ArchUnit rules in `PlatformBoundaryTest` (below).

**Why `api` holds DTOs, not the domain model directly:** the `*Management` facades work exclusively with command/query/view records, never with the internal `domain.model` aggregate — Spring Modulith verifies that any type appearing in an exposed interface's signature is itself exposed, so the real aggregate stays hidden only because a dedicated DTO layer stands in front of it. Translation is hand-written in each `application.service` class (no separate mapper for this step). Simple value types with no aggregate behavior to hide (`Address`, `OrderStatus`, `BeerStyle`, `CustomerStatus`) live in `api.type`, reused by both DTOs and the domain model. `ApiArchitectureTest` enforces the `view`/`exception`/`type` placement (see cheat-sheet below) — it does **not** enforce a `command`/`query` placement rule, nor does it restrict the api root to `*Management` types specifically (it only requires the root hold interfaces; the `*Management` naming/location rule itself lives in `DomainArchitectureTest`).

**Rich domain aggregates, not anemic transaction scripts** (**ADR-05**): `Customer`/`Beer`/`Order` are self-validating immutable records with intention-revealing factory/behavior methods (`Customer.register/.updateProfile/.suspend/.activate`, `Beer.create`, `Order.place/.transitionTo/.cancel/.totalAmount`) — invariants live in compact constructors, never in the calling service. `Order.transitionTo` delegates to `OrderStatus.canTransitionTo`; `Customer.suspend()`/`.activate()` throw `IllegalCustomerStateException` on a repeated no-op transition. `Customers`/`Beers`/`Orders` (the `@Service` implementing each `*Management` port) are thin orchestrators as a result — enforced by ArchUnit: `repositoriesShouldHaveOnlyAccessedByServices` (only a `@Service` may call a `*Repository` port) and `adaptersShouldBeNamedAdapter` (any non-`*Management` `@Service` must be named `*Adapter`).

#### Module reference

| | `customer` | `product` | `order` |
|---|---|---|---|
| `allowedDependencies` | `{"shared"}` | `{"shared"}` | `{"customer :: api", "product :: api", "shared"}` |
| api root | `CustomerManagement` | `BeerManagement` | `OrderManagement` |
| `command` | RegisterCustomer, UpdateCustomer, DeleteCustomer, SuspendCustomer, ActivateCustomer | CreateBeer (+`availability`, defaults `IN_STOCK` via a convenience overload) | PlaceOrder, UpdateOrderStatus, CancelOrder |
| `query` | GetCustomer, FindCustomerByName | FindBeers, GetBeer, BrowseCatalog (filter/sort criteria — own enums/primitives/`BigDecimal` only, no Spring Data/JPA types, see **ADR-21**) | GetOrder |
| `view` | CustomerView | BeerView (+`availability`) | OrderView (`customerId`/`beers` are plain `Long`/`List<Long>`, never other modules' view objects) |
| `type` | Address, CustomerStatus (ACTIVE/SUSPENDED) | BeerStyle, BeerAvailability (IN_STOCK/LOW_STOCK/OUT_OF_STOCK/DISCONTINUED/COMING_SOON — display-only, see **ADR-07**), BeerSortField, SortDirection | OrderStatus |
| `exception` | CustomerNotFoundException, IllegalCustomerStateException | BeerNotFoundException | OrderNotFoundException, IllegalOrderStateException, UnknownBeerException |
| `domain/model` | Customer | Beer (`price: Money`, `availability: BeerAvailability`) | Order, OrderLine (`unitPriceSnapshot`/`beerNameSnapshot` — a price change on `Beer` never retroactively changes an existing order) |
| `domain/event` | — | — | OrderPlaced (internal only, no cross-module listener — see **ADR-12**) |
| `application/service` | Customers | Beers | Orders, OrderPlacedEventListener |
| `application/port/out` | CustomerRepository | BeerRepository (`findMatching(BrowseCatalog)`, not a Spring Data query method - see **ADR-21**) | OrderRepository, CustomerLookup, BeerLookup, BeerSnapshot |
| `adapter/in` | rest: CustomerController, CustomerRestAdapter, CustomerMapper, CustomerRestExceptionHandler; soap: CustomerWs | rest: CatalogController, CatalogRestAdapter, CatalogMapper, CatalogRestExceptionHandler (`GET /catalog` browse+filter+sort, `GET /catalog/{id}` — see **ADR-16**) | rest: OrderController, OrderRestAdapter, OrderMapper, OrderRestExceptionHandler |
| `adapter/out/persistence/jpa` (entities in `jpa/entity`) | CustomerJpaEntity, CustomerJpaRepository, CustomerPersistenceAdapter, CustomerPersistenceMapper | BeerJpaEntity (+`availability`), BeerJpaRepository (also `JpaSpecificationExecutor<BeerJpaEntity>`), BeerSpecifications (`BrowseCatalog` → `Specification`/`Sort`, package-private, see **ADR-21**), BeerPersistenceAdapter, BeerPersistenceMapper (`priceAmount: BigDecimal` ↔ `Money`) | OrderJpaEntity, OrderLineJpaEntity (`@OneToMany(mappedBy="order", cascade=ALL, orphanRemoval=true)`), OrderJpaRepository, OrderPersistenceAdapter, OrderPersistenceMapper |
| `adapter/out/<other module>` | — | — | `adapter/out/customer/CustomerLookupAdapter`, `adapter/out/product/BeerLookupAdapter` |

Notes:
- `customer.suspendCustomer`/`.activateCustomer` are full, tested `*Management` methods with no REST/SOAP wiring yet — `product.createBeer` is in the same spot (still programmatic-only; `/catalog` is read-only, see **ADR-16**); adding any of these requires extending `beer-store-contract` first.
- `/catalog`'s filter/sort criteria travels as the plain `BrowseCatalog` DTO through `api`/`application`/`domain`; only `product.adapter.out.persistence.jpa.BeerSpecifications` (package-private) translates it into a JPA `Specification`/`Sort` — required by `DomainArchitectureTest.domainAndApplicationShouldNotDependOnPersistenceTechnology`, which forbids Spring Data/JPA types outside the persistence adapter. See **ADR-21**.
- `order` never stores/references another module's domain type; since it depends on *two* modules, it never injects `CustomerManagement`/`BeerManagement` directly but goes through its own ACL ports (`CustomerLookup`, `BeerLookup`) and their package-private adapters — see **ADR-01**. `OrderLine`'s aggregate expands a repeated beer id back into a flat list (`Order.beerIds()`), so `OrderView.beers` never had to change even though an id can legitimately repeat.
- No cross-module foreign keys (**ADR-09**): `OrderJpaEntity.customerId`/`OrderLineJpaEntity.beerId` are plain columns, not FKs — the old `@ElementCollection List<Long> beers`/`beer_order_beers` table this replaced had a latent bug (a unique constraint on `beer_id` alone meant a beer could only ever appear in one order system-wide).
- `OrderPersistenceMapper` can't use plain MapStruct constructor mapping for the domain side (`Order`'s compact constructor rejects an intermediate "no lines yet" instance) — only the flat entity fields are MapStruct-generated; lines and the back-reference are wired by hand.
- `OrderPlaced` is published as a decoupled follow-up, consumed only within `order` via `OrderPlacedEventListener`/`@ApplicationModuleListener` — a listener in `customer`/`product` would create a dependency back onto `order`, which their `allowedDependencies` forbid. Demonstrates the Spring Modulith JDBC event publication registry (`spring-modulith-starter-jdbc`; the `EVENT_PUBLICATION` table is auto-created via `spring.modulith.events.jdbc.schema-initialization.enabled`, which defaults to `true` and is left unset). See **ADR-12** for what this doesn't yet demonstrate.

#### `shared` and `platform`

- `shared/` — `@ApplicationModule(type = OPEN)`: any module may depend on it. Holds the generated wire-format code (`shared/api[.model]`, `shared/contract/customerdata` — see Contract-first code generation above), the hand-written `shared/rest/HomeController`, and `shared/kernel/Money` (single-currency `BigDecimal` wrapper, no `CurrencyCode`), reused by `product`/`order` — it lives here rather than in `product.api.type` because `order` never imports another module's `api` types directly (**ADR-01**).
- `platform/` — a plain package, **not** a Spring Modulith module (**ADR-04**). Subpackages business modules use from their inbound adapters: `security` (`Authorized`/`AuthorizedAspect`/`AuthorizationException` — AOP method authorization via `spring-boot-starter-aspectj` — plus `WebSecurityConfig`/`TestSecurityConfig`), `rest` (`ErrorDetails` + `CommonRestExceptionHandler`, handling only generic/cross-cutting cases — `NoSuchElementException`, `IllegalArgumentException`, `AuthorizationException`, Spring's validation errors — never a business module's domain exceptions, see **ADR-15**) and `observability` (`PropertiesLogger`, `RequestResponseLoggingFilter`, `RestLoggingConfig`). `openapi` (`OpenApiConfig`) and `ws` (`WebServiceConfig`, SOAP wiring) are never reached from outside `platform`. Both boundary invariants are ArchUnit rules in `PlatformBoundaryTest` (`platformShouldNotDependOnBusinessModules`, `platformInternalsShouldNotBeAccessedFromOutside`), not Modulith's `verify()`. Because `platform` can't see business exceptions, each business module maps its own domain exceptions in its own `adapter.in.rest.*RestExceptionHandler`.

### Naming & convention cheat-sheet

All ArchUnit-enforced (suite lives in `architecture/`) — new code violating these fails `mvn test`.

| Type | Package | Constraint |
|---|---|---|
| `*Management` | `<module>.api` (root only) | interface (`DomainArchitectureTest`) |
| `*View` | `<module>.api.view` | (`ApiArchitectureTest`) |
| domain exception | `<module>.api.exception` | assignable to `RuntimeException` (`ApiArchitectureTest`) |
| value enum | `<module>.api.type` | enum (`ApiArchitectureTest`) — `command`/`query` have **no** dedicated placement rule |
| aggregate service (`Customers`/`Beers`/`Orders`) | `application.service` | `@Service`, implements a `*Management` port |
| `*Repository` port | `application.port.out` | `@Repository`; only a `@Service` may call it |
| `*Controller` | `adapter.in.rest` (+ `shared.rest` for the one hand-written `HomeController`) | `@RestController`/`@Controller` |
| `*Adapter` (any non-`*Management` `@Service`) | `adapter.in.rest` | `@Service` |
| `*LookupAdapter` (ACL) | `adapter.out.<other module>` | `@Component` |
| `*PersistenceAdapter` | `adapter.out.persistence.jpa` | `@Repository` |
| `*JpaEntity` | `adapter.out.persistence.jpa.entity` | `@Entity` |
| `*Mapper` | `adapter.in.rest` or `adapter.out.persistence.jpa` | — |
| `*Config` | anywhere under `platform` (rule is scoped to that package only, via `PlatformArchitectureTest`) | `@Configuration` |
| logger | anywhere | `private static final log` (Lombok `@Slf4j`) |
| domain layer | `<module>.domain..` | never depends on `<module>.application..`/`<module>.adapter..` (`LayeredArchitectureTest`) |
| application layer | `<module>.application..` | never depends on `<module>.adapter..` (`LayeredArchitectureTest`) |
| general coding | anywhere in `src/main` | no `System.out`/`err`, no throwing generic `Exception`/`RuntimeException`, no `java.util.logging`, no `@Autowired`-annotated field (constructor injection only) — `GeneralRulesTest`, via ArchUnit's `GeneralCodingRules` plus one custom `noFields()` rule |

Architecture test classes: `ModularityTests` (Modulith `verify()` + PlantUML docs), `DomainArchitectureTest`, `AdapterArchitectureTest`, `ApiArchitectureTest`, `ApplicationArchitectureTest` (the driving side of each module depends on the `*Management` port, never the concrete aggregate service directly; that concrete service itself must reside in `application.service`), `LayeredArchitectureTest` (the onion-layering rules above — Modulith's `verify()` only guards boundaries *between* modules, not layering *within* one), `PlatformArchitectureTest`, `PlatformBoundaryTest`, `GeneralRulesTest`, `TestingRulesTest` (below). A predicate or package-name constant reused by more than one of these classes (e.g. "a concrete `@Service` implementing a `*Management` port") is defined once in the package-private `ArchitectureSupport` helper rather than copied per class; a value used by only one class stays local to it.

ArchUnit's `archRule.failOnEmptyShould` is left at its default (`true`): a rule matching zero classes fails the build rather than silently passing, so a typo'd package/name pattern can't quietly stop checking anything. The one deliberate exception is `TestingRulesTest.springBootTestShouldBeNamedIT`, marked `.allowEmptyShould(true)` — today only the exempted `contract` base classes (see below) are directly `@SpringBootTest`-annotated outside `IntegrationTest` itself, so the rule has no current matches but stays in place to catch future misuse. Relatedly, `repositoriesShouldBeAnnotatedWithRepository` excludes `*JpaRepository` by name — the Spring Data repositories (`CustomerJpaRepository` etc.) also end in "Repository" but are intentionally not `@Repository`-annotated themselves.

### Testing strategy

`TESTING_STRATEGY.md` (repo root) is the operative testing strategy — **read it before writing any
test.** It prescribes which level owns a behavior (honeycomb: thin domain-unit band, a dominant
middle band of `@ApplicationModuleTest` module tests, full `@SpringBootTest` only in the designated
saga/e2e and security packages, Spring Cloud Contract at the published REST seams), the hard build
mapping (surefire = no Spring context; everything Spring-context is `*IT` under failsafe on
Testcontainers Postgres; no H2 under `src/test`), the mock rules (only a module's own ACL port may
be doubled), the mandatory Allure taxonomy, and the forbidden anti-patterns. **ADR-22** is the
decision record. The conventions below are the mechanically enforced subset, not a substitute for
that document.

### Testing conventions (enforced by `TestingRulesTest`)
- `TestingRulesTest` is the one architecture test that deliberately analyzes the test tree itself (its `@AnalyzeClasses` omits `ImportOption.DoNotIncludeTests`, unlike every other class in `architecture/`).
- Any class annotated `@SpringBootTest`/`@DataJpaTest`/`@WebMvcTest` (non-abstract) must be named `*IT` so it runs under failsafe, not surefire. Exempt: `contract`'s own `ContractTestBase`/`ContractTestBaseMockMvc` — hand-written, non-abstract `@SpringBootTest` base classes for the generated `contract.explicit`/`contract.mockmvc` suites, never run directly themselves (the generated suites they back, e.g. `CustomerIT`/`CatalogIT`, *are* named `*IT` — set via `nameSuffixForTests` on the `spring-cloud-contract-maven-plugin` — and both bases extend `IntegrationTest` so `springBootTestShouldExtendBaseClass` holds for them too).
- Non-`@DataJpaTest` `*IT` classes must extend `IntegrationTest` (`base/IntegrationTest.java`, activates the `test` profile / `BeerStoreApplication.PROFILE_TEST`, and imports `TestcontainersConfig` — see below).
- Use AssertJ (`org.assertj.core.api.Assertions`), never `org.junit.jupiter.api.Assertions`, in any class named `*Test`. Exempt: the `architecture` package itself (every class in it ends in `Test`, and `TestingRulesTest` has to reference `Assertions.class` to define the rule that checks everyone else) — this is meta code, not a test suite for business logic.
- Liquibase changelogs live under `beer-store-application/src/main/resources/database/changelog/`, referenced from `changelog-master.xml`. Requires the `spring-boot-starter-liquibase` dependency (not just `liquibase-core`) — Spring Boot 4 moved `LiquibaseAutoConfiguration` into its own module, so without the starter migrations silently never run and Hibernate schema validation fails.
- **While the app is under active DDD/architecture transformation, do not create new per-release changelog folders (`1.1.0`, `1.2.0`, ...).** Keep every changeset in the single `1.0.0` folder and edit files there in place — schema history doesn't need to be preserved yet since no environment carries a persistent database (local `spring-boot:run` uses H2 with `ddl-auto: create`, Liquibase disabled; `*IT` tests spin up a fresh Testcontainers Postgres every run). Revisit this once the app reaches a stable release and real deployments start depending on replaying past migrations.
- Database backend differs by context: local `spring-boot:run` uses H2 (`application.yml`, `ddl-auto: create`, Liquibase disabled). `*IT` classes extending `IntegrationTest` run against a real Postgres via Testcontainers (`base/TestcontainersConfig.java`, `@ServiceConnection PostgreSQLContainer`), with Liquibase enabled and `ddl-auto: validate` so migrations are actually exercised. The Spring Cloud Contract test bases (`ContractTestBase`, `ContractTestBaseMockMvc`) also extend `IntegrationTest`, so the generated `contract.explicit`/`contract.mockmvc` suites (`CustomerIT`, `CatalogIT`, ...) run under failsafe against the same Testcontainers Postgres, not H2 — H2 remains only for local `spring-boot:run`. Both bases seed test data via `TRUNCATE TABLE ... RESTART IDENTITY CASCADE` (Postgres syntax) rather than H2's `DELETE`/`ALTER COLUMN ... RESTART WITH`, and seed outside any `@Transactional` wrapper since the seeded rows must be committed before the contract test's own HTTP call/MockMvc dispatch reads them. Postgres is also the runtime database.
- Spring Boot 4 split `spring-boot-test-autoconfigure` per feature. `@AutoConfigureDataJpa`/`@AutoConfigureTestDatabase`/`@AutoConfigureTestEntityManager`/`TestEntityManager` now live under `org.springframework.boot.data.jpa.test.autoconfigure` / `org.springframework.boot.jdbc.test.autoconfigure` / `org.springframework.boot.jpa.test.autoconfigure`, `@AutoConfigureMockMvc` under `org.springframework.boot.webmvc.test.autoconfigure`, and `@AutoConfigureCache` under `org.springframework.boot.cache.test.autoconfigure` — each needs its own `spring-boot-starter-*-test` dependency (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-cache-test`) rather than coming transitively from `spring-boot-starter-test`.
