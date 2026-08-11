## :link: Ligoj Bootstrap ![Maven Central](https://img.shields.io/maven-central/v/org.ligoj.bootstrap/root)
REST+Front-End template with a ton of integrated component with many enterprise features : RBAC, Cache, modular modules

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.ligoj.bootstrap%3Aroot&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=org.ligoj.bootstrap%3Aroot)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?metric=alert_status&project=org.ligoj.bootstrap:root)](https://sonarcloud.io/dashboard/index/org.ligoj.bootstrap:root)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e6c472b13c5a49b4882d27632f79b6de)](https://www.codacy.com/gh/ligoj/bootstrap?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ligoj/bootstrap&amp;utm_campaign=Badge_Grade)
[![CodeFactor](https://www.codefactor.io/repository/github/ligoj/bootstrap/badge)](https://www.codefactor.io/repository/github/ligoj/bootstrap)
[![Known Vulnerabilities](https://snyk.io/test/github/ligoj/bootstrap/badge.svg)](https://snyk.io/test/github/ligoj/bootstrap)
[![Maintainability](https://api.codeclimate.com/v1/badges/f5654026ebe3ab16526c/maintainability)](https://codeclimate.com/github/ligoj/bootstrap/maintainability)
[![License](http://img.shields.io/:license-mit-blue.svg)](http://fabdouglas.mit-license.org/)

Key features:
- Convention over Code for everything: light webpack, http error code, CDI, Java9 named parameters
- RBAC with URL based authorization and dynamic roles
- Exception to REST/HTTP code mapping
- Advanced RS validation
- Tuned Jackson configuration for minified payload and validation
- Test powered with CSV data load to/from JPA entities
- TDD ready with pre-built asserts with Mockito and Wiremock, target is `100%` code coverage
- Optional encrypted properties support and database configuration with Jasypt
- Tuned configuration for dev, build and production
- Spring-Data extensions for performance, minimal code and exception handling

A Spring based REST architecture
- Spring Boot/Security/Web/Data
- CXF
- JPA / Hibernate
- Hibernate Validator for JPA and RS
- JUnit
- Wiremock

Requirements
- Java-25
- Maven 3.9

## Overview

Ligoj Bootstrap (`org.ligoj.bootstrap`, currently `4.0.0-SNAPSHOT`) is a Spring + Apache CXF JAX-RS architecture template consumed by downstream Ligoj projects as libraries and parent POMs. Requirements: Java 21, Maven 3.9. The Maven parent chain goes `module → parent/pom.xml → org.ligoj.parent:project` (external repo) — build profiles and plugin defaults live there.

## Commands

```bash
# Build everything (unit + integration tests)
mvn clean package

# All features (w/o deployment)
mvn clean package -Psources,javadoc,github,sonatype,jacoco,test

# Compile a module quickly (with its dependents)
mvn -pl bootstrap-core,bootstrap-business -am test-compile

# Tests for one module
mvn test -pl bootstrap-business

# Single test class
mvn test -pl bootstrap-business -Dtest=SystemPluginResourceTest

# Coverage (target is 100%)
mvn clean package -Pjacoco -Djacoco.includes="org.ligoj.bootstrap.*"

# Full feature build (w/o deployment)
mvn clean package -Psources,javadoc,github,sonatype,jacoco,test

# Check dependency updates
mvn versions:display-dependency-updates -Pjacoco -Dmaven.version.ignore="^(.*[.-](alpha|beta|rc|M|B|Alpha|Beta|BETA|RC|pre)[.-]?[0-9]*|[0-9]{8}.*)$"
```

**Test failures do NOT fail the build**: the parent POM sets `testFailureIgnore=true` for both surefire and failsafe. `BUILD SUCCESS` is meaningless for tests — always check the `Tests run: … Failures: … Errors: …` lines or `target/surefire-reports/`.

Test naming splits unit vs integration: `*Test` = unit/Spring test, `*IT` = integration test that boots the real app on Jetty (port 6380). `bootstrap-business` runs them as two surefire executions via `UTSuite`/`ITSuite` (class-name regex suites); elsewhere failsafe runs `*IT` only with `-Pit`.

`.mvn/jvm.config` passes `--sun-misc-unsafe-memory-access=allow` — required for the build JVM.

## Module map

Dependency layering: `bootstrap-core → bootstrap-business → bootstrap-plugin`, plus `bootstrap-core + bootstrap-launcher → bootstrap-business-test` and `bootstrap-launcher → bootstrap-web-test → bootstrap-web`.

- **bootstrap-core** — foundation library, no Spring Boot/CXF server. Bean & JPA base classes (`AbstractPersistable`, `AbstractAudited`), system entities (`org.ligoj.bootstrap.model.system.*`, tables prefixed `S_`), CSV engine, plugin SPI + `PluginsClassLoader`, Jasypt crypto, Hibernate naming strategies, custom validators.
- **bootstrap-business** — the REST/JPA runtime: CXF wiring, exception mappers, Jackson config, Spring Data extensions, RBAC filters, Hazelcast/JCache, `/system/**` REST resources.
- **bootstrap-plugin** — plugin management REST layer (`/system/plugin`): install/update from Maven Central/Nexus, `PluginApplicationRunListener` (swaps in `PluginsClassLoader` at Spring Boot startup), Javadoc→OpenAPI enrichment.
- **bootstrap-business-test** — test-support **library** (compile-scope deps): the `Abstract*Test` hierarchy, HSQLDB Spring contexts, RBAC CSV fixtures.
- **bootstrap-business-parent** / **bootstrap-web-parent** — pom-only parents consumed by downstream backend (`org.ligoj.app:app-api`) and UI (`app-ui`) projects; they preconfigure dependencies and resource filtering.
- **bootstrap-web** — UI-tier library: `BackendProxyServlet` reverse proxy, extended Spring Security request matchers, OAuth2 client support.
- **bootstrap-web-test** — pom-only test dependency aggregation (`<type>pom</type>`).
- **bootstrap-launcher** — two-class embedded-Jetty launcher (`org.ligoj.bootstrap.http.server.Main`) used by IDE runs and `*IT` tests; reads `jetty.properties` (default `META-INF/jetty/jetty-dev.properties`). Production downstream apps boot via Spring Boot instead.

## Architecture

**Spring wiring is XML-first**: contexts live at `src/*/resources/META-INF/spring/*.xml` and compose via `<import resource="classpath*:/META-INF/spring/…"/>` so plugin JARs can contribute. `core-context-common.xml` holds the single component-scan (`org.ligoj.bootstrap`); `default-autowire="byName"` is used pervasively — bean/field names matter.

**Package conventions** (root is always `org.ligoj.bootstrap`): `model.system` = JPA entities, `dao.system` = Spring Data repositories, `resource.system.<feature>` = JAX-RS resources under `/system/**`, `core.*` = reusable technical code, `http.*` = launcher/proxy/web security. One deliberate exception: `org.eclipse.jetty.util.resource.VisibleCombinedResource` in bootstrap-launcher (package-private Jetty API access).

**Spring Data extensions**: all repositories in `org.ligoj` get `RestRepository` methods (`findOneExpected`, `deleteAllExpected`, …) through `RestRepositoryFactoryBean` declared in `jpa-context-common.xml`. jqGrid/DataTables JSON filters translate to Criteria via `PaginationDao`/`DynamicSpecification`; grid responses use `TableItem`/`PaginationJson`.

**Exception → HTTP mapping**: `@Provider` mappers in `bootstrap-business/.../core/resource/mapper/` (registered in `rest-context-common.xml`) serialize a uniform `ServerError` JSON body. E.g. `BusinessException`/`ValidationJsonException` → 400, `EntityNotFoundException` → 404, `DataIntegrityViolationException` → 412, `FailSafeExceptionMapper` → 500 with message stripped. `@OnNullReturn404` turns a null JAX-RS return into 404.

**Jackson**: the single `objectMapper` bean is `ObjectMapperTrim` (NON_NULL, lower-cased enums, custom date/time (de)serializers via `BootstrapModule`). The codebase is on Jackson 3 (`tools.jackson.*` packages) with `jackson-annotations` still 2.x — watch package names when touching JSON code.

**RBAC**: `SystemAuthorization` rows hold role + HTTP method + URL regex (`type` API or UI). `AuthorizingFilter` (last Spring Security filter) matches request path+method against a JCache-cached structure built by `AuthorizationResource`; `RbacUserDetailsService` loads roles (cache `user-details`). Authentication is pre-auth header based (`SM_UNIVERSALID` + `x-api-key` via `ApiTokenAuthenticationFilter`).

**Plugin system**: `PluginsClassLoader` scans `${ligoj.home}/plugins/*.jar` (default `~/.ligoj/plugins`), keeps only the newest version per artifact, optionally verifies JAR signatures. Plugins implement `FeaturePlugin` (key format `a:b:c`); `SystemPluginResource` diffs discovered beans against the `S_PLUGIN` table on context refresh and runs `install()`/`update()`, seeding entities from `csv/<kebab-case-entity>.csv` inside the plugin JAR.

**Configuration/crypto**: properties may be Jasypt-encrypted as `ENC(...)`; password resolved from `app.crypto.password`/`APP_CRYPTO_PASSWORD`/`app.crypto.file` (see `core-context-common.xml`). `ConfigurationResource` resolves keys from Spring `Environment` first, then the `S_CONFIGURATION` table, cached in JCache.

## Testing

Extend the chain in `bootstrap-business-test` (`org.ligoj.bootstrap` package): `AbstractTest → AbstractDataGeneratorTest → AbstractSecurityTest → AbstractJpaTest → AbstractAppTest → AbstractServerTest` (WireMock on port 8120). `AbstractRestTest` boots the real server for `*IT`. Canonical Spring test setup (see `AbstractBootTest`):

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback @Transactional
```

- DB is in-memory HSQLDB (`jpa-context-test.xml`), which imports the production `jpa-context-common.xml` — repositories/auditing behave as in production. Persistence unit name is always `pu`.
- Test data loads from CSV via `CsvForJpa` / `persistEntities("csv/system-test", Class...)`; header row = property names, foreign keys as dotted paths (`role.name`), files named `<entity-kebab-case>.csv`.
- `AbstractAppTest.persistSystemEntities()` seeds the RBAC tables; default principal is `junit` (`initSpringSecurityContext(...)` to switch).
- `src/main/resources` **and** `src/test/resources` are Maven-filtered in bootstrap-business and downstream — literal `${...}` in resources will be substituted at build time.
- `MatcherUtil` asserts field/rule inside `ValidationJsonException`; `AbstractBusinessEntityTest` covers entity equals/hashCode reflectively.

