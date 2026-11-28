# Jasypt Master Password From OpenBao Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Optionally read the Jasypt master password from one field of an OpenBao / HashiCorp Vault KV secret at startup.

**Architecture:** A new `VaultPasswordStore` in bootstrap-core resolves its settings, authenticates (token, token file or AppRole) and reads the secret over the HTTP API. `SystemEnvironmentAndFilePBEConfig` asks it first and keeps its current chain when the reader is not enabled. Nothing else changes: `CryptoHelper` and the Spring XML contexts are untouched.

**Tech Stack:** Java 25, `java.net.http.HttpClient`, Jackson 3 (`tools.jackson.databind`), Jasypt, JUnit 5, JDK `com.sun.net.httpserver.HttpServer` for tests.

**Spec:** `docs/superpowers/specs/2026-11-28-openbao-master-password-design.md`

## Global Constraints

- No new dependency in any POM.
- Never put the password, the token or the AppRole secret identifier in a log or an exception message, at any level. Never put a server response body in them either.
- Fail closed: once `app.crypto.vault.url` is defined, any failure throws; no fallback to another password source.
- TLS uses the JVM trust store; no option disables the verification.
- Without `app.crypto.vault.url`, the behaviour is byte-for-byte the current one.
- Project conventions: MIT header comment on every Java file, tabs, Lombok, `final var` locals, Javadoc on public members, 100% coverage of new branches.
- Test failures do not fail the Maven build: read the `Tests run: … Failures: … Errors: …` line after every run.
- Tests must restore every system property they set (`@AfterEach`), and must bind the HTTP server on port `0` (ephemeral).
- Work on a branch `feat/openbao-master-password`, not on `master`. Commit messages follow Conventional Commits.

## Review Focus

1. Address with a trailing `/`, or path with a leading `/` or a leading `v1/`: the request URL is still `<url>/v1/<path>` with single slashes.
2. A setting defined but blank (`APP_CRYPTO_VAULT_URL=` in a container manifest): treated as not defined, so the source stays disabled and the current chain applies.
3. A standby node answering `307` with a `Location`: the redirect is followed and the password is returned.
4. A field that is not a string: a number or boolean is returned as its text; `null`, an object or an array fails as a missing field.
5. A non-numeric or non-positive `app.crypto.vault.timeout`: fails with a message naming the setting, instead of a `NumberFormatException` or an infinite wait.

Each of these has a test in Task 1.

---

### Task 1: `VaultPasswordStore`

**Files:**
- Create: `bootstrap-core/src/main/java/org/ligoj/bootstrap/core/crypto/VaultPasswordStore.java`
- Test: `bootstrap-core/src/test/java/org/ligoj/bootstrap/core/crypto/VaultPasswordStoreTest.java`

**Interfaces:**
- Consumes: `GlobalPropertyUtils.getProperty(String)` (static, bootstrap-core), `org.jasypt.exceptions.EncryptionInitializationException`.
- Produces:

```java
public class VaultPasswordStore {
	public static final String URL = "app.crypto.vault.url";
	public static final String PATH = "app.crypto.vault.path";
	public static final String FIELD = "app.crypto.vault.field";           // default "password"
	public static final String TOKEN = "app.crypto.vault.token";
	public static final String TOKEN_FILE = "app.crypto.vault.token-file";
	public static final String ROLE_ID = "app.crypto.vault.role-id";
	public static final String SECRET_ID = "app.crypto.vault.secret-id";
	public static final String AUTH_PATH = "app.crypto.vault.auth-path";   // default "approle"
	public static final String NAMESPACE = "app.crypto.vault.namespace";
	public static final String TIMEOUT = "app.crypto.vault.timeout";       // seconds, default "10"

	public VaultPasswordStore();                                  // environment = System::getenv
	VaultPasswordStore(UnaryOperator<String> environment);        // package-private, for tests
	public boolean isEnabled();                                    // URL setting is not blank
	public String read();                                          // throws EncryptionInitializationException
	String getSetting(String property);                            // package-private, null when not defined or blank
}
```

