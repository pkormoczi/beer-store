# Drift map — where each doc's facts come from

Detailed inventory backing `update-docs`'s Step 1/3. Line numbers are approximate (docs move) —
use them to locate the section, then re-read the current file; don't trust a stale line number
over the actual heading text.

## README.md

| Section (approx. lines) | What it claims | Source of truth to diff against |
|---|---|---|
| Badges (L3–6) | CI badge, SonarCloud Quality Gate + Coverage, Docker Hub image size | Repo slug `pkormoczi/beer-store`, workflow filename `ci.yml`, SonarCloud project key `pkormoczi_beer-store`, Docker Hub repo `pkormoczi/beer-store` — check these identifiers still match if the repo/project was ever renamed |
| Feature checklist — Architecture & domain, API layer, Testing & quality, CI/CD & DevOps, Misc (L10–59) | `[x]`/`[ ]` per feature | The actual code/config for that feature. High-risk items: Spring Modulith (`@ApplicationModule` usage), virtual threads (`spring.threads.virtual.enabled` in `application.yml`), RFC 9457 Problem Details (`spring.mvc.problemdetails.enabled`), OAuth2/JWT, Testcontainers/`@ServiceConnection`, ArchUnit tests, Spring Cloud Contracts, Trivy/SonarCloud/Buildpacks/Docker Hub CI steps. A `[ ]` only flips to `[x]` when the feature is demonstrably implemented — check the actual property/annotation, don't infer from intent |
| CI/CD pipeline prose (L61–75) | Step-by-step description of the GitHub Actions job, plus the GitHub Pages reports URL | `.github/workflows/ci.yml` — job triggers, tool list (Jacoco, Allure, SonarCloud, Buildpacks, Trivy), master-only vs. every-push steps, image tags, GitHub Pages path (`pkormoczi.github.io/beer-store`) |
| Endpoint list (L83–86) | Swagger UI, `/v3/api-docs`, SOAP WSDL path + basic-auth creds, H2 console path | `beer-store-application/src/main/resources/application.yml` (paths, `spring.h2.console.enabled`) and the SOAP security config in `platform/security` |
| Build/run commands (L80–136) | Contract-first build order, `mvnw spring-boot:run`, `spring-boot:build-image` + derived tag, `postgres:17-alpine` pin, Spring datasource/Liquibase/`ddl-auto` CLI overrides, `config/` mount path | Root/module `pom.xml` `revision` property (image tag), Testcontainers Postgres image version (must match what `TestcontainersConfig` actually pulls), `application.yml` defaults, `config/application.properties` |

Cross-reference: README points to `CLAUDE.md` twice (Postgres/Liquibase/`ddl-auto: validate`
rationale) and to `.github/workflows/ci.yml`. It does not link `docs/architektura-diagramok.md`
or `docs/ADR.MD` — that's fine, no need to add links unless asked.

## docs/architektura-diagramok.md

All four diagrams are explicitly hand-maintained (the file says so in its own intro and in its
"Karbantartás" section — **read that section first**, it's the file's own drift checklist and
this table only restates it for quick lookup without opening the file).

| Diagram | Hard-codes | Source of truth |
|---|---|---|
| 1. Modul-térkép | The 4 `@ApplicationModule`s + `platform`, edges = `allowedDependencies` | Each module's `package-info.java` (`customer`, `product`, `order`, `shared`); `spring.modulith.detection-strategy=explicitly-annotated`; `PlatformBoundaryTest`. Tied to **ADR-04** |
| 2. Hexagonális port-metszet | The `order` module's entire class inventory by name: `OrderController`, `OrderRestAdapter`, `OrderMapper`, `OrderRestExceptionHandler`, `OrderManagement`, the `command/query/view/type/exception` packages, `Orders`, `Order`/`OrderLine`, `OrderPlaced`, `OrderRepository`/`CustomerLookup`/`BeerLookup`/`BeerSnapshot`, `OrderPersistenceAdapter`/`OrderJpaRepository`/entity classes, `CustomerLookupAdapter`/`BeerLookupAdapter` | `order/api/OrderManagement.java`, `order/application/service/Orders.java`, `order/application/port/out/*.java`, `order/adapter/in/rest/*.java`, `order/adapter/out/**/*.java` — same source `CLAUDE.md`'s Module reference table uses, so check both together |
| 3. Rendelés végigfutása (sequence) | The exact step order of `Orders.placeOrder`: `assertCustomerExists` → `getCustomer` → `findExisting` → `findAllById` → `BeerView → BeerSnapshot` → `Order.place` → `save` → `publishEvent(OrderPlaced)`, plus `CustomerNotFoundException`/`UnknownBeerException` | `order/application/service/Orders.java` — a reordered step, renamed method, or new/removed guard clause desyncs this diagram specifically |
| 4. Modulközi ACL kommunikáció | `CustomerLookup`/`BeerLookup`/`BeerSnapshot` ports, `CustomerLookupAdapter`/`BeerLookupAdapter`, `customer::api` (`CustomerManagement`, `GetCustomer`), `product::api` (`BeerManagement`, `FindBeers`, `BeerView`), the `BeerView → BeerSnapshot` mapping | Same `order/adapter/out/**` + the two foreign `api` packages it reads from. Tied to **ADR-01** |

