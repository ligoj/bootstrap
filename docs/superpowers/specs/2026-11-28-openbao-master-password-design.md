# Jasypt master password sourced from OpenBao — design

Date: 2026-11-28. Size: small change (one new class, one modified class, tests, documentation).

## Intent

Let a deployment keep the Jasypt master password in an external secret manager (OpenBao, or HashiCorp Vault, same HTTP API) instead of a system property, an environment variable or a file. The feature is optional: without its settings, nothing changes.

The master password is resolved in a single place, `SystemEnvironmentAndFilePBEConfig` (bootstrap-core), when the `configurationEncryptor` bean initialises. Adding the source there covers every `ENC(...)` value: the `S_CONFIGURATION` table, the properties files and the MFA device secrets. `CryptoHelper` is not modified.

## Goals

- Read the master password from one field of a KV secret, over the HTTP API, at startup.
- No new dependency for `bootstrap-core` and downstream projects.
- Fail closed when the source is configured but unusable.

## Non-goals

- Storing the configuration values themselves in OpenBao, or using its transit engine.
- Rotation or reload of the password at runtime: it is read once.
- Authentication methods other than token and AppRole (Kubernetes, cloud IAM): later, if needed.
- A generic secret provider SPI.

## Anti-goals

- Falling back silently to another password source when OpenBao is configured and fails: the application would start with a wrong or weaker password.
- Logging the password, the token or the AppRole secret identifier, at any level, including in exception messages.

## Approaches

1. **OpenBao Agent renders a file, `APP_CRYPTO_FILE` points at it.** No code, works today. Needs an agent or sidecar in every deployment, and the password is on disk. Kept as a documented recipe.
2. **Built-in source with the JDK `HttpClient` — chosen.** No dependency, the password never touches the disk. We maintain a small client, and each additional authentication method is extra code.
3. **Built-in source on `spring-vault-core`.** Every authentication method for free, but a new dependency for all downstream projects, used for one read at startup.

Picked by the maintainer: 2, plus the README recipe of 1.

## Design

### Settings

Each setting is resolved like the current password: system property, then the loaded properties files (`GlobalPropertyUtils`), then the environment variable. The values of these settings cannot be `ENC(...)`: they are needed before the encryptor exists.

| System property / properties key | Environment variable | Default | Usage |
|---|---|---|---|
| `app.crypto.vault.url` | `APP_CRYPTO_VAULT_URL` | none | Server address, such as `https://bao.example.com:8200`. Defining it enables the source |
| `app.crypto.vault.path` | `APP_CRYPTO_VAULT_PATH` | required | API path of the secret, without `/v1/`: `secret/data/ligoj` (KV v2) or `secret/ligoj` (KV v1) |
| `app.crypto.vault.field` | `APP_CRYPTO_VAULT_FIELD` | `password` | Field of the secret holding the master password |
| `app.crypto.vault.token` | `APP_CRYPTO_VAULT_TOKEN` | none | Token authentication |
| `app.crypto.vault.token-file` | `APP_CRYPTO_VAULT_TOKEN_FILE` | none | File containing the token, trimmed |
| `app.crypto.vault.role-id`, `app.crypto.vault.secret-id` | `APP_CRYPTO_VAULT_ROLE_ID`, `APP_CRYPTO_VAULT_SECRET_ID` | none | AppRole authentication |
| `app.crypto.vault.auth-path` | `APP_CRYPTO_VAULT_AUTH_PATH` | `approle` | Mount path of the AppRole method |
| `app.crypto.vault.namespace` | `APP_CRYPTO_VAULT_NAMESPACE` | none | Sent as `X-Vault-Namespace` |
| `app.crypto.vault.timeout` | `APP_CRYPTO_VAULT_TIMEOUT` | `10` | Connection and request timeout, in seconds |

Authentication is the first defined among: token, token file, AppRole. TLS relies on the JVM trust store; there is no option to disable the verification.

### Behaviour

- When `app.crypto.vault.url` is not defined: the current resolution order, unchanged.
- When it is defined, OpenBao is the **only** source: the other password settings are ignored, and a warning names the ones that are also defined.
- AppRole: `POST /v1/auth/<auth-path>/login` with `role_id` and `secret_id`, the token is `auth.client_token`.
- Read: `GET /v1/<path>` with `X-Vault-Token`. The field is looked up in `data.data` (KV v2), then in `data` (KV v1). The value is trimmed.
- Any failure stops the startup with an exception naming the address, the path and the cause (HTTP status, missing field, missing authentication, timeout), never a secret: missing `path`, no authentication setting, unreadable token file, non 2xx response, invalid JSON, missing or blank field.
- The password is fetched once per `SystemEnvironmentAndFilePBEConfig` instance and kept: later `getPassword()` calls do not call the server again.

### Code

- New `org.ligoj.bootstrap.core.crypto.VaultPasswordStore` (bootstrap-core): settings resolution, authentication and read, with `java.net.http.HttpClient` and the Jackson 3 mapper already available in the module.
- `SystemEnvironmentAndFilePBEConfig.getPasswordCharArray()` asks the reader first, and keeps the current chain when the source is not enabled. The settings names are fixed constants of the reader: no change of `core-context-common.xml`, so downstream contexts are not impacted.

### Documentation

- README "Configuration/crypto" paragraph and "Configuration reference" table: the settings above.
- README: the OpenBao Agent + `APP_CRYPTO_FILE` recipe, as the alternative without these settings.
- Add the missing `ligoj.plugin.dependency.check` row to the reference table while there.

## Left to the builder

- Internal structure of the reader, and how the password is kept after the first read.
- Test HTTP server in bootstrap-core tests, where WireMock is not available: the JDK `com.sun.net.httpserver.HttpServer` on an ephemeral port is suggested. Coverage stays at 100%: token, token file, AppRole, KV v1 and v2, namespace header, each failure, the "also defined" warning, and the disabled case.
- Tests must not leak system properties between test methods.

## Calls made by the author, to be confirmed

- OpenBao is exclusive when configured, instead of being one more step of the chain.
- KV version detected from the response shape, instead of a version setting.
- Setting names under `app.crypto.vault.*`, as OpenBao and Vault share the API.
- Timeout default of 10 seconds, TLS verification not optional.