**Decisions the tests rely on:**
- `getSetting(p)`: first non-blank among `System.getProperty(p)`, `GlobalPropertyUtils.getProperty(p)`, `environment.apply(env)`, where `env` is `p` upper-cased with `.` and `-` replaced by `_` (`app.crypto.vault.token-file` → `APP_CRYPTO_VAULT_TOKEN_FILE`). Values are trimmed.
- Authentication, first defined: `TOKEN`; `TOKEN_FILE` (file content, trimmed); `ROLE_ID` with `SECRET_ID` → `POST <url>/v1/auth/<auth-path>/login`, JSON body `{"role_id":…,"secret_id":…}`, token = `auth.client_token`.
- Read: `GET <url>/v1/<path>`, header `X-Vault-Token`, plus `X-Vault-Namespace` on both requests when `NAMESPACE` is defined. Field looked up in `data.data`, then in `data`. Returned value is trimmed.
- `HttpClient` built with `followRedirects(NORMAL)` and the timeout as connect timeout; each request carries the same timeout.
- Every failure throws `EncryptionInitializationException` with the message `Unable to read the master password from <url>/v1/<path>: <cause>`. Causes, exact text:
  - `'app.crypto.vault.path' is required`
  - `no authentication configured`
  - `unable to read the token file` (no file name in the message is required, no content ever)
  - `invalid 'app.crypto.vault.timeout'`
  - `HTTP <status>` for a non 2xx read; `authentication failed, HTTP <status>` for a non 2xx login
  - `authentication failed, no token returned`
  - `invalid response` for unparsable JSON
  - `field '<field>' not found` for a missing, `null`, blank, object or array field
  - `<exception simple class name>` for an `IOException` (connection refused, timeout)
  - `interrupted`, after re-setting the thread interrupt flag

- [ ] **Step 1: Write the failing tests**

Test class setup: an `HttpServer` on `new InetSocketAddress("localhost", 0)` started in `@BeforeEach`, stopped in `@AfterEach`; a list recording `method path`, and the `X-Vault-Token` / `X-Vault-Namespace` headers of each request; a helper `stub(path, status, body)`; a `Map<String, String> env` passed as `new VaultPasswordStore(env::get)`; `@AfterEach` clears every `app.crypto.vault.*` system property. `url` is `http://localhost:<port>`.

Tests, each asserting the listed result:

```java
// Settings
disabledByDefault()            // no setting: isEnabled() false
blankUrlIsDisabled()           // env APP_CRYPTO_VAULT_URL = "  ": isEnabled() false
settingPrecedence()            // system property "a" and env "b" for TOKEN: getSetting(TOKEN) == "a"; only env: "b", trimmed
settingEnvironmentName()       // env APP_CRYPTO_VAULT_TOKEN_FILE = "f": getSetting(TOKEN_FILE) == "f"
defaultConstructor()           // new VaultPasswordStore().isEnabled() false

// Nominal
readKv2WithToken()             // PATH secret/data/ligoj, body {"data":{"data":{"password":" s3cret "}}}: "s3cret";
                               // recorded: "GET /v1/secret/data/ligoj", X-Vault-Token "t0ken", no namespace header
readKv1()                      // PATH secret/ligoj, body {"data":{"password":"s3cret"}}: "s3cret"
readCustomField()              // FIELD master, body {"data":{"data":{"master":"m"}}}: "m"
readWithTokenFile(@TempDir)    // file content "t0ken\n": X-Vault-Token "t0ken"
readWithAppRole()              // login stub {"auth":{"client_token":"t0ken"}}: recorded
                               // "POST /v1/auth/approle/login" with body containing "role_id":"r" and "secret_id":"s",
                               // then GET with X-Vault-Token "t0ken"
readWithAppRoleCustomMount()   // AUTH_PATH custom: "POST /v1/auth/custom/login"
readWithNamespace()            // NAMESPACE team: header "team" on login and read
readNormalizedUrl()            // URL url + "/", PATH "/v1/secret/data/ligoj": "GET /v1/secret/data/ligoj"
readFollowsRedirect()          // /v1/secret/data/a answers 307 Location /v1/secret/data/b: password of b
readNumericField()             // {"data":{"data":{"password":1234}}}: "1234"

// Failures: assertThrows(EncryptionInitializationException.class, reader::read), message ends with the cause
failMissingPath()              // "'app.crypto.vault.path' is required"
failNoAuthentication()         // "no authentication configured"
failRoleWithoutSecret()        // ROLE_ID only: "no authentication configured"
failTokenFile()                // missing file: "unable to read the token file"
failTimeoutNotNumber()         // TIMEOUT abc: "invalid 'app.crypto.vault.timeout'"
failTimeoutNotPositive()       // TIMEOUT 0: same
failForbidden()                // 403 with body {"errors":["permission denied"]}: "HTTP 403",
                               // and the message does not contain "permission denied"
failLogin()                    // login 400: "authentication failed, HTTP 400"
failLoginNoToken()             // login 200 {"auth":{}}: "authentication failed, no token returned"
failInvalidJson()              // body "<html>": "invalid response"
failMissingField()             // {"data":{"data":{"other":"x"}}}: "field 'password' not found"
failNullField()                // {"data":{"data":{"password":null}}}: same
failObjectField()              // {"data":{"data":{"password":{}}}}: same
failBlankField()               // {"data":{"data":{"password":" "}}}: same
failNoData()                   // {}: same
failUnreachable()              // server stopped before read: message ends with an exception simple name,
                               // and starts with "Unable to read the master password from http://localhost:"
failSlowServer()               // TIMEOUT 1, handler sleeping 3s: throws in less than 3s
failInterrupted()              // Thread.currentThread().interrupt() before read: "interrupted",
                               // Thread.interrupted() is true afterwards
failNeverLeaksSecrets()        // AppRole, read answers 500: message contains neither "t0ken" nor the secret id "s3cr3t-id"
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -pl bootstrap-core -Dtest=VaultPasswordStoreTest`
Expected: compilation error, `VaultPasswordStore` not found.

