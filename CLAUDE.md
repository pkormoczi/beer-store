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
- `beer-store-contract.yaml` → REST server interfaces/models generated into `dev.ronin.demo.beerstore.infrastructure.api[.model]` via the `openapi-generator-maven-plugin` (`generate-producer` execution). Controllers implement these generated interfaces.
- `Customer.xsd` → JAXB classes for the SOAP endpoint via `cxf-xjc-plugin`.
- Spring Cloud Contract contracts (defined in `beer-store-contract/src/test/resources/contracts/`) generate two parallel test suites in `beer-store-application` at `generate-test-sources` time: `contract.explicit` (EXPLICIT mode, extends `ContractTestBase`, uses RestAssured against a random port) and `contract.mockmvc` (MOCKMVC mode, extends `ContractTestBaseMockMvc`). Both bases seed a `CustomerData` row read via `ContractDataReader` from the contract's JSON/yaml fixtures before running. The `generateTests` executions resolve the contract module's `-stubs` classifier via `contractsMode=LOCAL` (Aether-based, set directly on `contractDependency`) — the default `CLASSPATH` mode became unreliable under Spring Cloud Contract 5.0.x's plugin-realm classloading.
- `beer-store-client` generates a WebClient-based Java client from the *same* `beer-store-contract.yaml`, and verifies it against WireMock stubs published from the contract module's `-stubs` classifier.

Because of this pipeline, changes to the API almost always start in `beer-store-contract`, followed by `mvn clean install` in that module before the dependent modules will pick up regenerated sources.

### DDD layering in beer-store-application
Package layout under `dev.ronin.demo.beerstore` encodes the DDD/hexagonal (ports & adapters) layers directly, and `ApplicationArchitectureTest` (ArchUnit) enforces the dependency direction: `domain` may only be accessed by `adapter`/`infrastructure`; `adapter` may only be accessed by `infrastructure`; `infrastructure` may only be accessed by `adapter` (the `BeerStoreApplication` composition root wiring `PropertiesLogger` directly is an explicit, documented `ignoreDependency` exception to the last rule).

- `domain/<aggregate>/` (e.g. `customer`, `order`) — the aggregate root service (`Customers`, `Orders`), `data/` (JPA `@Entity` classes, always suffixed `Data`, never `Entity` — entities are "stupid data bags"), `repository/` (Spring Data repositories, only ever injected into `@Service` classes), `value/` (value objects/enums). These stay framework-annotated by deliberate choice: this PoC does not maintain a separate persistence model, so the JPA entity doubles as the domain model.
- `adapter/in/` — the driving/inbound side, translating between the generated OpenAPI/SOAP contracts and the domain services. `CustomersAdapter`/`OrdersAdapter` (plus `mapper/`, MapStruct, Spring component model, constructor injection) live at the package root because both protocols share them; protocol-specific entry points sit in subpackages: `adapter/in/rest/` (REST controllers implementing the generated OpenAPI interfaces, plus `rest/handler/` for `ErrorHandlingControllerAdvice` + `ErrorDetails`) and `adapter/in/soap/` (the `CustomerWs` SOAP endpoint).
- `infrastructure/config/`, `infrastructure/security/`, `infrastructure/logging/` — cross-cutting concerns only. `@Authorized` + `AuthorizedAspect` implement method-level authorization via AOP (`spring-boot-starter-aspectj` — Spring Boot 4 renamed the old `spring-boot-starter-aop`; still plain proxy-based Spring AOP, no weaving involved). `infrastructure/api[.model]` also holds the openapi-generator output — see Contract-first code generation above — since it's part of the wire-format contract rather than the adapter/translation logic that consumes it.

These naming/placement conventions (`*Repository` in a `repository` package and `@Repository`-annotated, `*Controller` in `adapter/in/rest` and `@RestController`/`@Controller`-annotated, `*Adapter` in `adapter/in` and `@Service`-annotated, `*Mapper` in `adapter/in/mapper`, `@Entity` classes named `*Data` living under `data`, `@Configuration` classes named `*Config`, loggers must be private static final `log` — i.e. use Lombok `@Slf4j`) are all enforced by the ArchUnit suite in `domain/achitecture/` (`DomainArchitectureTest`, `AdapterArchitectureTest`, `InfrastructureArchitectureTest`, `GeneralRulesTest`, `ApplicationArchitectureTest`). Follow them exactly — new code violating these will fail `mvn test`.

### Testing conventions (also ArchUnit-enforced, see `TestingRulesTest`)
- Any class annotated `@SpringBootTest`/`@DataJpaTest`/`@WebMvcTest` (non-abstract) must be named `*IT` so it runs under failsafe, not surefire.
- Non-`@DataJpaTest` `*IT` classes must extend `IntegrationTest` (`base/IntegrationTest.java`, activates the `test` profile / `BeerStoreApplication.PROFILE_TEST`, and imports `TestcontainersConfig` — see below).
- Use AssertJ (`org.assertj.core.api.Assertions`), never `org.junit.jupiter.api.Assertions`, in any class named `*Test`.
- Liquibase changelogs live under `beer-store-application/src/main/resources/database/changelog/`, referenced from `changelog-master.xml`. Requires the `spring-boot-starter-liquibase` dependency (not just `liquibase-core`) — Spring Boot 4 moved `LiquibaseAutoConfiguration` into its own module, so without the starter migrations silently never run and Hibernate schema validation fails.
- Database backend differs by context: local `spring-boot:run` uses H2 (`application.yml`, `ddl-auto: create`, Liquibase disabled). `*IT` classes extending `IntegrationTest` run against a real Postgres via Testcontainers (`base/TestcontainersConfig.java`, `@ServiceConnection PostgreSQLContainer`), with Liquibase enabled and `ddl-auto: validate` so migrations are actually exercised. The Spring Cloud Contract test bases (`ContractTestBase`, `ContractTestBaseMockMvc`) don't extend `IntegrationTest`, so they still run against the `test` profile's H2 datasource directly. Postgres is also the runtime database.
- Spring Boot 4 split `spring-boot-test-autoconfigure` per feature. `@AutoConfigureDataJpa`/`@AutoConfigureTestDatabase`/`@AutoConfigureTestEntityManager`/`TestEntityManager` now live under `org.springframework.boot.data.jpa.test.autoconfigure` / `org.springframework.boot.jdbc.test.autoconfigure` / `org.springframework.boot.jpa.test.autoconfigure`, `@AutoConfigureMockMvc` under `org.springframework.boot.webmvc.test.autoconfigure`, and `@AutoConfigureCache` under `org.springframework.boot.cache.test.autoconfigure` — each needs its own `spring-boot-starter-*-test` dependency (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-cache-test`) rather than coming transitively from `spring-boot-starter-test`.
