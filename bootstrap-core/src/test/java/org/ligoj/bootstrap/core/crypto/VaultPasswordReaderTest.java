/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Test class of {@link VaultPasswordReader}
 */
class VaultPasswordReaderTest {

	private static final String KV2_PATH = "secret/data/ligoj";
	private static final String KV2_BODY = "{\"data\":{\"data\":{\"password\":\"s3cret\"}}}";
	private static final String LOGIN_BODY = "{\"auth\":{\"client_token\":\"t0ken\"}}";
	private static final String MESSAGE_PREFIX = "Unable to read the master password from http://localhost:";

	/**
	 * A stubbed response.
	 */
	private record Stub(int status, String body, String location, long delay) {
	}

	private HttpServer server;
	private ExecutorService executor;
	private String url;
	private final Map<String, Stub> stubs = new ConcurrentHashMap<>();
	private final List<String> requests = new CopyOnWriteArrayList<>();
	private final List<String> tokens = new CopyOnWriteArrayList<>();
	private final List<String> namespaces = new CopyOnWriteArrayList<>();
	private final List<String> bodies = new CopyOnWriteArrayList<>();
	private final Map<String, String> env = new HashMap<>();
	private VaultPasswordReader reader;

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		executor = Executors.newCachedThreadPool();
		server.setExecutor(executor);
		server.createContext("/", this::handle);
		server.start();
		url = "http://localhost:" + server.getAddress().getPort();
		reader = new VaultPasswordReader(env::get);
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
		executor.shutdownNow();
		System.getProperties().stringPropertyNames().stream().filter(p -> p.startsWith("app.crypto.vault."))
				.forEach(System::clearProperty);
	}

	private void handle(final HttpExchange exchange) throws IOException {
		final var path = exchange.getRequestURI().getPath();
		requests.add(exchange.getRequestMethod() + " " + path);
		tokens.add(String.valueOf(exchange.getRequestHeaders().getFirst("X-Vault-Token")));
		namespaces.add(String.valueOf(exchange.getRequestHeaders().getFirst("X-Vault-Namespace")));
		bodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
		final var stub = stubs.getOrDefault(path, new Stub(404, "{\"errors\":[]}", null, 0));
		try {
			Thread.sleep(stub.delay());
		} catch (final InterruptedException _) {
			Thread.currentThread().interrupt();
		}
		if (stub.location() != null) {
			exchange.getResponseHeaders().add("Location", stub.location());
		}
		final var bytes = stub.body().getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(stub.status(), bytes.length == 0 ? -1 : bytes.length);
		if (bytes.length > 0) {
			exchange.getResponseBody().write(bytes);
		}
		exchange.close();
	}

	private void stub(final String path, final int status, final String body) {
		stubs.put(path, new Stub(status, body, null, 0));
	}

	/**
	 * Configure the address, the KV v2 path and a token, and stub the given secret response.
	 */
	private void tokenSetup(final int status, final String body) {
		env.put("APP_CRYPTO_VAULT_URL", url);
		env.put("APP_CRYPTO_VAULT_PATH", KV2_PATH);
		env.put("APP_CRYPTO_VAULT_TOKEN", "t0ken");
		stub("/v1/" + KV2_PATH, status, body);
	}

	/**
	 * Configure the address, the KV v2 path and an AppRole, and stub the nominal responses.
	 */
	private void appRoleSetup() {
		env.put("APP_CRYPTO_VAULT_URL", url);
		env.put("APP_CRYPTO_VAULT_PATH", KV2_PATH);
		env.put("APP_CRYPTO_VAULT_ROLE_ID", "r0le");
		env.put("APP_CRYPTO_VAULT_SECRET_ID", "s3cr3t-id");
		stub("/v1/auth/approle/login", 200, LOGIN_BODY);
		stub("/v1/" + KV2_PATH, 200, KV2_BODY);
	}

	private String assertFail(final String cause) {
		final var message = Assertions.assertThrows(EncryptionInitializationException.class, reader::read).getMessage();
		Assertions.assertTrue(message.startsWith(MESSAGE_PREFIX), message);
		Assertions.assertTrue(message.endsWith(": " + cause), message);
		return message;
	}

	// Settings

	@Test
	void disabledByDefault() {
		Assertions.assertFalse(reader.isEnabled());
	}

	@Test
	void blankUrlIsDisabled() {
		env.put("APP_CRYPTO_VAULT_URL", "  ");
		Assertions.assertFalse(reader.isEnabled());
	}

	@Test
	void settingPrecedence() {
		env.put("APP_CRYPTO_VAULT_TOKEN", " b ");
		Assertions.assertEquals("b", reader.getSetting(VaultPasswordReader.TOKEN));
		System.setProperty(VaultPasswordReader.TOKEN, " a ");
		Assertions.assertEquals("a", reader.getSetting(VaultPasswordReader.TOKEN));
	}

	@Test
	void settingEnvironmentName() {
		env.put("APP_CRYPTO_VAULT_TOKEN_FILE", "f");
		Assertions.assertEquals("f", reader.getSetting(VaultPasswordReader.TOKEN_FILE));
		Assertions.assertNull(reader.getSetting(VaultPasswordReader.TOKEN));
	}

	@Test
	void defaultConstructor() {
		Assertions.assertFalse(new VaultPasswordReader().isEnabled());
	}

	// Nominal

	@Test
	void readKv2WithToken() {
		tokenSetup(200, "{\"data\":{\"data\":{\"password\":\" s3cret \"}}}");
		Assertions.assertTrue(reader.isEnabled());
		Assertions.assertEquals("s3cret", reader.read());
		Assertions.assertEquals(List.of("GET /v1/secret/data/ligoj"), requests);
		Assertions.assertEquals(List.of("t0ken"), tokens);
		Assertions.assertEquals(List.of("null"), namespaces);
	}

	@Test
	void readKv1() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_PATH", "secret/ligoj");
		stub("/v1/secret/ligoj", 200, "{\"data\":{\"password\":\"kv1\"}}");
		Assertions.assertEquals("kv1", reader.read());
	}

	@Test
	void readCustomField() {
		tokenSetup(200, "{\"data\":{\"data\":{\"master\":\"m\",\"password\":\"p\"}}}");
		env.put("APP_CRYPTO_VAULT_FIELD", "master");
		Assertions.assertEquals("m", reader.read());
	}

	@Test
	void readWithTokenFile(@TempDir final Path directory) throws IOException {
		tokenSetup(200, KV2_BODY);
		env.remove("APP_CRYPTO_VAULT_TOKEN");
		final var file = directory.resolve("token");
		Files.writeString(file, "f1le-t0ken\n");
		env.put("APP_CRYPTO_VAULT_TOKEN_FILE", file.toString());
		Assertions.assertEquals("s3cret", reader.read());
		Assertions.assertEquals(List.of("f1le-t0ken"), tokens);
	}

	@Test
	void readWithAppRole() {
		appRoleSetup();
		Assertions.assertEquals("s3cret", reader.read());
		Assertions.assertEquals(List.of("POST /v1/auth/approle/login", "GET /v1/secret/data/ligoj"), requests);
		Assertions.assertTrue(bodies.getFirst().contains("\"role_id\":\"r0le\""), bodies.getFirst());
		Assertions.assertTrue(bodies.getFirst().contains("\"secret_id\":\"s3cr3t-id\""), bodies.getFirst());
		Assertions.assertEquals(List.of("null", "t0ken"), tokens);
	}

	@Test
	void readWithAppRoleCustomMount() {
		appRoleSetup();
		env.put("APP_CRYPTO_VAULT_AUTH_PATH", "custom");
		stub("/v1/auth/custom/login", 200, LOGIN_BODY);
		Assertions.assertEquals("s3cret", reader.read());
		Assertions.assertEquals("POST /v1/auth/custom/login", requests.getFirst());
	}

	@Test
	void readWithNamespace() {
		appRoleSetup();
		env.put("APP_CRYPTO_VAULT_NAMESPACE", "team");
		Assertions.assertEquals("s3cret", reader.read());
		Assertions.assertEquals(List.of("team", "team"), namespaces);
	}

	@Test
	void readNormalizedUrl() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_URL", url + "/");
		env.put("APP_CRYPTO_VAULT_PATH", "/v1/" + KV2_PATH);
		Assertions.assertEquals("s3cret", reader.read());
		Assertions.assertEquals(List.of("GET /v1/secret/data/ligoj"), requests);
	}

	@Test
	void readFollowsRedirect() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_PATH", "secret/data/a");
		stubs.put("/v1/secret/data/a", new Stub(307, "", "/v1/" + KV2_PATH, 0));
		Assertions.assertEquals("s3cret", reader.read());
		Assertions.assertEquals(List.of("GET /v1/secret/data/a", "GET /v1/secret/data/ligoj"), requests);
		Assertions.assertEquals(List.of("t0ken", "t0ken"), tokens);
	}

	@Test
	void readNumericField() {
		tokenSetup(200, "{\"data\":{\"data\":{\"password\":1234}}}");
		Assertions.assertEquals("1234", reader.read());
	}

	// Failures

	@Test
	void failMissingPath() {
		tokenSetup(200, KV2_BODY);
		env.remove("APP_CRYPTO_VAULT_PATH");
		assertFail("'app.crypto.vault.path' is required");
		Assertions.assertTrue(requests.isEmpty());
	}

	@Test
	void failNoAuthentication() {
		tokenSetup(200, KV2_BODY);
		env.remove("APP_CRYPTO_VAULT_TOKEN");
		assertFail("no authentication configured");
	}

	@Test
	void failRoleWithoutSecret() {
		appRoleSetup();
		env.remove("APP_CRYPTO_VAULT_SECRET_ID");
		assertFail("no authentication configured");
		Assertions.assertTrue(requests.isEmpty());
	}

	@Test
	void failSecretWithoutRole() {
		appRoleSetup();
		env.remove("APP_CRYPTO_VAULT_ROLE_ID");
		assertFail("no authentication configured");
	}

	@Test
	void failTokenFile() {
		tokenSetup(200, KV2_BODY);
		env.remove("APP_CRYPTO_VAULT_TOKEN");
		env.put("APP_CRYPTO_VAULT_TOKEN_FILE", "target/not-found/token");
		assertFail("unable to read the token file");
	}

	@Test
	void failEmptyTokenFile(@TempDir final Path directory) throws IOException {
		tokenSetup(200, KV2_BODY);
		env.remove("APP_CRYPTO_VAULT_TOKEN");
		final var file = directory.resolve("token");
		Files.writeString(file, " \n");
		env.put("APP_CRYPTO_VAULT_TOKEN_FILE", file.toString());
		assertFail("unable to read the token file");
	}

	@Test
	void failTimeoutNotNumber() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_TIMEOUT", "abc");
		assertFail("invalid 'app.crypto.vault.timeout'");
	}

	@Test
	void failTimeoutNotPositive() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_TIMEOUT", "0");
		assertFail("invalid 'app.crypto.vault.timeout'");
	}

	@Test
	void failInvalidUrl() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_URL", "localhost:" + server.getAddress().getPort());
		final var message = Assertions.assertThrows(EncryptionInitializationException.class, reader::read).getMessage();
		Assertions.assertTrue(message.endsWith(": invalid address"), message);
	}

	@Test
	void failUnsupportedScheme() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_URL", "ftp://localhost:" + server.getAddress().getPort());
		final var message = Assertions.assertThrows(EncryptionInitializationException.class, reader::read).getMessage();
		Assertions.assertTrue(message.endsWith(": invalid address"), message);
	}

	@Test
	void failNoUrl() {
		tokenSetup(200, KV2_BODY);
		env.remove("APP_CRYPTO_VAULT_URL");
		final var message = Assertions.assertThrows(EncryptionInitializationException.class, reader::read).getMessage();
		Assertions.assertTrue(message.endsWith(": invalid address"), message);
	}

	@Test
	void failInvalidPath() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_PATH", "secret/data/my secret");
		assertFail("invalid address");
	}

	@Test
	void failInvalidToken() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_TOKEN", "t0\nken");
		final var message = assertFail("invalid setting value");
		Assertions.assertFalse(message.contains("ken"));
		Assertions.assertTrue(requests.isEmpty());
	}

	@Test
	void failInvalidTokenFilePath() {
		tokenSetup(200, KV2_BODY);
		env.remove("APP_CRYPTO_VAULT_TOKEN");
		env.put("APP_CRYPTO_VAULT_TOKEN_FILE", "tok\u0000en");
		assertFail("unable to read the token file");
	}

	@Test
	void readBooleanField() {
		tokenSetup(200, "{\"data\":{\"data\":{\"password\":true}}}");
		Assertions.assertEquals("true", reader.read());
	}

	@Test
	void failArrayField() {
		tokenSetup(200, "{\"data\":{\"data\":{\"password\":[\"a\"]}}}");
		assertFail("field 'password' not found");
	}

	@Test
	void failForbidden() {
		tokenSetup(403, "{\"errors\":[\"permission denied\"]}");
		final var message = assertFail("HTTP 403");
		Assertions.assertFalse(message.contains("permission denied"));
		Assertions.assertEquals(MESSAGE_PREFIX + server.getAddress().getPort() + "/v1/secret/data/ligoj: HTTP 403", message);
	}

	@Test
	void failLogin() {
		appRoleSetup();
		stub("/v1/auth/approle/login", 400, "{\"errors\":[\"invalid role or secret ID\"]}");
		assertFail("authentication failed, HTTP 400");
	}

	@Test
	void failLoginNoToken() {
		appRoleSetup();
		stub("/v1/auth/approle/login", 200, "{\"auth\":{}}");
		assertFail("authentication failed, no token returned");
	}

	@Test
	void failLoginInvalidJson() {
		appRoleSetup();
		stub("/v1/auth/approle/login", 200, "<html>");
		assertFail("invalid response");
	}

	@Test
	void failInvalidJson() {
		tokenSetup(200, "<html>");
		assertFail("invalid response");
	}

	@Test
	void failMissingField() {
		tokenSetup(200, "{\"data\":{\"data\":{\"other\":\"x\"}}}");
		assertFail("field 'password' not found");
	}

	@Test
	void failNullField() {
		tokenSetup(200, "{\"data\":{\"data\":{\"password\":null}}}");
		assertFail("field 'password' not found");
	}

	@Test
	void failObjectField() {
		tokenSetup(200, "{\"data\":{\"data\":{\"password\":{}}}}");
		assertFail("field 'password' not found");
	}

	@Test
	void failBlankField() {
		tokenSetup(200, "{\"data\":{\"data\":{\"password\":\" \"}}}");
		assertFail("field 'password' not found");
	}

	@Test
	void failNoData() {
		tokenSetup(200, "{}");
		assertFail("field 'password' not found");
	}

	@Test
	void failEmptyBody() {
		tokenSetup(200, "");
		assertFail("field 'password' not found");
	}

	@Test
	void failUnreachable() {
		tokenSetup(200, KV2_BODY);
		server.stop(0);
		final var message = Assertions.assertThrows(EncryptionInitializationException.class, reader::read).getMessage();
		Assertions.assertTrue(message.startsWith(MESSAGE_PREFIX), message);
		Assertions.assertTrue(message.endsWith("Exception"), message);
	}

	@Test
	void failSlowServer() {
		tokenSetup(200, KV2_BODY);
		env.put("APP_CRYPTO_VAULT_TIMEOUT", "1");
		stubs.put("/v1/" + KV2_PATH, new Stub(200, KV2_BODY, null, 3000));
		assertFail("HttpTimeoutException");
	}

	@Test
	void failInterrupted() {
		tokenSetup(200, KV2_BODY);
		Thread.currentThread().interrupt();
		try {
			assertFail("interrupted");
		} finally {
			Assertions.assertTrue(Thread.interrupted());
		}
	}

	@Test
	void failNeverLeaksSecrets() {
		appRoleSetup();
		stub("/v1/" + KV2_PATH, 500, "{\"errors\":[\"t0ken s3cr3t-id\"]}");
		final var message = assertFail("HTTP 500");
		Assertions.assertFalse(message.contains("t0ken"));
		Assertions.assertFalse(message.contains("s3cr3t-id"));
	}
}