- [ ] **Step 3: Implement `VaultPasswordStore`**

Per the Interfaces and Decisions blocks. Parse JSON with a `tools.jackson.databind.json.JsonMapper` and `JsonNode.path(...)`; build the login body with the mapper, not by string concatenation.

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -pl bootstrap-core -Dtest=VaultPasswordStoreTest`
Expected: `Tests run: 34, Failures: 0, Errors: 0, Skipped: 0`.

- [ ] **Step 5: Commit**

```bash
git add bootstrap-core/src/main/java/org/ligoj/bootstrap/core/crypto/VaultPasswordStore.java bootstrap-core/src/test/java/org/ligoj/bootstrap/core/crypto/VaultPasswordStoreTest.java
git commit -m "feat(crypto): add OpenBao reader of the Jasypt master password"
```

---

### Task 2: Use the reader in `SystemEnvironmentAndFilePBEConfig`

**Files:**
- Modify: `bootstrap-core/src/main/java/org/ligoj/bootstrap/core/crypto/SystemEnvironmentAndFilePBEConfig.java` (class Javadoc, new field, `getPasswordCharArray()`)
- Test: `bootstrap-core/src/test/java/org/ligoj/bootstrap/core/crypto/SystemEnvironmentAndFilePBEConfigVaultTest.java` (new, plain JUnit, no Spring context)

**Interfaces:**
- Consumes: `VaultPasswordStore.isEnabled()`, `VaultPasswordStore.read()`, and the `URL`, `PATH`, `TOKEN` constants of Task 1.
- Produces: a Lombok-settable field `private VaultPasswordStore vaultPasswordStore = new VaultPasswordStore();` (setter `setVaultPasswordStore`, generated by the class-level `@Setter`). No change of `core-context-common.xml`.

**Decisions:**
- In `getPasswordCharArray()`: when `vaultPasswordStore.isEnabled()`, the password is `read()` on the first call only, then kept in a field and reused; the existing chain is skipped entirely. Otherwise the existing code runs unchanged.
- On that first call, when any of the six existing sources is defined (system property or `GlobalPropertyUtils` or environment variable, for the password and for the file name), log one warning: `OpenBao is the only source of the master password, ignored settings: <names>` with the setting names only.

- [ ] **Step 1: Write the failing tests**

Setup: the same `HttpServer` fixture as Task 1 answering `{"data":{"data":{"password":"fromVault"}}}` on `/v1/secret/data/ligoj` and counting requests; a `SystemEnvironmentAndFilePBEConfig` with `setPasswordSysPropertyName("app.crypto.password")`, `setPasswordFilePropertyName("app.crypto.file")`, `setPasswordEnvName("APP_CRYPTO_PASSWORD")`, `setPasswordFileEnvName("APP_CRYPTO_FILE")`; `@AfterEach` clears `app.crypto.vault.*` and `app.crypto.password`, and restores the previous value of `app.crypto.file` (other tests of the module set it).

```java
vaultDisabled()        // no vault setting, system property app.crypto.password = "local": getPassword() == "local", 0 request
vaultEnabled()         // URL, PATH, TOKEN set: getPassword() == "fromVault"
vaultIsExclusive()     // same, plus app.crypto.password = "local": getPassword() == "fromVault"
vaultReadOnce()        // getPassword() twice, getPasswordCharArray() once: 1 request, same value
vaultFailsClosed()     // server answers 500, app.crypto.password = "local":
                       // assertThrows(EncryptionInitializationException.class, config::getPassword)
