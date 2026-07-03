## Beer Store
Beer Store is a simple proof of concept application focusing on the following features:

 - [x] Multi module Maven build with Maven wrapper
 - [x] Spring Boot
 - [x] Domain Driven Design considerations
 - [x] Spring Framework with full java config
 - [x] Spring Data with Hibernate
 - [x] Database migrations with Liquibase
 - [x] SOAP Endpoints
 - [x] REST Controllers
 - [x] Swagger / OpenAPI, contract-first code generation
 - [x] Spring Security (REST + SOAP, basic authentication with roles)
 - [x] MapStruct as adapter
 - [x] Spring Boot Actuator
 - [x] Exception and error handling and documentation in the REST Controllers
 - [x] Unit tests
 - [x] Integration tests
 - [x] Architecture tests (ArchUnit) enforcing DDD layering and naming conventions
 - [x] Test coverage tool
 - [ ] Allure reports
 - [x] Spring Cloud Contracts
 - [x] Local CI pipeline (Jenkins + SonarQube via Docker Compose)
 - [ ] Dockerized E2E tests, Testcontainers?
 - [ ] GitHub Actions CI pipeline (build/test/coverage badge)
 - [ ] Static code analysis (Checkstyle/PMD/SpotBugs)
 - [ ] Dependency vulnerability scanning (OWASP dependency-check / Dependabot)
 - [ ] Observability - metrics/tracing (Micrometer, OpenTelemetry)
 - [ ] DBRider
 - [ ] Layered OCI image creation for the application itself
 - [ ] Profiling - local, integration test, docker, prod
 - [ ] Transaction management refinement 
 - [ ] Async calls via SOAP
 - [ ] Events, with Spring application events
 - [ ] License

## DEV notes
- Run with command: `mvnw spring-boot:run`
- beer-store-contract is a separate artifact. Need to run mvn clean install first for that module.
- Clover reports: `mvn clean clover:setup test clover:aggregate clover:clover`
- Swagger UI at: `http://localhost:8080/swagger-ui.html`
- Swagger JSON: `http://localhost:8080/v2/api-docs`
- SOAP Endpoint: `http://localhost:8080/services/customerService.wsdl` with user `user` and password `password`
- Sonar on local: `mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<your-sonar-token>`