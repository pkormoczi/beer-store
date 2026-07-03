# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Beer Store is a Spring Boot proof-of-concept exploring Domain-Driven Design in a multi-module Maven build (Java 25, Spring Boot 4.1.0, `parent` groupId `dev.ronin.demo`). It exposes both REST and SOAP for the same domain and generates code from a shared API contract module.

Modules (in build order — `beer-store-contract` must be built first):
- **beer-store-contract** — the source of truth. Holds the OpenAPI spec (`beer-store-contract.yaml`), a test-only OpenAPI spec (`test.yaml`), the SOAP XSD (`Customer.xsd`), and Spring Cloud Contract contracts (`src/test/resources/contracts/**`). Publishing this module also builds a `-stubs` jar consumed by other modules for contract/stub-runner tests.
- **beer-store-application** — the actual Spring Boot server. DDD-layered (see Architecture below). Generates REST server interfaces/models and SOAP JAXB classes from the contract module at build time.
- **beer-store-client** — a thin Spring Boot client app that consumes the same OpenAPI spec via the `webclient`/`java` generator, used to verify the contract from the consumer side (WireMock + stub-runner).
- **jenkins-groovy-helper** — Groovy scripts for Jenkins/SonarQube provisioning; not part of the application build.

## Commands

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
- Code coverage (Clover): `mvn clean clover:setup test clover:aggregate clover:clover`
- Jacoco is wired in automatically via the `jacoco-maven-plugin` during the standard test/verify lifecycle.
- Sonar (local): `mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<token>`
- Local CI stack (Jenkins + SonarQube via Docker): from `CI/`, `docker-compose up` (add `--build --force-recreate` to rebuild), `docker-compose down` to stop.

Endpoints when running locally (port 8080):
- Swagger UI: `http://localhost:8080/swagger-ui.html`, spec at `/v3/api-docs`
- SOAP: `http://localhost:8080/services/customerService.wsdl` (basic auth `user`/`password`)

## Architecture

### Contract-first code generation
Nothing in `beer-store-application`'s API surface is hand-written on the wire-format side:
- `beer-store-contract.yaml` → REST server interfaces/models generated into `dev.ronin.demo.beerstore.infrastructure.api[.model]` via the `openapi-generator-maven-plugin` (`generate-producer` execution). Controllers implement these generated interfaces.
- `test.yaml` → a second, test-only REST surface generated into `infrastructure.test.api[.model]`, plus a TypeScript Angular client — used purely for contract test fixtures, not the production API.
- `Customer.xsd` → JAXB classes for the SOAP endpoint via `cxf-xjc-plugin`.
- Spring Cloud Contract contracts (defined in `beer-store-contract/src/test/resources/contracts/`) generate two parallel test suites in `beer-store-application` at `generate-test-sources` time: `contract.explicit` (EXPLICIT mode, extends `ContractTestBase`, uses RestAssured against a random port) and `contract.mockmvc` (MOCKMVC mode, extends `ContractTestBaseMockMvc`). Both bases seed a `CustomerData` row read via `ContractDataReader` from the contract's JSON/yaml fixtures before running. The `generateTests` executions resolve the contract module's `-stubs` classifier via `contractsMode=LOCAL` (Aether-based, set directly on `contractDependency`) — the default `CLASSPATH` mode became unreliable under Spring Cloud Contract 5.0.x's plugin-realm classloading.
- `beer-store-client` generates a WebClient-based Java client from the *same* `beer-store-contract.yaml`, and verifies it against WireMock stubs published from the contract module's `-stubs` classifier.

Because of this pipeline, changes to the API almost always start in `beer-store-contract`, followed by `mvn clean install` in that module before the dependent modules will pick up regenerated sources.

### DDD layering in beer-store-application
Package layout under `dev.ronin.demo.beerstore` encodes the DDD layers directly, and `ApplicationArchitectureTest` (ArchUnit) enforces the dependency direction: `domain` may only be accessed by `adapter`/`infrastructure`; `adapter` may only be accessed by `infrastructure`.

- `domain/<aggregate>/` (e.g. `customer`, `order`) — the aggregate root service (`Customers`, `Orders`), `data/` (JPA `@Entity` classes, always suffixed `Data`, never `Entity` — entities are "stupid data bags"), `repository/` (Spring Data repositories, only ever injected into `@Service` classes), `value/` (value objects/enums).
- `infrastructure/adapter/` — adapters between the generated OpenAPI/SOAP interfaces and the domain services, with `mapper/` holding MapStruct mappers (Spring component model, constructor injection — see compiler args in the app's `pom.xml`).
- `infrastructure/controller/` — REST controllers implementing the generated OpenAPI interfaces.
- `infrastructure/endpoint/` — SOAP endpoints (`CustomerWs`).
- `infrastructure/config/`, `infrastructure/security/`, `infrastructure/logging/` — cross-cutting concerns. `@Authorized` + `AuthorizedAspect` implement method-level authorization via AOP (`spring-boot-starter-aspectj` — Spring Boot 4 renamed the old `spring-boot-starter-aop`; still plain proxy-based Spring AOP, no weaving involved).

These naming/placement conventions (`*Repository` in a `repository` package and `@Repository`-annotated, `*Controller` in `controller` and `@RestController`/`@Controller`-annotated, `@Entity` classes named `*Data` living under `data`, `@Configuration` classes named `*Config`, loggers must be private static final `log` — i.e. use Lombok `@Slf4j`) are all enforced by the ArchUnit suite in `domain/achitecture/` (`DomainArchitectureTest`, `InfrastructureArchitectureTest`, `GeneralRulesTest`, `ApplicationArchitectureTest`). Follow them exactly — new code violating these will fail `mvn test`.

### Testing conventions (also ArchUnit-enforced, see `TestingRulesTest`)
- Any class annotated `@SpringBootTest`/`@DataJpaTest`/`@WebMvcTest` (non-abstract) must be named `*IT` so it runs under failsafe, not surefire.
- Non-`@DataJpaTest` `*IT` classes must extend `IntegrationTest` (`base/IntegrationTest.java`, activates the `test` profile / `BeerStoreApplication.PROFILE_TEST`).
- Use AssertJ (`org.assertj.core.api.Assertions`), never `org.junit.jupiter.api.Assertions`, in any class named `*Test`.
- Liquibase changelogs live under `beer-store-application/src/main/resources/database/changelog/`, referenced from `changelog-master.xml`; H2 is used for tests/dev, Postgres at runtime. Requires the `spring-boot-starter-liquibase` dependency (not just `liquibase-core`) — Spring Boot 4 moved `LiquibaseAutoConfiguration` into its own module, so without the starter migrations silently never run and Hibernate schema validation fails.
- Spring Boot 4 split `spring-boot-test-autoconfigure` per feature. `@AutoConfigureDataJpa`/`@AutoConfigureTestDatabase`/`@AutoConfigureTestEntityManager`/`TestEntityManager` now live under `org.springframework.boot.data.jpa.test.autoconfigure` / `org.springframework.boot.jdbc.test.autoconfigure` / `org.springframework.boot.jpa.test.autoconfigure`, `@AutoConfigureMockMvc` under `org.springframework.boot.webmvc.test.autoconfigure`, and `@AutoConfigureCache` under `org.springframework.boot.cache.test.autoconfigure` — each needs its own `spring-boot-starter-*-test` dependency (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-cache-test`) rather than coming transitively from `spring-boot-starter-test`.
