/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.ligoj.bootstrap.core.GlobalPropertyUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Optional reader of the master password from a KV secret of an external secret manager exposing the HashiCorp Vault
 * HTTP API, such as OpenBao. Enabled when the {@value #URL} setting is defined.
 * <p>
 * Each setting is the first non-blank value among: the system property, the loaded properties, see
 * {@link GlobalPropertyUtils}, and the environment variable named from the property in upper case with
 * <code>_</code> as separator, such as <code>APP_CRYPTO_VAULT_URL</code>. These values cannot be encrypted.
 * </p>
 * <p>
 * Authentication is the first defined among: {@value #TOKEN}, {@value #TOKEN_FILE}, then the AppRole
 * {@value #ROLE_ID} with {@value #SECRET_ID}. Neither a secret nor a server response is put in a message.
 * </p>
 */
public class VaultPasswordReader {

	/**
	 * Server address setting, such as <code>https://bao.example.com:8200</code>. Enables this reader when defined.
	 */
	public static final String URL = "app.crypto.vault.url";

	/**
	 * Required API path setting of the secret, without <code>/v1/</code>: <code>secret/data/ligoj/master</code> for KV v2,
	 * <code>secret/ligoj/master</code> for KV v1.
	 */
	public static final String PATH = "app.crypto.vault.path";

	/**
	 * Setting of the secret field holding the master password. Default is <code>password</code>.
	 */
	public static final String FIELD = "app.crypto.vault.field";

	/**
	 * Token setting.
	 */
	public static final String TOKEN = "app.crypto.vault.token";

	/**
	 * Setting of the file containing the token.
	 */
	public static final String TOKEN_FILE = "app.crypto.vault.token-file";

	/**
	 * AppRole role identifier setting.
	 */
	public static final String ROLE_ID = "app.crypto.vault.role-id";

	/**
	 * AppRole secret identifier setting.
	 */
	public static final String SECRET_ID = "app.crypto.vault.secret-id";

	/**
	 * Setting of the mount path of the AppRole authentication method. Default is <code>approle</code>.
	 */
	public static final String AUTH_PATH = "app.crypto.vault.auth-path";

	/**
	 * Optional namespace setting.
	 */
	public static final String NAMESPACE = "app.crypto.vault.namespace";

	/**
	 * Setting of the connection and request timeout in seconds. Default is <code>10</code>.
	 */
	public static final String TIMEOUT = "app.crypto.vault.timeout";

	private static final JsonMapper MAPPER = new JsonMapper();

	/**
	 * A failure with a message safe to expose.
	 */
	private static final class Failure extends Exception {

		private Failure(final String message) {
			super(message);
		}
	}

	/**
	 * Environment variable resolver.
	 */
	private final UnaryOperator<String> environment;

	/**
	 * Reader using the environment variables of the system.
	 */
	public VaultPasswordReader() {
		this(System::getenv);
	}

	/**
	 * Reader using the given environment variables.
	 *
	 * @param environment The environment variable resolver.
	 */
	VaultPasswordReader(final UnaryOperator<String> environment) {
		this.environment = environment;
	}

	/**
	 * Indicate the master password must be read from the external secret manager.
	 *
	 * @return <code>true</code> when the {@value #URL} setting is defined.
	 */
	public boolean isEnabled() {
		return getSetting(URL) != null;
	}

	/**
	 * Return the trimmed value of a setting.
	 *
	 * @param property The setting name, as a system property.
	 * @return The first non-blank value among the system property, the loaded properties and the environment
	 * variable. <code>null</code> when not defined.
	 */
	String getSetting(final String property) {
		final var variable = property.toUpperCase(Locale.ENGLISH).replace('.', '_').replace('-', '_');
		return Stream.<UnaryOperator<String>>of(System::getProperty, GlobalPropertyUtils::getProperty, _ -> environment.apply(variable))
				.map(source -> StringUtils.trimToNull(source.apply(property))).filter(Objects::nonNull).findFirst()
				.orElse(null);
	}

	/**
	 * Read the master password from the configured secret.
	 *
	 * @return The trimmed and not blank master password.
	 * @throws EncryptionInitializationException When the password cannot be read for any reason.
	 */
	public String read() {
		final var address = StringUtils.stripEnd(Objects.toString(getSetting(URL), ""), "/");
		final var path = Strings.CS.removeStart(StringUtils.stripStart(Objects.toString(getSetting(PATH), ""), "/"), "v1/");
		final var prefix = "Unable to read the master password from " + address + "/v1/" + path + ": ";
		try {
			return read(address, path);
		} catch (final Failure failure) {
			throw new EncryptionInitializationException(prefix + failure.getMessage());
		} catch (final IllegalArgumentException _) {
			// Such as a token or a namespace refused as header value, the value is not reported
			throw new EncryptionInitializationException(prefix + "invalid setting value");
		} catch (final JacksonException _) {
			throw new EncryptionInitializationException(prefix + "invalid response");
		} catch (final IOException ioe) {
			throw new EncryptionInitializationException(prefix + ioe.getClass().getSimpleName());
		} catch (final InterruptedException _) {
			Thread.currentThread().interrupt();
			throw new EncryptionInitializationException(prefix + "interrupted");
		}
	}

	/**
	 * Authenticate, then read the password from the secret.
	 */
	private String read(final String address, final String path) throws Failure, IOException, InterruptedException {
		if (path.isEmpty()) {
			throw new Failure("'" + PATH + "' is required");
		}
		final var seconds = NumberUtils.toInt(Objects.toString(getSetting(TIMEOUT), "10"), 0);
		if (seconds <= 0) {
			throw new Failure("invalid '" + TIMEOUT + "'");
		}
		final var timeout = Duration.ofSeconds(seconds);
		final var secret = toUri(address + "/v1/" + path);
		try (var client = HttpClient.newBuilder().connectTimeout(timeout).followRedirects(HttpClient.Redirect.NORMAL).build()) {
			final var token = getToken(client, address, timeout);
			final var response = client.send(newRequest(secret, timeout).header("X-Vault-Token", token).GET().build(),
					HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() / 100 != 2) {
				throw new Failure("HTTP " + response.statusCode());
			}

			// KV v2 wraps the fields in an additional "data" object
			final var field = Objects.toString(getSetting(FIELD), "password");
			final var data = MAPPER.readTree(response.body()).path("data");
			final var password = ObjectUtils.getIfNull(toText(data.path("data").path(field)), () -> toText(data.path(field)));
			if (password == null) {
				throw new Failure("field '" + field + "' not found");
			}
			return password;
		}
	}

	/**
	 * Return the token from the settings: a token, the content of a file, or an AppRole login.
	 */
	private String getToken(final HttpClient client, final String address, final Duration timeout)
			throws Failure, IOException, InterruptedException {
		final var token = getSetting(TOKEN);
		if (token != null) {
			return token;
		}
		final var file = getSetting(TOKEN_FILE);
		if (file != null) {
			return readTokenFile(file);
		}
		final var roleId = getSetting(ROLE_ID);
		final var secretId = getSetting(SECRET_ID);
		if (roleId == null || secretId == null) {
			throw new Failure("no authentication configured");
		}

		// AppRole login
		final var login = toUri(address + "/v1/auth/" + Objects.toString(getSetting(AUTH_PATH), "approle") + "/login");
		final var body = MAPPER.writeValueAsString(Map.of("role_id", roleId, "secret_id", secretId));
		final var response = client.send(newRequest(login, timeout).header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() / 100 != 2) {
			throw new Failure("authentication failed, HTTP " + response.statusCode());
		}
		final var clientToken = toText(MAPPER.readTree(response.body()).path("auth").path("client_token"));
		if (clientToken == null) {
			throw new Failure("authentication failed, no token returned");
		}
		return clientToken;
	}

	/**
	 * Return the trimmed and not blank content of the token file.
	 */
	private String readTokenFile(final String file) throws Failure {
		String token = null;
		try {
			token = StringUtils.trimToNull(Files.readString(Path.of(file)));
		} catch (final IOException | InvalidPathException _) {
			// Reported as an empty file, without detail
		}
		if (token == null) {
			throw new Failure("unable to read the token file");
		}
		return token;
	}

	/**
	 * Return the HTTP or HTTPS URI of the given location.
	 */
	private static URI toUri(final String location) throws Failure {
		try {
			final var uri = URI.create(location);
			if (uri.getHost() != null && Strings.CI.startsWith(uri.getScheme(), "http")) {
				return uri;
			}
		} catch (final IllegalArgumentException _) {
			// Reported as an invalid address
		}
		throw new Failure("invalid address");
	}

	/**
	 * New request builder with the timeout and the optional namespace.
	 */
	private HttpRequest.Builder newRequest(final URI uri, final Duration timeout) {
		final var builder = HttpRequest.newBuilder(uri).timeout(timeout);
		final var namespace = getSetting(NAMESPACE);
		if (namespace != null) {
			builder.header("X-Vault-Namespace", namespace);
		}
		return builder;
	}

	/**
	 * Return the trimmed text of a scalar node. <code>null</code> for a blank, <code>null</code>, missing or
	 * structured node.
	 */
	private static String toText(final JsonNode node) {
		return node.isValueNode() && !node.isNull() ? StringUtils.trimToNull(node.asString()) : null;
	}
}
