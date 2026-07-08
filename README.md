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
 - [x] Events, with Spring application events
 - [x] Spring Modulith (module boundaries, event publication, generated module docs)
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
