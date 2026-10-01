## :link: Ligoj Bootstrap ![Maven Central](https://img.shields.io/maven-central/v/org.ligoj.bootstrap/root)
REST back-end template with many integrated components and enterprise features: RBAC, MFA, cache, plug-ins

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.ligoj.bootstrap%3Aroot&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=org.ligoj.bootstrap%3Aroot)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?metric=alert_status&project=org.ligoj.bootstrap:root)](https://sonarcloud.io/dashboard/index/org.ligoj.bootstrap:root)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e6c472b13c5a49b4882d27632f79b6de)](https://www.codacy.com/gh/ligoj/bootstrap?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ligoj/bootstrap&amp;utm_campaign=Badge_Grade)
[![CodeFactor](https://www.codefactor.io/repository/github/ligoj/bootstrap/badge)](https://www.codefactor.io/repository/github/ligoj/bootstrap)
[![Known Vulnerabilities](https://snyk.io/test/github/ligoj/bootstrap/badge.svg)](https://snyk.io/test/github/ligoj/bootstrap)
[![License](http://img.shields.io/:license-mit-blue.svg)](http://fabdouglas.mit-license.org/)

Key features:
- Convention over code: HTTP error codes, Spring XML wiring by name, JAX-RS named parameters
- RBAC with URL based authorization and dynamic roles, API tokens
- Multi-factor authentication: TOTP authenticator applications and passkeys (WebAuthn)
- Exception to REST/HTTP code mapping
- Advanced JAX-RS validation
- Tuned Jackson configuration for minified payload and validation
- Test powered with CSV data load to/from JPA entities
- TDD ready with pre-built asserts with Mockito and WireMock, target is `100%` code coverage
- Optional encrypted properties support and database configuration with Jasypt
- Plug-in class loader with optional JAR signature verification
- Spring-Data extensions for performance, minimal code and exception handling

A Spring based REST architecture
- Spring Security/Web/Data
- Apache CXF (JAX-RS)
- JPA / Hibernate
- Hibernate Validator for JPA and JAX-RS
- Hazelcast / JCache
- JUnit, WireMock

Requirements
- Java 25
- Maven 3.9.16

## Overview

Ligoj Bootstrap (`org.ligoj.bootstrap`) is a Spring + Apache CXF JAX-RS architecture template consumed by downstream Ligoj projects as libraries and parent POMs. The Maven parent chain goes `module → parent/pom.xml → org.ligoj.parent:project` (external repository): build profiles and plugin defaults live there, dependency versions live in `parent/pom.xml`.

## Commands

```bash
# Build everything (unit + integration tests)
mvn clean package

# All features (w/o deployment)
mvn clean package -Psources,javadoc,github,sonatype,jacoco,test

# Compile modules quickly (with their dependencies), tests included
mvn -pl bootstrap-core,bootstrap-business -am test-compile

# Tests of one module
mvn test -pl bootstrap-core

# Single test class
mvn test -pl bootstrap-core -Dtest=PluginsClassLoaderTest

# Coverage (target is 100%)
mvn clean package -Pjacoco -Djacoco.includes="org.ligoj.bootstrap.*"

# Check dependency updates
mvn versions:display-dependency-updates -Pjacoco -Dmaven.version.ignore="(?i)^(.*[.-](alpha|beta|rc|M|B|RC|pre|CR|jdk5)[.-]?[0-9]*|[0-9]{8}.*)$"
```

**Test failures do NOT fail the build**: the parent POM sets `testFailureIgnore=true` for both surefire and failsafe. `BUILD SUCCESS` is meaningless for tests — always check the `Tests run: … Failures: … Errors: …` lines or `target/surefire-reports/`.

Test naming splits unit vs integration: `*Test` = unit/Spring test, `*IT` = integration test that boots the real application on Jetty (port 6380). `bootstrap-business` runs them as two surefire executions via `UTSuite`/`ITSuite` (class-name regex suites); elsewhere failsafe runs `*IT` only with `-Pit`.

`.mvn/jvm.config` passes `--sun-misc-unsafe-memory-access=allow` — required for the build JVM.

## Module map

Dependency layering: `bootstrap-core → bootstrap-business`, plus `bootstrap-core → bootstrap-business-test` (test scope dependency of `bootstrap-business`).

- **bootstrap-core** — foundation library, no Spring Boot/CXF server. Bean & JPA base classes (`AbstractPersistable`, `AbstractAudited`), system entities (`org.ligoj.bootstrap.model.system.*`, tables prefixed `S_`), CSV engine, plug-in SPI + `PluginsClassLoader`, Jasypt crypto, TOTP/WebAuthn helpers, Hibernate naming strategies, custom validators.
- **bootstrap-business** — the REST/JPA runtime: CXF wiring, exception mappers, Jackson config, Spring Data extensions, RBAC filters, Hazelcast/JCache, `/system/**` REST resources (configuration, cache, security, user, API tokens, MFA, hooks, session).
- **bootstrap-business-test** — test-support **library** (compile-scope deps): the `Abstract*Test` hierarchy, HSQLDB Spring contexts, RBAC CSV fixtures, and the embedded Jetty launcher `http.server.Main` used by `*IT`.
- **bootstrap-business-parent** — pom-only parent consumed by downstream back-end projects (`org.ligoj.app:app-api`); it preconfigures dependencies and resource filtering.
- **parent** — BOM and shared build configuration of all the modules above.

## Architecture

**Spring wiring is XML-first**: contexts live at `src/*/resources/META-INF/spring/*.xml` and compose via `<import resource="classpath*:/META-INF/spring/…"/>` so plug-in JARs can contribute. `core-context-common.xml` holds the single component-scan (`org.ligoj.bootstrap`); `default-autowire="byName"` is used pervasively — bean/field names matter.

**Package conventions** (root is always `org.ligoj.bootstrap`): `model.system` = JPA entities, `dao.system` = Spring Data repositories, `resource.system.<feature>` = JAX-RS resources under `/system/**`, `core.*` = reusable technical code. One deliberate exception: `org.eclipse.jetty.util.resource.VisibleCombinedResource` in bootstrap-business-test (package-private Jetty API access).

**Spring Data extensions**: all repositories in `org.ligoj` get `RestRepository` methods (`findOneExpected`, `deleteAllExpected`, …) through `RestRepositoryFactoryBean`. jqGrid/DataTables JSON filters translate to Criteria via `PaginationDao`/`DynamicSpecification`; grid responses use `TableItem`/`PaginationJson`.

**Exception → HTTP mapping**: `@Provider` mappers in `bootstrap-business/.../core/resource/mapper/` (registered in `rest-context-common.xml`) serialize a uniform `ServerError` JSON body. E.g. `BusinessException`/`ValidationJsonException` → 400, `EntityNotFoundException` → 404, `DataIntegrityViolationException` → 412, `FailSafeExceptionMapper` → 500 with message stripped. `@OnNullReturn404` turns a null JAX-RS return into 404.

**Jackson**: the single `objectMapper` bean is `ObjectMapperTrim` (NON_NULL, lower-cased enums, custom date/time (de)serializers registered in its internal `BootstrapModule`). The codebase is on Jackson 3 (`tools.jackson.*` packages) with `jackson-annotations` still 2.x — watch package names when touching JSON code.

**RBAC**: `SystemAuthorization` rows hold role + HTTP method (`null` for all methods) + URL regex (`type` API or UI). `AuthorizingFilter` (last Spring Security filter) matches request path+method against a JCache-cached structure built by `AuthorizationResource`. API patterns apply to the decoded path without the context path (such as `rest/system/user`), from its start: `^rest/system/` and `rest/system/` are equivalent, and a pattern never matches in the middle of the path. A role holding the `.*` API pattern for all methods makes its users administrators (`SecurityHelper.ADMIN` authority), required by the `@Secured(SecurityHelper.ADMIN)` endpoints and by API delegation; `RbacUserDetailsService` loads roles (cache `user-details`). Authentication is pre-authenticated header based: a principal header (`SM_UNIVERSALID` in the test contexts) + `x-api-key` via `ApiTokenAuthenticationFilter`. The Spring Security context itself is provided by the consuming application, see [Deployment security requirements](#deployment-security-requirements).

**MFA**: `MfaResource` (`/system/mfa`) manages the devices of the current user — TOTP authenticator applications and passkeys — and verifies codes and assertions. Enforcing the second factor after the primary authentication is the front-end's responsibility; server-side code can also check `MfaResource.getVerifiedDate(login)` (also exposed as `verifiedDate` by `GET /system/mfa`): the last successful verification, kept in the cluster-wide `mfa-verified` cache until the next `POST /system/mfa/login` or 12 hours (`cache.mfa-verified.ttl`). This state is per user, not per session. Passkeys require `ligoj.mfa.rp-id` (and optionally the accepted origins) to be set in production. Code verification is locked after `ligoj.mfa.max-attempts` consecutive failures (default 5), shared by the cluster, until 15 minutes after the last failure (`cache.mfa-attempts.ttl` property, in seconds).

**Plug-in system**: `PluginsClassLoader` scans `${ligoj.home}/plugins/*.jar` (default `~/.ligoj/plugins`), keeps only the newest version per artifact, and loads classes child-first so a plug-in JAR overrides the same plug-in bundled in the application. JAR signatures are optionally verified against a truststore (`ligoj.plugin.signature.*` system properties). Plug-ins implement `FeaturePlugin` (key format `a:b:c`); their installation lifecycle is driven by the consuming application, this project only provides the SPI, the class loader and the `SystemPlugin` entity.

**Configuration/crypto**: properties may be Jasypt-encrypted as `ENC(...)`; password resolved from `app.crypto.password`/`APP_CRYPTO_PASSWORD` or a file (`app.crypto.file`/`APP_CRYPTO_FILE`), see `core-context-common.xml`. `ConfigurationResource` resolves keys from Spring `Environment` first, then the `S_CONFIGURATION` table, cached in JCache.

## Deployment security requirements

`ApiTokenAuthenticationFilter` authenticates requests from headers:

| Request headers | Authenticated user |
|---|---|
| principal header + `x-api-key` | the principal, when the token belongs to this user |
| principal header + `x-api-key` + `x-api-via-user` | the principal, when the token belongs to the `x-api-via-user` user, and this user is an administrator (delegation) |
| principal header only | **the principal, without any check** |

The last row is the SSO mode: an authenticating reverse proxy sets the principal header after its own authentication. It is only safe when:

- the application port is never reachable without going through the proxy (private interface or network, firewall);
- the proxy removes or overwrites the principal header of every client request, so it cannot be forged;
- the proxy removes the `x-api-via-user` and `x-api-local-roles` client headers, unless API delegation is used.

Otherwise, any client sending `SM_UNIVERSALID: admin` is authenticated as `admin`. These requirements apply to every application built on this project.

## Testing

Extend the chain in `bootstrap-business-test` (`org.ligoj.bootstrap` package): `AbstractTest → AbstractDataGeneratorTest → AbstractSecurityTest → AbstractJpaTest → AbstractAppTest → AbstractServerTest` (WireMock on port 8120). `AbstractRestTest` boots the real server for `*IT`. Canonical Spring test setup (see `AbstractBootTest` in bootstrap-business test sources):

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
