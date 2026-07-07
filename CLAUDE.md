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
Package layout under `dev.ronin.demo.beerstore` is **module-first**, not layer-first: each top-level package is a Spring Modulith application module, and the hexagonal/DDD layers (`application`, `persistence`, `web`) live *inside* each module rather than as global top-level packages. `ModularityTests` (`architecture/ModularityTests.java`) calls `ApplicationModules.of(BeerStoreApplication.class, ...).verify()` to enforce module boundaries — this is the primary boundary check now; it also writes PlantUML module diagrams to `target/spring-modulith-docs`.

- `customer/` — the customer module. Exposes `Customer` (a record — the plain domain model, no JPA), `Address` (record value object), `ManageCustomersUseCase` (inbound port) and `CustomerNotFoundException` at the package root; `@ApplicationModule(allowedDependencies = {})` — **customer depends on nothing else**, enforced by Modulith. Internals: `application/` (the `Customers` aggregate service + the `CustomerRepository` outbound port, both working with the `Customer` record), `persistence/` (`CustomerData` JPA `@Entity`, flat columns — no nested `@Embeddable`; `CustomerJpaRepository`; `CustomerPersistenceAdapter`; `CustomerPersistenceMapper`, MapStruct, mapping `Customer` ↔ `CustomerData`), `web/` (`CustomerController`, `CustomersAdapter`, `CustomerMapper`, mapping `Customer` ↔ the generated `CustomerModel`/SOAP `Customer`), `soap/` (`CustomerWs`).
- `order/` — the order module. Exposes `Order`/`Beer` (records — `Order.customerId` is a plain `Long`, *not* a JPA association to the customer module), `OrderStatus`/`BeerStyle` (enums), `ManageOrdersUseCase`, `OrderPlaced` (domain event) and the order exceptions at the package root; `@ApplicationModule(allowedDependencies = "customer")` — order may depend on customer (to validate the customer an order is placed for via `ManageCustomersUseCase`) plus the always-open `shared`/`infrastructure` modules, nothing else. Internals mirror `customer`: `application/` (`Orders`, `OrderRepository`/`BeerRepository` ports, `OrderPlacedEventListener`), `persistence/` (`OrderData`/`BeerData` JPA entities — `OrderData.beers` keeps its `@OneToMany`/`@JoinTable` since `BeerData` is in the same module; `OrderPersistenceMapper`), `web/` (`OrderController`, `OrdersAdapter`, `OrderMapper`).
- `shared/` — `@ApplicationModule(type = OPEN)`: any module may depend on it. Holds only the openapi-generator/cxf-xjc output (`shared/api[.model]`, `shared/contract/customerdata`, see Contract-first code generation above) plus the hand-written `shared/web/HomeController`.
- `infrastructure/` — `@ApplicationModule(type = OPEN)`. `config/`, `security/`, `logging/` (unchanged: `@Authorized` + `AuthorizedAspect` AOP-based method authorization via `spring-boot-starter-aspectj`), plus `web/handler/` for the global `ErrorHandlingControllerAdvice` + `ErrorDetails` (references the customer/order modules' exposed exception types, which is fine — OPEN only affects who may depend *on* infrastructure, not the reverse).

**Cross-aggregate coupling, deliberately removed:** `order` used to hold a direct `@ManyToOne(cascade = ALL)` JPA association from `OrderData` to the customer module's entity — a hard Modulith boundary violation (an entity from one module referenced by another module's entity) and the reason the persistence models needed separating from the domain records in the first place. `Orders.newOrder` now only calls `ManageCustomersUseCase.getCustomer(customerId)` synchronously (to validate the customer exists) and stores the plain `customerId`; `OrderPlaced` is published as a decoupled follow-up event (see below), consumed only within the `order` module itself via `OrderPlacedEventListener`/`@ApplicationModuleListener` (a listener in `customer` would create a `customer → order` dependency, which `customer`'s `allowedDependencies = {}` forbids) — demonstrating the Spring Modulith JPA event publication registry (`spring-modulith-starter-jpa`; the `EVENT_PUBLICATION` table is auto-created on startup via `spring.modulith.events.jdbc.schema-initialization.enabled`, which defaults to `true`).

These naming/placement conventions (`*Repository` ports in `application/` and `@Repository`-annotated, `*Controller` in `web/` and `@RestController`/`@Controller`-annotated, `*Adapter` in `web/` and `@Service`-annotated — except the concrete aggregate services like `Customers`/`Orders`, which are `@Service` but implement a `*UseCase` port instead of being named `*Adapter`, `*Mapper` in `web/` or `persistence/`, `*PersistenceAdapter` in `persistence/` and `@Repository`-annotated, `@Entity` classes named `*Data` living under `persistence/`, `@Configuration` classes named `*Config`, loggers must be private static final `log` — i.e. use Lombok `@Slf4j`) are all enforced by the ArchUnit suite in `architecture/` (`DomainArchitectureTest`, `AdapterArchitectureTest`, `InfrastructureArchitectureTest`, `GeneralRulesTest`, `ApplicationArchitectureTest`, plus `ModularityTests` for the module-boundary check). Follow them exactly — new code violating these will fail `mvn test`.

### Testing conventions (also ArchUnit-enforced, see `TestingRulesTest`)
- Any class annotated `@SpringBootTest`/`@DataJpaTest`/`@WebMvcTest` (non-abstract) must be named `*IT` so it runs under failsafe, not surefire.
- Non-`@DataJpaTest` `*IT` classes must extend `IntegrationTest` (`base/IntegrationTest.java`, activates the `test` profile / `BeerStoreApplication.PROFILE_TEST`, and imports `TestcontainersConfig` — see below).
- Use AssertJ (`org.assertj.core.api.Assertions`), never `org.junit.jupiter.api.Assertions`, in any class named `*Test`.
- Liquibase changelogs live under `beer-store-application/src/main/resources/database/changelog/`, referenced from `changelog-master.xml`. Requires the `spring-boot-starter-liquibase` dependency (not just `liquibase-core`) — Spring Boot 4 moved `LiquibaseAutoConfiguration` into its own module, so without the starter migrations silently never run and Hibernate schema validation fails.
- Database backend differs by context: local `spring-boot:run` uses H2 (`application.yml`, `ddl-auto: create`, Liquibase disabled). `*IT` classes extending `IntegrationTest` run against a real Postgres via Testcontainers (`base/TestcontainersConfig.java`, `@ServiceConnection PostgreSQLContainer`), with Liquibase enabled and `ddl-auto: validate` so migrations are actually exercised. The Spring Cloud Contract test bases (`ContractTestBase`, `ContractTestBaseMockMvc`) don't extend `IntegrationTest`, so they still run against the `test` profile's H2 datasource directly. Postgres is also the runtime database.
- Spring Boot 4 split `spring-boot-test-autoconfigure` per feature. `@AutoConfigureDataJpa`/`@AutoConfigureTestDatabase`/`@AutoConfigureTestEntityManager`/`TestEntityManager` now live under `org.springframework.boot.data.jpa.test.autoconfigure` / `org.springframework.boot.jdbc.test.autoconfigure` / `org.springframework.boot.jpa.test.autoconfigure`, `@AutoConfigureMockMvc` under `org.springframework.boot.webmvc.test.autoconfigure`, and `@AutoConfigureCache` under `org.springframework.boot.cache.test.autoconfigure` — each needs its own `spring-boot-starter-*-test` dependency (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-cache-test`) rather than coming transitively from `spring-boot-starter-test`.