Cross-reference: this file names `CLAUDE.md`'s "Spring Modulith module layout" and "Naming &
convention cheat-sheet" sections, and explicitly says diagrams 2 and 4 must move with `CLAUDE.md`'s
Module reference table (`customer`/`product`/`order` columns). It also names
`ModularityTests.writesDocumentation()` and `target/spring-modulith-docs/{components.puml,
module-*.puml}` as the generated (non-authoritative-for-this-file, git-ignored) counterpart —
don't try to reconcile against those generated files, they're not committed and not this file's
job to match exactly.

## CLAUDE.md

| Section (approx. lines) | What it claims | Source of truth |
|---|---|---|
| Header (L1–6) | Points to `docs/ADR.MD` for "why" | `docs/ADR.MD` exists at that path (already true post-move) |
| Project (L8–15) | Java/Spring Boot/groupId versions, 3-module list, build order | Root `pom.xml` (`<properties>`, `<modules>`), `.mvn/wrapper` (Maven wrapper version) |
| Commands (L17–38) | Build/test/run commands, two embedded test class names (`OrdersTest`, `CustomersIT`), Jacoco wiring claim, CI secrets list, endpoint list | The named test classes must still exist under `beer-store-application/src/test/java` (or `src/it` per the honeycomb layout); `.github/workflows/ci.yml` for the secrets list; endpoint list must match README's copy |
| Contract-first code generation (L42–49) | `openapi-generator-maven-plugin` execution name, `apiPackage`/`modelPackage` property, generated package names, `cxf-xjc-plugin` target package, contract folder names (`customer/`, `catalog/`), generated suite names (`contract.explicit`/`contract.mockmvc`), `nameSuffixForTests=IT`, `contractsMode=LOCAL` | `beer-store-contract/beer-store-contract.yaml`, `Customer.xsd`, `beer-store-application/pom.xml` plugin config, `beer-store-contract/src/test/resources/contracts/**` folder names |
| Spring Modulith module layout — package tree (L51–82) | The `<module>/api\|domain\|application\|adapter/...` tree shape | Actual package layout under `dev.ronin.demo.beerstore.**` — any package restructure desyncs this ASCII tree |
| **Module reference table (L84–109)** — highest-risk block in the repo | Full class-name matrix for `customer`/`product`/`order`: `allowedDependencies`, api root, `command`/`query`/`view`/`type`/`exception`, `domain/model`/`domain/event`, `application/service`, `application/port/out`, `adapter/in`, `adapter/out/persistence/jpa`, `adapter/out/<other module>` | Each module's `package-info.java` (`allowedDependencies` row) + that module's actual class inventory (every other row). **This is the same source diagrams 2 and 4 in `docs/architektura-diagramok.md` use — update both together.** The Notes below the table cite ADR-16, ADR-21, ADR-01, ADR-09, ADR-12, ADR-07 — verify those tags still resolve in `docs/ADR.MD` |
| shared and platform (L111–114) | Concrete class names: `HomeController`, `kernel.Money`, `security.*`, `rest.*`, `observability.*`, `openapi.OpenApiConfig`, `ws.WebServiceConfig`, `PlatformBoundaryTest` rule names | `shared/` and `platform/` package contents; tied to ADR-04, ADR-15, ADR-01 |
| Naming & convention cheat-sheet (L116–142) | Table mapping naming pattern → package → enforcing ArchUnit test class; a paragraph inventorying all arch-test class names | `beer-store-application/src/test/java/.../architecture/**` — a renamed/added/removed ArchUnit test class or rule desyncs this |
| Testing strategy (L144–154) | Points to `docs/TESTING_STRATEGY.md` + ADR-22 | `docs/TESTING_STRATEGY.md` exists at that path (already true post-move); ADR-22 resolves in `docs/ADR.MD` |
| Testing conventions (L156–164) | `TestingRulesTest`, `IntegrationTest`, `TestcontainersConfig`, Liquibase changelog path, single-folder policy, Spring Boot 4 test-autoconfigure package relocations | `base/IntegrationTest.java`, `base/TestcontainersConfig.java`, `src/main/resources/database/changelog/`, the actual Spring Boot version's package layout. **The single-folder Liquibase policy is a deliberate rule, not a fact — don't "fix" it, only verify the described mechanics (H2 local / Postgres `*IT`) still hold** |

Cross-reference: `CLAUDE.md` → `docs/ADR.MD` (header), `docs/TESTING_STRATEGY.md` (Testing
strategy section), `README.md`'s "CI/CD pipeline" section + `.github/workflows/ci.yml` (Commands
section). It does not link `docs/architektura-diagramok.md` directly, but the coupling is real in
practice via the Module reference table — see above.

## Cheap, high-value checks (run every time, not diff-gated)

- Every `ADR-NN` tag in `CLAUDE.md` or `docs/architektura-diagramok.md` resolves to a real entry
  in `docs/ADR.MD` (currently used: ADR-01, ADR-04, ADR-05, ADR-07, ADR-09, ADR-12, ADR-15,
  ADR-16, ADR-21, ADR-22).
- `README.md` and `CLAUDE.md` endpoint lists match each other word-for-word (Swagger UI path,
  `/v3/api-docs`, SOAP WSDL path + creds).
- Mermaid syntax in `docs/architektura-diagramok.md` — see `SKILL.md` Step 3 for the two specific
  failure modes already hit in this file (generic angle brackets in sequence messages; the
  sandwiched `-. text .->` dotted-edge form breaking on hyphenated labels).
