## Beer Store

[![CI](https://github.com/pkormoczi/beer-store/actions/workflows/ci.yml/badge.svg)](https://github.com/pkormoczi/beer-store/actions/workflows/ci.yml)

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
 - [ ] Testcontainers: integration tests against a real Postgres (`@ServiceConnection`) instead of H2, exercising the Liquibase migrations
 - [ ] Dockerized E2E tests
 - [ ] DBRider

### CI/CD & DevOps
 - [x] Local CI pipeline (Jenkins + SonarQube via Docker Compose)
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

### Misc
 - [ ] License

## CI/CD pipeline (GitHub Actions)

The `.github/workflows/ci.yml` workflow runs on every push to `master` and on pull requests targeting it:

1. Build `beer-store-contract` first (installed to the local repo, since dependent modules resolve its `-stubs` classifier from there), then the full reactor.
2. Run unit (Surefire) and integration (Failsafe) tests in one pass, with Jacoco coverage covering both. Test and coverage reports are uploaded as workflow artifacts.
3. Generate an [Allure](https://allurereport.org/) test report (request/response attachments on the RestAssured-based contract tests via `allure-rest-assured`).
4. Run SonarCloud analysis (on `master` pushes and on same-repo pull requests).
5. On `master` pushes only:
   - Build a layered OCI image with Cloud Native Buildpacks (`spring-boot:build-image`, no Dockerfile).
   - Scan the image with [Trivy](https://trivy.dev/) (CRITICAL/HIGH, report-only — a scanner hiccup or a known base-image CVE doesn't block the pipeline).
   - Publish both the Allure and Trivy reports to **GitHub Pages**, behind a small landing page linking to each (`/allure/`, `/trivy/`).
   - Push the image to Docker Hub (tagged with the commit SHA and `latest`).

Live reports (updated on every `master` push): **https://pkormoczi.github.io/beer-store/**

## DEV notes
- Run with command: `mvnw spring-boot:run`
- beer-store-contract is a separate artifact. Need to run mvn clean install first for that module.
- Clover reports: `mvn clean clover:setup test clover:aggregate clover:clover`
- Swagger UI at: `http://localhost:8080/swagger-ui.html`
- Swagger JSON: `http://localhost:8080/v2/api-docs`
- SOAP Endpoint: `http://localhost:8080/services/customerService.wsdl` with user `user` and password `password`
- Sonar on local: `mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<your-sonar-token>`