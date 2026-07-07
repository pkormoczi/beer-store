# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Beer Store is a Spring Boot proof-of-concept exploring Domain-Driven Design in a multi-module Maven build (Java 25, Spring Boot 4.1.0, `parent` groupId `dev.ronin.demo`). It exposes both REST and SOAP for the same domain and generates code from a shared API contract module.

Modules (in build order — `beer-store-contract` must be built first):
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
- `beer-store-contract.yaml` → REST server interfaces/models generated into `dev.ronin.demo.beerstore.shared.api[.model]` via the `openapi-generator-maven-plugin` (`generate-producer` execution, `apiPackage`/`modelPackage` driven by the `package.shared` property). Controllers implement these generated interfaces.
- `Customer.xsd` → JAXB classes for the SOAP endpoint via `cxf-xjc-plugin`, generated into `dev.ronin.demo.beerstore.shared.contract.customerdata` (an explicit `packagename` xsdOption, since the XSD's own target namespace would otherwise drive the package name).
- Spring Cloud Contract contracts (defined in `beer-store-contract/src/test/resources/contracts/`) generate two parallel test suites in `beer-store-application` at `generate-test-sources` time: `contract.explicit` (EXPLICIT mode, extends `ContractTestBase`, uses RestAssured against a random port) and `contract.mockmvc` (MOCKMVC mode, extends `ContractTestBaseMockMvc`). Both bases seed a `CustomerData` row read via `ContractDataReader` from the contract's JSON/yaml fixtures before running. The `generateTests` executions resolve the contract module's `-stubs` classifier via `contractsMode=LOCAL` (Aether-based, set directly on `contractDependency`) — the default `CLASSPATH` mode became unreliable under Spring Cloud Contract 5.0.x's plugin-realm classloading.
- `beer-store-client` generates a WebClient-based Java client from the *same* `beer-store-contract.yaml`, and verifies it against WireMock stubs published from the contract module's `-stubs` classifier.

Because of this pipeline, changes to the API almost always start in `beer-store-contract`, followed by `mvn clean install` in that module before the dependent modules will pick up regenerated sources.

### Spring Modulith module layout in beer-store-application
Package layout under `dev.ronin.demo.beerstore` is **module-first**, not layer-first, and each module explicitly separates its public contract from its implementation the way the Spring Modulith reference docs recommend:

```
<module>/
├── package-info.java        @ApplicationModule(allowedDependencies = {...})
├── api/                     @NamedInterface("api") — the module's *only* public surface
│   └── ...Command/Query/View DTOs, the *UseCase facade, shared value types, exceptions
└── internal/                everything else - never imported from another module
    ├── domain/
    │   ├── model/            the real aggregate (e.g. Customer, Order, Beer)
    │   └── event/             domain events with no cross-module listener (e.g. OrderPlaced)
    ├── application/
    │   ├── service/           the @Service implementing the module's *UseCase
    │   └── port/out/          outbound ports (*Repository, and order's own *Lookup ACL ports)
    └── adapter/
        ├── in/rest/ (and /soap/ for customer)   inbound adapters
        └── out/persistence/ (and /<other module>/ for order's ACL adapters)   outbound adapters
```

`ModularityTests` (`architecture/ModularityTests.java`) calls `ApplicationModules.of(BeerStoreApplication.class, ...).verify()` to enforce this — the primary boundary check now; it also writes PlantUML module diagrams to `target/spring-modulith-docs`.

**Why `api` holds DTOs, not the domain model directly:** `ManageCustomersUseCase`/`ManageOrdersUseCase`/`ManageBeersUseCase` work exclusively with Command/Query/Result/View records (`RegisterCustomerCommand`, `GetCustomerQuery`, `CustomerView`, `PlaceOrderCommand`, `OrderView`, `CreateBeerCommand`, `BeerView`, ...), never with `internal.domain.model.Customer`/`Order`/`Beer` directly — Spring Modulith verifies that any type appearing in an exposed interface's signature is itself exposed, so the real aggregate can stay `internal` only because a dedicated View/Command layer stands in front of it. The translation is hand-written in the `internal.application.service` classes (`Customers`/`Orders`/`Beers`, e.g. `Customers.registerCustomer` builds the internal `Customer`, saves it, then maps it to a `CustomerView`) — no separate mapper class for this step. Simple value types with no aggregate-style behavior to hide (`Address`, `OrderStatus`, `BeerStyle`) stay directly in `api`, reused by both the DTOs and the internal domain model, rather than being duplicated as View types.

- `customer/` — `@ApplicationModule(allowedDependencies = {"shared", "platform :: rest", "platform :: security"})` — **depends on no other business module**. `api/`: `ManageCustomersUseCase`, `RegisterCustomerCommand`, `UpdateCustomerCommand`, `GetCustomerQuery`, `FindCustomerByNameQuery`, `DeleteCustomerCommand`, `CustomerView`, `Address`, `CustomerNotFoundException`. `internal/domain/model/Customer.java`; `internal/application/service/Customers.java` + `internal/application/port/out/CustomerRepository.java`; `internal/adapter/in/rest/{CustomerController,CustomersAdapter,CustomerMapper,CustomerRestExceptionHandler}.java` + `internal/adapter/in/soap/CustomerWs.java`; `internal/adapter/out/persistence/{CustomerJpaEntity,CustomerJpaRepository,CustomerPersistenceAdapter,CustomerPersistenceMapper}.java` (flat columns, no nested `@Embeddable`).
- `catalog/` — the beer domain; `@ApplicationModule(allowedDependencies = {"shared"})`, same independence as `customer` — no inbound REST adapter yet, so it needs neither `platform :: rest` nor `platform :: security`. Named `catalog` rather than `beer` deliberately — the module is the capability, `Beer` is the aggregate inside it (mirrors how a generic webshop names the module `catalog` while keeping `Product` as the aggregate name). `api/`: `ManageBeersUseCase`, `CreateBeerCommand`, `FindBeersQuery`, `BeerView`, `BeerStyle`. `internal/domain/model/Beer.java`; `internal/application/service/Beers.java` + `port/out/BeerRepository.java`; `internal/adapter/out/persistence/{BeerJpaEntity,BeerJpaRepository,BeerPersistenceAdapter,BeerPersistenceMapper}.java`. No `adapter/in` — no REST endpoint yet, beers are only created programmatically (e.g. by tests) via `ManageBeersUseCase.createBeer(...)`.
- `order/` — `@ApplicationModule(allowedDependencies = {"customer :: api", "catalog :: api", "shared", "platform :: rest", "platform :: security"})`. `api/`: `ManageOrdersUseCase`, `PlaceOrderCommand`, `GetOrderQuery`, `UpdateOrderStatusCommand`, `CancelOrderCommand`, `OrderView` (`customerId`/`beers` are plain `Long`/`List<Long>`, never `customer.api.CustomerView`/`catalog.api.BeerView` objects), `OrderStatus`, and the order exceptions. `internal/domain/model/Order.java`; `internal/domain/event/OrderPlaced.java` — a genuinely *internal* domain event (nothing outside `order` ever needs it, unlike a "public application event" that would live in `api`); `internal/application/service/{Orders,OrderPlacedEventListener}.java` + `internal/application/port/out/{OrderRepository,CustomerLookup,BeerLookup}.java`; `internal/adapter/in/rest/{OrderController,OrdersAdapter,OrderMapper,OrderRestExceptionHandler}.java`; `internal/adapter/out/persistence/{OrderJpaEntity,OrderJpaRepository,OrderPersistenceAdapter,OrderPersistenceMapper}.java` (`OrderJpaEntity.beers` is an `@ElementCollection` of plain `Long`s against the `beer_order_beers` table, not a `@OneToMany`/`@JoinTable` to another entity); `internal/adapter/out/customer/CustomerLookupAdapter.java` and `internal/adapter/out/catalog/BeerLookupAdapter.java` — see below.
- `shared/` — `@ApplicationModule(type = OPEN)`: any module may depend on it. Holds only the openapi-generator/cxf-xjc output (`shared/api[.model]`, `shared/contract/customerdata`, see Contract-first code generation above) plus the hand-written `shared/rest/HomeController`.
- `platform/` — `@ApplicationModule(allowedDependencies = {"shared"})`. Unlike the other cross-cutting module (`shared`, which is `OPEN`), `platform` is a regular, **closed** module that itself depends on nothing but `shared` — and, critically, never depends back on a business module. It exposes three `@NamedInterface`s that business modules declare in their own `allowedDependencies`: `security` (`Authorized`/`AuthorizedAspect`/`AuthorizationException`, AOP-based method authorization via `spring-boot-starter-aspectj`, plus `WebSecurityConfig`/`TestSecurityConfig`), `rest` (`ErrorDetails` + `CommonRestExceptionHandler`, which only handles the generic/cross-cutting cases — `NoSuchElementException`, `IllegalArgumentException`, `AuthorizationException`, Spring's own validation errors — and deliberately never imports a business module's domain exception types) and `observability` (`PropertiesLogger`, `RequestResponseLoggingFilter`, `RestLoggingConfig`). `openapi/` (`OpenApiConfig`) and `ws/` (`WebServiceConfig`, the SOAP endpoint wiring) stay unexposed since nothing outside `platform` needs them directly. Because `platform` can't see business exceptions, each business module maps its *own* domain exceptions to HTTP responses in its own `internal.adapter.in.rest` package — see `CustomerRestExceptionHandler`/`OrderRestExceptionHandler` above.

**No cross-module object embedding, and an anti-corruption layer for order's two dependencies:** `order` never stores or references another module's domain type — `OrderView`/`Order` only ever hold plain ids (`customerId: Long`, `beers: List<Long>`). Since `order` depends on *two* other modules (`customer` and `catalog`), `Orders` doesn't inject `ManageCustomersUseCase`/`ManageBeersUseCase` directly; it depends on two ports it owns itself — `internal.application.port.out.CustomerLookup` (`assertCustomerExists(Long)`, deliberately returning nothing - the caller already knows the id) and `BeerLookup` (`findExistingIds(List<Long>): List<Long>`, deliberately not returning `BeerView`) — each implemented by a package-private `@Component` adapter in its own `internal.adapter.out.<other module>` subpackage (`CustomerLookupAdapter`, `BeerLookupAdapter`). This way a change to either module's API only ripples into its adapter, and neither `customer.api.CustomerView` nor `catalog.api.BeerView` needs to be imported anywhere else in `order`. `OrderPlaced` is published as a decoupled follow-up (not part of the synchronous validation), consumed only within `order` itself via `OrderPlacedEventListener`/`@ApplicationModuleListener` (a listener in `customer`/`catalog` would create a dependency back onto `order`, which their `allowedDependencies` forbid) — demonstrating the Spring Modulith JDBC event publication registry (`spring-modulith-starter-jdbc`; the `EVENT_PUBLICATION` table is auto-created on startup via `spring.modulith.events.jdbc.schema-initialization.enabled`, which defaults to `true`, independent of Hibernate's own `ddl-auto`).

These naming/placement conventions (`*Repository` ports in `internal.application.port.out` and `@Repository`-annotated, `*Controller` in `internal.adapter.in.rest` and `@RestController`/`@Controller`-annotated, `*Adapter` in `internal.adapter.in.rest` and `@Service`-annotated — except the concrete aggregate services like `Customers`/`Orders`/`Beers`, which are `@Service` but implement a `*UseCase` port instead of being named `*Adapter`, `*Mapper` in `internal.adapter.in.rest` or `internal.adapter.out.persistence`, `*PersistenceAdapter` in `internal.adapter.out.persistence` and `@Repository`-annotated, `*LookupAdapter` anywhere under `internal.adapter` and plain `@Component`-annotated (the cross-module anti-corruption adapters), `@Entity` classes named `*JpaEntity` living under `internal.adapter.out.persistence` — the `Jpa` prefix makes explicit this is a JPA-technology-specific persistence model, distinct from the `internal.domain.model` aggregate it's mapped from/to — `@Configuration` classes named `*Config`, loggers must be private static final `log` — i.e. use Lombok `@Slf4j`) are all enforced by the ArchUnit suite in `architecture/` (`DomainArchitectureTest`, `AdapterArchitectureTest`, `PlatformArchitectureTest`, `GeneralRulesTest`, `ApplicationArchitectureTest`, plus `ModularityTests` for the module-boundary check). Follow them exactly — new code violating these will fail `mvn test`.

### Testing conventions (also ArchUnit-enforced, see `TestingRulesTest`)
- Any class annotated `@SpringBootTest`/`@DataJpaTest`/`@WebMvcTest` (non-abstract) must be named `*IT` so it runs under failsafe, not surefire.
- Non-`@DataJpaTest` `*IT` classes must extend `IntegrationTest` (`base/IntegrationTest.java`, activates the `test` profile / `BeerStoreApplication.PROFILE_TEST`, and imports `TestcontainersConfig` — see below).
- Use AssertJ (`org.assertj.core.api.Assertions`), never `org.junit.jupiter.api.Assertions`, in any class named `*Test`.
- Liquibase changelogs live under `beer-store-application/src/main/resources/database/changelog/`, referenced from `changelog-master.xml`. Requires the `spring-boot-starter-liquibase` dependency (not just `liquibase-core`) — Spring Boot 4 moved `LiquibaseAutoConfiguration` into its own module, so without the starter migrations silently never run and Hibernate schema validation fails.
- Database backend differs by context: local `spring-boot:run` uses H2 (`application.yml`, `ddl-auto: create`, Liquibase disabled). `*IT` classes extending `IntegrationTest` run against a real Postgres via Testcontainers (`base/TestcontainersConfig.java`, `@ServiceConnection PostgreSQLContainer`), with Liquibase enabled and `ddl-auto: validate` so migrations are actually exercised. The Spring Cloud Contract test bases (`ContractTestBase`, `ContractTestBaseMockMvc`) don't extend `IntegrationTest`, so they still run against the `test` profile's H2 datasource directly. Postgres is also the runtime database.
- Spring Boot 4 split `spring-boot-test-autoconfigure` per feature. `@AutoConfigureDataJpa`/`@AutoConfigureTestDatabase`/`@AutoConfigureTestEntityManager`/`TestEntityManager` now live under `org.springframework.boot.data.jpa.test.autoconfigure` / `org.springframework.boot.jdbc.test.autoconfigure` / `org.springframework.boot.jpa.test.autoconfigure`, `@AutoConfigureMockMvc` under `org.springframework.boot.webmvc.test.autoconfigure`, and `@AutoConfigureCache` under `org.springframework.boot.cache.test.autoconfigure` — each needs its own `spring-boot-starter-*-test` dependency (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-cache-test`) rather than coming transitively from `spring-boot-starter-test`.
