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
- Spring Cloud Contract contracts (defined in `beer-store-contract/src/test/resources/contracts/`) generate two parallel test suites in `beer-store-application` at `generate-test-sources` time: `contract.explicit` (EXPLICIT mode, extends `ContractTestBase`, uses RestAssured against a random port) and `contract.mockmvc` (MOCKMVC mode, extends `ContractTestBaseMockMvc`). Both bases seed a `CustomerData` row read via `ContractDataReader` from the contract's JSON/yaml fixtures before running. The `generateTests` executions resolve the contract module's `-stubs` classifier via `contractsMode=LOCAL` — a top-level `spring-cloud-contract-maven-plugin` `<configuration>` element, a sibling of (not nested inside) `<contractDependency>` — since the default `CLASSPATH` mode became unreliable under Spring Cloud Contract 5.0.x's plugin-realm classloading.
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
| `command` | RegisterCustomer, UpdateCustomer, DeleteCustomer, SuspendCustomer, ActivateCustomer | CreateBeer | PlaceOrder, UpdateOrderStatus, CancelOrder |
| `query` | GetCustomer, FindCustomerByName | FindBeers | GetOrder |
| `view` | CustomerView | BeerView | OrderView (`customerId`/`beers` are plain `Long`/`List<Long>`, never other modules' view objects) |
| `type` | Address, CustomerStatus (ACTIVE/SUSPENDED) | BeerStyle | OrderStatus |
| `exception` | CustomerNotFoundException, IllegalCustomerStateException | *(none)* | OrderNotFoundException, IllegalOrderStateException, UnknownBeerException |
| `domain/model` | Customer | Beer (`price: Money`) | Order, OrderLine (`unitPriceSnapshot`/`beerNameSnapshot` — a price change on `Beer` never retroactively changes an existing order) |
| `domain/event` | — | — | OrderPlaced (internal only, no cross-module listener — see **ADR-12**) |
| `application/service` | Customers | Beers | Orders, OrderPlacedEventListener |
| `application/port/out` | CustomerRepository | BeerRepository | OrderRepository, CustomerLookup, BeerLookup, BeerSnapshot |
| `adapter/in` | rest: CustomerController, CustomerRestAdapter, CustomerMapper, CustomerRestExceptionHandler; soap: CustomerWs | *(none yet — see **ADR-16**)* | rest: OrderController, OrderRestAdapter, OrderMapper, OrderRestExceptionHandler |
| `adapter/out/persistence/jpa` (entities in `jpa/entity`) | CustomerJpaEntity, CustomerJpaRepository, CustomerPersistenceAdapter, CustomerPersistenceMapper | BeerJpaEntity, BeerJpaRepository, BeerPersistenceAdapter, BeerPersistenceMapper (`priceAmount: BigDecimal` ↔ `Money`) | OrderJpaEntity, OrderLineJpaEntity (`@OneToMany(mappedBy="order", cascade=ALL, orphanRemoval=true)`), OrderJpaRepository, OrderPersistenceAdapter, OrderPersistenceMapper |
| `adapter/out/<other module>` | — | — | `adapter/out/customer/CustomerLookupAdapter`, `adapter/out/product/BeerLookupAdapter` |

Notes:
- `customer.suspendCustomer`/`.activateCustomer` are full, tested `*Management` methods with no REST/SOAP wiring yet — same posture as `product`'s missing REST adapter (**ADR-16**); adding either requires extending `beer-store-contract` first.
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
| `*Controller` | `adapter.in.rest` | `@RestController`/`@Controller` |
| `*Adapter` (any non-`*Management` `@Service`) | `adapter.in.rest` | `@Service` |
| `*LookupAdapter` (ACL) | `adapter.out.<other module>` | `@Component` |
| `*PersistenceAdapter` | `adapter.out.persistence.jpa` | `@Repository` |
| `*JpaEntity` | `adapter.out.persistence.jpa.entity` | `@Entity` |
| `*Mapper` | `adapter.in.rest` or `adapter.out.persistence.jpa` | — |
| `*Config` | anywhere | `@Configuration` |
| logger | anywhere | `private static final log` (Lombok `@Slf4j`) |

Architecture test classes: `ModularityTests` (Modulith `verify()` + PlantUML docs), `DomainArchitectureTest`, `AdapterArchitectureTest`, `ApiArchitectureTest`, `ApplicationArchitectureTest`, `PlatformArchitectureTest`, `PlatformBoundaryTest`, `GeneralRulesTest`, `TestingRulesTest` (below).

### Testing conventions (enforced by `TestingRulesTest`)
- Any class annotated `@SpringBootTest`/`@DataJpaTest`/`@WebMvcTest` (non-abstract) must be named `*IT` so it runs under failsafe, not surefire.
- Non-`@DataJpaTest` `*IT` classes must extend `IntegrationTest` (`base/IntegrationTest.java`, activates the `test` profile / `BeerStoreApplication.PROFILE_TEST`, and imports `TestcontainersConfig` — see below).
- Use AssertJ (`org.assertj.core.api.Assertions`), never `org.junit.jupiter.api.Assertions`, in any class named `*Test`.
- Liquibase changelogs live under `beer-store-application/src/main/resources/database/changelog/`, referenced from `changelog-master.xml`. Requires the `spring-boot-starter-liquibase` dependency (not just `liquibase-core`) — Spring Boot 4 moved `LiquibaseAutoConfiguration` into its own module, so without the starter migrations silently never run and Hibernate schema validation fails.
- Database backend differs by context: local `spring-boot:run` uses H2 (`application.yml`, `ddl-auto: create`, Liquibase disabled). `*IT` classes extending `IntegrationTest` run against a real Postgres via Testcontainers (`base/TestcontainersConfig.java`, `@ServiceConnection PostgreSQLContainer`), with Liquibase enabled and `ddl-auto: validate` so migrations are actually exercised. The Spring Cloud Contract test bases (`ContractTestBase`, `ContractTestBaseMockMvc`) don't extend `IntegrationTest`, so they still run against the `test` profile's H2 datasource directly. Postgres is also the runtime database.
- Spring Boot 4 split `spring-boot-test-autoconfigure` per feature. `@AutoConfigureDataJpa`/`@AutoConfigureTestDatabase`/`@AutoConfigureTestEntityManager`/`TestEntityManager` now live under `org.springframework.boot.data.jpa.test.autoconfigure` / `org.springframework.boot.jdbc.test.autoconfigure` / `org.springframework.boot.jpa.test.autoconfigure`, `@AutoConfigureMockMvc` under `org.springframework.boot.webmvc.test.autoconfigure`, and `@AutoConfigureCache` under `org.springframework.boot.cache.test.autoconfigure` — each needs its own `spring-boot-starter-*-test` dependency (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-cache-test`) rather than coming transitively from `spring-boot-starter-test`.
