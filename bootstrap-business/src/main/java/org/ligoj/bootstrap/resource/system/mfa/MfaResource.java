package org.ligoj.bootstrap.resource.system.mfa;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.ligoj.bootstrap.core.crypto.WebAuthnHelper;
import tools.jackson.databind.ObjectMapper;

import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.core.crypto.TotpHelper;
import org.ligoj.bootstrap.core.security.SecurityHelper;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import org.ligoj.bootstrap.dao.system.SystemMfaDeviceRepository;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.model.system.SystemMfaDevice;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

/**
 * Multi-factor authentication of the current user: registered devices (TOTP authenticator applications, passkeys),
 * enrollment, default device and verification. The front-end enforces the second factor right after any primary
 * authentication (form, OIDC, ...) when at least one device is registered; this resource only manages the devices
 * and verifies the codes and assertions. Passkey challenges are single-use, per user, kept in memory for
 * {@link #CHALLENGE_TIMEOUT}.
 */
@Path("/system/mfa")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class MfaResource {

	/**
	 * Configuration: issuer shown by the authenticator applications.
	 */
	public static final String CONF_ISSUER = "ligoj.mfa.issuer";

	/**
	 * Default issuer.
	 */
	public static final String DEFAULT_ISSUER = "Ligoj";

	/**
	 * Accepted time steps before and after the current one.
	 */
	static final int WINDOW = 1;

	/**
	 * Configuration: relying party identifier of the passkeys, the site host name. Defaults to <code>localhost</code>:
	 * MUST be set in production, and never changed afterwards (registered passkeys are bound to it).
	 */
	public static final String CONF_RP_ID = "ligoj.mfa.rp-id";

	/**
	 * Configuration: comma separated accepted browser origins of the passkeys. When empty, any HTTPS origin whose host
	 * is the relying party identifier or one of its sub-domains is accepted (HTTP for localhost).
	 */
	public static final String CONF_ORIGINS = "ligoj.mfa.origins";

	/**
	 * Lifetime of a passkey challenge.
	 */
	static final Duration CHALLENGE_TIMEOUT = Duration.ofMinutes(5);

	private static final String CODE_PROPERTY = "code";
	private static final String INVALID_CODE = "invalid-code";
	private static final String PASSKEY_PROPERTY = "passkey";
	private static final String PUBLIC_KEY_TYPE = "public-key";
	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * Pending challenges: user + purpose to the challenge and its expiry.
	 */
	private final Map<String, PendingChallenge> challenges = new ConcurrentHashMap<>();

	private record PendingChallenge(String challenge, Instant expiry) {
	}

	/**
	 * Persisted passkey data (encrypted in the device secret).
	 */
	private record Passkey(String credentialId, String publicKey, int alg, long signCount) {
	}

	@Autowired
	private SystemMfaDeviceRepository repository;

	@Autowired
	private SystemUserRepository userRepository;

	@Autowired
	private SecurityHelper securityHelper;

	@Autowired
	private CryptoHelper cryptoHelper;

	@Autowired
	private ConfigurationResource configuration;

	/**
	 * Return the MFA state of the current user.
	 *
	 * @return The registered devices, whether a verification is required, and the last authentication.
	 */
	@GET
	public MfaStatusVo get() {
		return status(securityHelper.getLogin());
	}

	/**
	 * Record an authentication of the current user (called by the front-end right after the primary authentication)
	 * and return the MFA state: the front-end then requires a verification when a device is registered.
	 *
	 * @return The MFA state.
	 */
	@POST
	@Path("login")
	public MfaStatusVo login() {
		final var login = securityHelper.getLogin();
		var user = userRepository.findOne(login);
		if (user == null) {
			user = new SystemUser();
			user.setLogin(login);
		}
		user.setLastConnection(Instant.now());
		userRepository.saveAndFlush(user);
		final var status = status(login);
		log.info("Authentication of {} recorded, MFA {}", login, status.isRequired() ? "required" : "not registered");
		return status;
	}

	/**
	 * Start a TOTP enrollment: a new secret, and the URI to show as a QR code. Nothing is persisted until
	 * {@link #createTotp(MfaDeviceEditionVo)} confirms it with a first code.
	 *
	 * @return The secret and the URI.
	 */
	@POST
	@Path("totp/setup")
	public TotpSetupVo setupTotp() {
		final var vo = new TotpSetupVo();
		vo.setSecret(TotpHelper.generateSecret());
		vo.setIssuer(configuration.get(CONF_ISSUER, DEFAULT_ISSUER));
		vo.setAccount(securityHelper.getLogin());
		vo.setUri(TotpHelper.toUri(vo.getIssuer(), vo.getAccount(), vo.getSecret()));
		return vo;
	}

	/**
	 * Register a TOTP device: the code proves the authenticator has the secret.
	 *
	 * @param vo The device name, the secret from the setup and the current code.
	 * @return The identifier of the registered device.
	 */
	@POST
	@Path("totp")
	public int createTotp(final MfaDeviceEditionVo vo) {
		final var login = securityHelper.getLogin();
		if (repository.findByUserAndName(login, vo.getName()) != null) {
			throw new ValidationJsonException("name", "already-exist", "0", "name");
		}
		if (!TotpHelper.verify(vo.getSecret(), vo.getCode(), WINDOW)) {
			throw new ValidationJsonException(CODE_PROPERTY, INVALID_CODE);
		}
		final var device = register(login, vo.getName(), SystemMfaDevice.TYPE_TOTP, vo.getSecret());
		return device.getId();
	}

	/**
	 * Start a passkey registration: the creation options for <code>navigator.credentials.create</code>, with a
	 * single-use challenge. Nothing is persisted until {@link #createPasskey(PasskeyRegistrationVo)}.
	 *
	 * @return The <code>PublicKeyCredentialCreationOptions</code>-like map, binary fields Base64url encoded.
	 */
	@POST
	@Path("passkey/setup")
	public Map<String, Object> setupPasskey() {
		final var login = securityHelper.getLogin();
		final var challenge = newChallenge(login, "create");
		final var options = new LinkedHashMap<String, Object>();
		options.put("challenge", challenge);
		options.put("rp", Map.of("id", getRpId(), "name", configuration.get(CONF_ISSUER, DEFAULT_ISSUER)));
		options.put("user", Map.of("id", WebAuthnHelper.base64Url(login.getBytes(StandardCharsets.UTF_8)), "name", login,
				"displayName", login));
		options.put("pubKeyCredParams", List.of(Map.of("type", PUBLIC_KEY_TYPE, "alg", WebAuthnHelper.ALG_ES256),
				Map.of("type", PUBLIC_KEY_TYPE, "alg", WebAuthnHelper.ALG_RS256)));
		options.put("excludeCredentials", passkeys(login).stream()
				.map(p -> Map.of("type", PUBLIC_KEY_TYPE, "id", p.credentialId())).toList());
		options.put("authenticatorSelection", Map.of("residentKey", "preferred", "userVerification", "preferred"));
		options.put("attestation", "none");
		options.put("timeout", CHALLENGE_TIMEOUT.toMillis());
		return options;
	}

	/**
	 * Register a passkey from the credential created by the browser: the challenge, the origin and the relying
	 * party are checked, the attestation statement is not (attestation <code>none</code>).
	 *
	 * @param vo The device name and the created credential.
	 * @return The identifier of the registered device.
	 */
	@POST
	@Path("passkey")
	public int createPasskey(final PasskeyRegistrationVo vo) {
		final var login = securityHelper.getLogin();
		if (repository.findByUserAndName(login, vo.getName()) != null) {
			throw new ValidationJsonException("name", "already-exist", "0", "name");
		}
		checkClientData(login, "create", "webauthn.create", vo.getClientDataJSON());
		final WebAuthnHelper.AuthData authData;
		final WebAuthnHelper.Credential credential;
		try {
			authData = WebAuthnHelper.parseAttestationObject(WebAuthnHelper.base64UrlDecode(vo.getAttestationObject()));
			checkAuthData(authData);
			if (!authData.has(WebAuthnHelper.FLAG_AT) || authData.credentialId() == null) {
				throw new IllegalArgumentException("No attested credential");
			}
			credential = WebAuthnHelper.toCredential(authData.cosePublicKey());
		} catch (final IllegalArgumentException e) {
			log.info("Passkey registration rejected for {}: {}", login, e.getMessage());
			throw new ValidationJsonException(PASSKEY_PROPERTY, INVALID_CODE);
		}
		final var credentialId = WebAuthnHelper.base64Url(authData.credentialId());
		if (!credentialId.equals(vo.getId()) || passkeys(login).stream().anyMatch(p -> p.credentialId().equals(credentialId))) {
			throw new ValidationJsonException(PASSKEY_PROPERTY, INVALID_CODE);
		}
		final var passkey = new Passkey(credentialId, WebAuthnHelper.encodePublicKey(credential.publicKey()),
				credential.alg(), authData.signCount());
		final var device = register(login, vo.getName(), SystemMfaDevice.TYPE_PASSKEY, MAPPER.writeValueAsString(passkey));
		return device.getId();
	}

	/**
	 * Start a passkey verification: the request options for <code>navigator.credentials.get</code>, with a single-use
	 * challenge and the user's passkeys.
	 *
	 * @return The <code>PublicKeyCredentialRequestOptions</code>-like map, binary fields Base64url encoded.
	 */
	@POST
	@Path("passkey/challenge")
	public Map<String, Object> challengePasskey() {
		final var login = securityHelper.getLogin();
		final var options = new LinkedHashMap<String, Object>();
		options.put("challenge", newChallenge(login, "get"));
		options.put("rpId", getRpId());
		options.put("allowCredentials", passkeys(login).stream()
				.map(p -> Map.of("type", PUBLIC_KEY_TYPE, "id", p.credentialId())).toList());
		options.put("userVerification", "preferred");
		options.put("timeout", CHALLENGE_TIMEOUT.toMillis());
		return options;
	}

	/**
	 * Verify a passkey assertion against the user's passkeys: challenge, origin, relying party, user presence,
	 * signature and signature counter. On success, the device records the usage.
	 *
	 * @param vo The assertion returned by the browser.
	 */
	@POST
	@Path("passkey/verify")
	public void verifyPasskey(final PasskeyAssertionVo vo) {
		final var login = securityHelper.getLogin();
		checkClientData(login, "get", "webauthn.get", vo.getClientDataJSON());
		final var device = repository.findAllByUserOrderByName(login).stream()
				.filter(d -> SystemMfaDevice.TYPE_PASSKEY.equals(d.getType()))
				.filter(d -> passkey(d).credentialId().equals(vo.getId())).findFirst()
				.orElseThrow(() -> new ValidationJsonException(PASSKEY_PROPERTY, INVALID_CODE));
		final var passkey = passkey(device);
		final byte[] authDataBytes;
		final WebAuthnHelper.AuthData authData;
		try {
			authDataBytes = WebAuthnHelper.base64UrlDecode(vo.getAuthenticatorData());
			authData = WebAuthnHelper.parseAuthData(authDataBytes);
			checkAuthData(authData);
		} catch (final IllegalArgumentException e) {
			log.info("Passkey assertion rejected for {}: {}", login, e.getMessage());
			throw new ValidationJsonException(PASSKEY_PROPERTY, INVALID_CODE);
		}
		final var valid = WebAuthnHelper.verifySignature(WebAuthnHelper.decodePublicKey(passkey.publicKey(), passkey.alg()),
				passkey.alg(), authDataBytes, WebAuthnHelper.base64UrlDecode(vo.getClientDataJSON()),
				WebAuthnHelper.base64UrlDecode(vo.getSignature()));
		if (!valid) {
			log.info("Passkey signature rejected for {} with device '{}'", login, device.getName());
			throw new ValidationJsonException(PASSKEY_PROPERTY, INVALID_CODE);
		}
		if (passkey.signCount() > 0 && authData.signCount() <= passkey.signCount()) {
			// A cloned authenticator would replay a counter
			log.warn("Passkey counter regression for {} with device '{}': {} <= {}", login, device.getName(),
					authData.signCount(), passkey.signCount());
			throw new ValidationJsonException(PASSKEY_PROPERTY, INVALID_CODE);
		}
		device.setSecret(cryptoHelper.encrypt(MAPPER.writeValueAsString(
				new Passkey(passkey.credentialId(), passkey.publicKey(), passkey.alg(), authData.signCount()))));
		device.setLastUsed(Instant.now());
		repository.saveAndFlush(device);
		log.info("MFA verification succeeded for {} with passkey '{}'", login, device.getName());
	}

	/**
	 * Create and store a single-use challenge for the user and purpose.
	 */
	private String newChallenge(final String login, final String purpose) {
		final var challenge = WebAuthnHelper.base64Url(WebAuthnHelper.randomChallenge());
		challenges.put(login + "#" + purpose, new PendingChallenge(challenge, Instant.now().plus(CHALLENGE_TIMEOUT)));
		return challenge;
	}

	/**
	 * Consume the pending challenge of the user and check the client data against it: type, challenge, origin.
	 */
	private void checkClientData(final String login, final String purpose, final String type, final String clientDataJSON) {
		final var pending = challenges.remove(login + "#" + purpose);
		final Map<String, Object> client;
		try {
			client = WebAuthnHelper.parseClientData(WebAuthnHelper.base64UrlDecode(clientDataJSON));
		} catch (final RuntimeException _) {
			throw new ValidationJsonException(PASSKEY_PROPERTY, INVALID_CODE);
		}
		final var origins = Arrays.stream(StringUtils.defaultString(configuration.get(CONF_ORIGINS)).split(","))
				.map(String::trim).filter(StringUtils::isNotEmpty).toList();
		if (pending == null || pending.expiry().isBefore(Instant.now()) || !type.equals(client.get("type"))
				|| !pending.challenge().equals(client.get("challenge"))
				|| !WebAuthnHelper.isOriginAllowed(Objects.toString(client.get("origin"), null), getRpId(), origins)) {
			log.info("Passkey client data rejected for {} (purpose {}, origin {})", login, purpose, client.get("origin"));
			throw new ValidationJsonException(PASSKEY_PROPERTY, INVALID_CODE);
		}
	}

	/**
	 * Check the relying party hash and the user presence of authenticator data.
	 */
	private void checkAuthData(final WebAuthnHelper.AuthData authData) {
		if (!MessageDigest.isEqual(authData.rpIdHash(), WebAuthnHelper.sha256(getRpId().getBytes(StandardCharsets.UTF_8)))) {
			throw new IllegalArgumentException("Relying party mismatch");
		}
		if (!authData.has(WebAuthnHelper.FLAG_UP)) {
			throw new IllegalArgumentException("User not present");
		}
	}

	private String getRpId() {
		return configuration.get(CONF_RP_ID, "localhost");
	}

	private List<Passkey> passkeys(final String login) {
		return repository.findAllByUserOrderByName(login).stream().filter(d -> SystemMfaDevice.TYPE_PASSKEY.equals(d.getType()))
				.map(this::passkey).toList();
	}

	private Passkey passkey(final SystemMfaDevice device) {
		return MAPPER.readValue(cryptoHelper.decrypt(device.getSecret()), Passkey.class);
	}

	/**
	 * Make a device the default one, proposed first at verification.
	 *
	 * @param id The device identifier.
	 */
	@PUT
	@Path("{id:\\d+}/default")
	public void setDefault(@PathParam("id") final int id) {
		final var login = securityHelper.getLogin();
		final var devices = repository.findAllByUserOrderByName(login);
		final var target = devices.stream().filter(d -> d.getId() == id).findFirst()
				.orElseThrow(() -> new EntityNotFoundException(String.valueOf(id)));
		devices.forEach(d -> d.setDefaultDevice(d == target));
		repository.saveAllAndFlush(devices);
		log.info("MFA device '{}' is now the default one for {}", target.getName(), login);
	}

	/**
	 * Persist a device. The first device of the user becomes the default one.
	 */
	private SystemMfaDevice register(final String login, final String name, final String type, final String secret) {
		final var device = new SystemMfaDevice();
		device.setUser(login);
		device.setName(name);
		device.setType(type);
		device.setSecret(cryptoHelper.encrypt(secret));
		device.setDefaultDevice(repository.countByUser(login) == 0);
		repository.saveAndFlush(device);
		log.info("MFA device '{}' ({}) registered for {}{}", name, type, login, device.isDefaultDevice() ? " as default" : "");
		return device;
	}

	/**
	 * Remove a device of the current user.
	 *
	 * @param id The device identifier.
	 */
	@DELETE
	@Path("{id:\\d+}")
	public void delete(@PathParam("id") final int id) {
		final var login = securityHelper.getLogin();
		final var device = Optional.ofNullable(repository.findByIdAndUser(id, login))
				.orElseThrow(() -> new EntityNotFoundException(String.valueOf(id)));
		repository.delete(device);
		repository.flush();
		log.info("MFA device '{}' removed for {}", device.getName(), login);
		if (device.isDefaultDevice()) {
			// The oldest remaining device becomes the default one
			final var next = repository.findFirstByUserOrderByIdAsc(login);
			if (next != null) {
				next.setDefaultDevice(true);
				repository.saveAndFlush(next);
				log.info("MFA device '{}' is now the default one for {}", next.getName(), login);
			}
		}
	}

	/**
	 * Verify a code against the selected device of the current user, or every device when none is selected. On
	 * success, the matching device records the usage.
	 *
	 * @param vo The code and the optional device.
	 */
	@POST
	@Path("verify")
	public void verify(final MfaCodeVo vo) {
		final var login = securityHelper.getLogin();
		final List<SystemMfaDevice> candidates;
		if (vo.getDevice() == null) {
			candidates = repository.findAllByUserOrderByName(login);
		} else {
			candidates = Optional.ofNullable(repository.findByIdAndUser(vo.getDevice(), login)).map(List::of).orElse(List.of());
		}
		for (final var device : candidates) {
			if (matches(device, vo.getCode())) {
				device.setLastUsed(Instant.now());
				repository.saveAndFlush(device);
				log.info("MFA verification succeeded for {} with device '{}'", login, device.getName());
				return;
			}
		}
		log.info("MFA verification failed for {}", login);
		throw new ValidationJsonException(CODE_PROPERTY, INVALID_CODE);
	}

	/**
	 * Whether the typed code matches the device: a TOTP code within the window. A passkey is never verified by a
	 * code, see {@link #verifyPasskey(PasskeyAssertionVo)}.
	 */
	private boolean matches(final SystemMfaDevice device, final String code) {
		return SystemMfaDevice.TYPE_TOTP.equals(device.getType())
				&& TotpHelper.verify(cryptoHelper.decrypt(device.getSecret()), code, WINDOW);
	}

	private MfaStatusVo status(final String login) {
		final var status = new MfaStatusVo();
		final var devices = repository.findAllByUserOrderByName(login);
		if (!devices.isEmpty() && devices.stream().noneMatch(SystemMfaDevice::isDefaultDevice)) {
			// Devices registered before the default flag existed: the oldest one becomes the default
			final var oldest = devices.stream().min(java.util.Comparator.comparing(SystemMfaDevice::getId)).orElseThrow();
			oldest.setDefaultDevice(true);
			repository.saveAndFlush(oldest);
			log.info("MFA device '{}' promoted as the default one for {}", oldest.getName(), login);
		}
		status.setDevices(devices.stream().map(this::toVo).toList());
		status.setRequired(!status.getDevices().isEmpty());
		status.setLastConnection(Optional.ofNullable(userRepository.findOne(login)).map(SystemUser::getLastConnection)
				.orElse(null));
		return status;
	}

	private MfaDeviceVo toVo(final SystemMfaDevice device) {
		final var vo = new MfaDeviceVo();
		vo.setId(device.getId());
		vo.setName(device.getName());
		vo.setType(device.getType());
		vo.setCreatedDate(device.getCreatedDate());
		vo.setLastUsed(device.getLastUsed());
		vo.setDefaultDevice(device.isDefaultDevice());
		return vo;
	}
}