vaultCustomReader()    // setVaultPasswordStore(mock: isEnabled() true, read() "mocked"): getPassword() == "mocked"
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -pl bootstrap-core -Dtest=SystemEnvironmentAndFilePBEConfigVaultTest`
Expected: compilation error on `setVaultPasswordStore`, or `vaultEnabled` failing.

- [ ] **Step 3: Implement the change**

Per the Decisions block. Add OpenBao as the first item of the class Javadoc list, stating it is exclusive when `app.crypto.vault.url` is defined.

- [ ] **Step 4: Run the module tests**

Run: `mvn test -pl bootstrap-core`
Expected: `Failures: 0, Errors: 0` on the final `Tests run:` line; `SystemEnvironmentAndFilePBEConfigTest` and `SystemEnvironmentAndFilePBEConfigResourceTest` still pass.

- [ ] **Step 5: Check the coverage of the two classes**

Run: `mvn clean package -pl bootstrap-core -Pjacoco -Djacoco.includes="org.ligoj.bootstrap.*"`, then open `bootstrap-core/target/site/jacoco/org.ligoj.bootstrap.core.crypto/index.html`.
Expected: `VaultPasswordStore` and `SystemEnvironmentAndFilePBEConfig` at 100% instructions and branches. Add the missing test otherwise.

- [ ] **Step 6: Commit**

```bash
git add bootstrap-core/src/main/java/org/ligoj/bootstrap/core/crypto/SystemEnvironmentAndFilePBEConfig.java bootstrap-core/src/test/java/org/ligoj/bootstrap/core/crypto/SystemEnvironmentAndFilePBEConfigVaultTest.java
git commit -m "feat(crypto): source the Jasypt master password from OpenBao when configured"
```

---

### Task 3: Documentation

**Files:**
- Modify: `README.md` (key features list, "Configuration/crypto" paragraph, "Configuration reference" table)
- Modify: `CLAUDE.md` ("Configuration/crypto" paragraph; this file is git-ignored, the edit stays local)

- [ ] **Step 1: Update `README.md`**

- "Configuration/crypto" paragraph: state that when `app.crypto.vault.url` is defined, the password is read once at startup from OpenBao or HashiCorp Vault, is the only source, and that any failure stops the startup. Then the existing order.
- Same paragraph, the alternative without these settings: an OpenBao Agent template rendering the secret to a file, with `APP_CRYPTO_FILE` naming this file.
- "Configuration reference" table: one row per setting of the spec's Settings table (same keys, defaults and usages), source `system, properties, env (APP_CRYPTO_VAULT_…)`, placed after the `app.crypto.password` row. State that these values cannot be `ENC(...)`.
- Same table: add the missing row `ligoj.plugin.dependency.check` | `true` | system | `false`: a plug-in with a missing parent plug-in is still loaded.

- [ ] **Step 2: Update `CLAUDE.md`**

In "Configuration/crypto", one sentence: the master password comes from `SystemEnvironmentAndFilePBEConfig`, with `VaultPasswordStore` (OpenBao, `app.crypto.vault.*`) exclusive and fail-closed when its URL is defined.

- [ ] **Step 3: Verify**

Run: `grep -c 'app.crypto.vault' README.md`
Expected: at least 9 (one per setting key, `role-id` and `secret-id` sharing a row).

- [ ] **Step 4: Commit**

```bash
git add README.md docs/superpowers
git commit -m "docs(crypto): document the OpenBao source of the master password"
```
