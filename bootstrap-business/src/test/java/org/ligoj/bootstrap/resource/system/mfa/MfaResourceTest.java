/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.mfa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.ligoj.bootstrap.FakeAuthenticator;
import org.ligoj.bootstrap.MatcherUtil;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.ligoj.bootstrap.core.crypto.TotpHelper;
import org.ligoj.bootstrap.core.crypto.WebAuthnHelper;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import org.ligoj.bootstrap.dao.system.SystemMfaDeviceRepository;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.model.system.SystemMfaDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.persistence.EntityNotFoundException;

/**
 * Test class of {@link MfaResource}.
 */
@ExtendWith(SpringExtension.class)
class MfaResourceTest extends AbstractBootTest {

	private static final String OTHER_USER = "other";

	@Autowired
	private MfaResource resource;

	@Autowired
	private SystemMfaDeviceRepository repository;

	@Autowired
	private SystemUserRepository userRepository;

	@Autowired
	private ConfigurationResource configuration;

	@BeforeEach
	void resetAttempts() {
		// The failure counters are shared by the tests of the same user
		clearAllCache();
	}

	private MfaDeviceEditionVo edition(final String name, final String secret, final String code) {
		final var vo = new MfaDeviceEditionVo();
		vo.setName(name);
		vo.setSecret(secret);
		vo.setCode(code);
		return vo;
	}

	private int enroll(final String name) {
		final var setup = resource.setupTotp();
		return resource.createTotp(edition(name, setup.getSecret(), TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
	}

	@Test
	void statusWithoutDevice() {
		final var status = resource.get();
		Assertions.assertFalse(status.isRequired());
		Assertions.assertTrue(status.getDevices().isEmpty());
	}

	@Test
	void setupAndCreate() {
		final var setup = resource.setupTotp();
		Assertions.assertEquals(32, setup.getSecret().length());
		Assertions.assertEquals(DEFAULT_USER, setup.getAccount());
		Assertions.assertEquals(MfaResource.DEFAULT_ISSUER, setup.getIssuer());
		Assertions.assertTrue(setup.getUri().startsWith("otpauth://totp/Ligoj:" + DEFAULT_USER + "?secret=" + setup.getSecret()));

		final var code = TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter());
		final var id = resource.createTotp(edition("phone", setup.getSecret(), code));
		final var device = repository.findOne(id);
		Assertions.assertEquals(DEFAULT_USER, device.getUser());
		Assertions.assertEquals("phone", device.getName());
		Assertions.assertEquals(SystemMfaDevice.TYPE_TOTP, device.getType());
		// Encrypted at rest
		Assertions.assertNotEquals(setup.getSecret(), device.getSecret());
		Assertions.assertEquals(setup.getSecret(), cryptoHelper.decrypt(device.getSecret()));
		Assertions.assertNotNull(device.getCreatedDate());

		final var status = resource.get();
		Assertions.assertTrue(status.isRequired());
		Assertions.assertEquals(1, status.getDevices().size());
		Assertions.assertEquals("phone", status.getDevices().getFirst().getName());
		Assertions.assertEquals(id, status.getDevices().getFirst().getId());
		Assertions.assertNull(status.getDevices().getFirst().getLastUsed());
	}

	@Test
	void createInvalidCode() {
		final var setup = resource.setupTotp();
		final var vo = edition("phone", setup.getSecret(), "000000");
		final var error = Assertions.assertThrows(ValidationJsonException.class, () -> resource.createTotp(vo));
		Assertions.assertTrue(error.getErrors().containsKey("code"));
		Assertions.assertEquals(0, repository.countByUser(DEFAULT_USER));
	}

	@Test
	void createDuplicateName() {
		enroll("phone");
		final var setup = resource.setupTotp();
		final var vo = edition("phone", setup.getSecret(), TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter()));
		Assertions.assertTrue(Assertions.assertThrows(ValidationJsonException.class, () -> resource.createTotp(vo))
				.getErrors().containsKey("name"));
	}

	@Test
	void verify() {
		final var setup = resource.setupTotp();
		final var id = resource.createTotp(edition("phone", setup.getSecret(), TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
		final var wrong = new MfaCodeVo();
		wrong.setCode("000000");
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.verify(wrong));
		Assertions.assertNull(repository.findOne(id).getLastUsed());

		final var right = new MfaCodeVo();
		right.setCode(TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter()));
		resource.verify(right);
		Assertions.assertNotNull(repository.findOne(id).getLastUsed());
		Assertions.assertNotNull(resource.get().getDevices().getFirst().getLastUsed());
	}

	private MfaCodeVo code(final String code) {
		final var vo = new MfaCodeVo();
		vo.setCode(code);
		return vo;
	}

	private void failVerify(final int times) {
		for (var i = 0; i < times; i++) {
			MatcherUtil.assertThrows(Assertions.assertThrows(ValidationJsonException.class, () -> resource.verify(code("000000"))),
					"code", "invalid-code");
		}
	}

	@Test
	void verifiedState() {
		final var setup = resource.setupTotp();
		resource.createTotp(edition("phone", setup.getSecret(), TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
		Assertions.assertNull(resource.get().getVerifiedDate());

		// A failed verification does not verify
		failVerify(1);
		Assertions.assertNull(resource.get().getVerifiedDate());
		Assertions.assertNull(resource.getVerifiedDate(DEFAULT_USER));

		// A successful one does, for this user only
		final var before = Instant.now();
		resource.verify(code(TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
		final var verified = resource.getVerifiedDate(DEFAULT_USER);
		Assertions.assertNotNull(verified);
		Assertions.assertFalse(verified.isBefore(before.truncatedTo(ChronoUnit.MILLIS)));
		Assertions.assertEquals(verified, resource.get().getVerifiedDate());
		Assertions.assertNull(resource.getVerifiedDate(OTHER_USER));

		// A new primary authentication requires a new verification
		Assertions.assertNull(resource.login().getVerifiedDate());
		Assertions.assertNull(resource.getVerifiedDate(DEFAULT_USER));
	}

	@Test
	void verifyLockedAfterTooManyFailures() {
		final var setup = resource.setupTotp();
		resource.createTotp(edition("phone", setup.getSecret(), TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
		failVerify(MfaResource.DEFAULT_MAX_ATTEMPTS);

		// Locked: even the right code is refused, without being checked
		final var right = code(TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter()));
		MatcherUtil.assertThrows(Assertions.assertThrows(ValidationJsonException.class, () -> resource.verify(right)),
				"code", "too-many-attempts");
		Assertions.assertNull(resource.get().getDevices().getFirst().getLastUsed());

		// The lock expires with the cache entry
		cacheManager.getCache("mfa-attempts").clear();
		resource.verify(right);
	}

	@Test
	void verifySuccessResetsFailures() {
		final var setup = resource.setupTotp();
		resource.createTotp(edition("phone", setup.getSecret(), TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
		failVerify(MfaResource.DEFAULT_MAX_ATTEMPTS - 1);
		resource.verify(code(TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
		failVerify(MfaResource.DEFAULT_MAX_ATTEMPTS - 1);
		resource.verify(code(TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
	}

	@Test
	void verifyMaxAttemptsConfiguration() {
		configuration.put(MfaResource.CONF_MAX_ATTEMPTS, "2");
		final var setup = resource.setupTotp();
		resource.createTotp(edition("phone", setup.getSecret(), TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
		failVerify(2);
		final var right = code(TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter()));
		MatcherUtil.assertThrows(Assertions.assertThrows(ValidationJsonException.class, () -> resource.verify(right)),
				"code", "too-many-attempts");
	}

	@Test
	void verifyIgnoresOtherUsersDevices() {
		final var secret = TotpHelper.generateSecret();
		final var other = new SystemMfaDevice();
		other.setUser(OTHER_USER);
		other.setName("phone");
		other.setSecret(cryptoHelper.encrypt(secret));
		repository.saveAndFlush(other);
		final var vo = new MfaCodeVo();
		vo.setCode(TotpHelper.code(secret, TotpHelper.currentCounter()));
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.verify(vo));
		Assertions.assertFalse(resource.get().isRequired());
	}

	@Test
	void deleteOwnDevice() {
		final var id = enroll("phone");
		resource.delete(id);
		Assertions.assertNull(repository.findOne(id));
		Assertions.assertFalse(resource.get().isRequired());
	}

	@Test
	void deleteOtherDevice() {
		final var other = new SystemMfaDevice();
		other.setUser(OTHER_USER);
		other.setName("phone");
		other.setSecret(cryptoHelper.encrypt(TotpHelper.generateSecret()));
		repository.saveAndFlush(other);
		final var id = other.getId();
		Assertions.assertThrows(EntityNotFoundException.class, () -> resource.delete(id));
		Assertions.assertNotNull(repository.findOne(id));
	}

	@Test
	void defaultDeviceRules() {
		// The first registered device is the default one
		final var first = enroll("phone");
		final var setup = resource.setupTotp();
		final var second = resource.createTotp(edition("tablet", setup.getSecret(), TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
		var devices = resource.get().getDevices();
		Assertions.assertTrue(devices.stream().filter(d -> d.getId() == first).findFirst().orElseThrow().isDefaultDevice());
		Assertions.assertFalse(devices.stream().filter(d -> d.getId() == second).findFirst().orElseThrow().isDefaultDevice());

		// Explicit default
		resource.setDefault(second);
		devices = resource.get().getDevices();
		Assertions.assertFalse(devices.stream().filter(d -> d.getId() == first).findFirst().orElseThrow().isDefaultDevice());
		Assertions.assertTrue(devices.stream().filter(d -> d.getId() == second).findFirst().orElseThrow().isDefaultDevice());
		Assertions.assertThrows(EntityNotFoundException.class, () -> resource.setDefault(-1));

		// Removing the default hands the role to the oldest remaining device
		resource.delete(second);
		Assertions.assertTrue(repository.findOne(first).isDefaultDevice());

		// Removing a non-default device keeps the default
		final var third = enroll("laptop");
		resource.delete(third);
		Assertions.assertTrue(repository.findOne(first).isDefaultDevice());
	}

	@Test
	void legacyDevicesWithoutDefault() {
		// Rows created before the default flag: none is the default, the oldest gets promoted on read
		final var older = new SystemMfaDevice();
		older.setUser(DEFAULT_USER);
		older.setName("older");
		older.setSecret(cryptoHelper.encrypt(TotpHelper.generateSecret()));
		repository.saveAndFlush(older);
		final var newer = new SystemMfaDevice();
		newer.setUser(DEFAULT_USER);
		newer.setName("newer");
		newer.setSecret(cryptoHelper.encrypt(TotpHelper.generateSecret()));
		repository.saveAndFlush(newer);
		Assertions.assertFalse(older.isDefaultDevice());

		final var devices = resource.get().getDevices();
		Assertions.assertTrue(devices.stream().filter(d -> d.getId().equals(older.getId())).findFirst().orElseThrow().isDefaultDevice());
		Assertions.assertFalse(devices.stream().filter(d -> d.getId().equals(newer.getId())).findFirst().orElseThrow().isDefaultDevice());
		Assertions.assertTrue(repository.findOne(older.getId()).isDefaultDevice());
	}

	private PasskeyRegistrationVo registration(final FakeAuthenticator authenticator, final String name, final Map<String, Object> options, final String origin) {
		final var vo = new PasskeyRegistrationVo();
		vo.setName(name);
		vo.setId(authenticator.getCredentialId());
		vo.setClientDataJSON(FakeAuthenticator.clientData("webauthn.create", (String) options.get("challenge"), origin));
		vo.setAttestationObject(authenticator.attestationObject("localhost", WebAuthnHelper.FLAG_UP | WebAuthnHelper.FLAG_UV));
		return vo;
	}

	private PasskeyAssertionVo assertion(final FakeAuthenticator authenticator, final Map<String, Object> options, final String origin) {
		final var clientData = FakeAuthenticator.clientData("webauthn.get", (String) options.get("challenge"), origin);
		final var signed = authenticator.assertion("localhost", WebAuthnHelper.FLAG_UP | WebAuthnHelper.FLAG_UV, clientData);
		final var vo = new PasskeyAssertionVo();
		vo.setId(authenticator.getCredentialId());
		vo.setClientDataJSON(clientData);
		vo.setAuthenticatorData(signed.authenticatorData());
		vo.setSignature(signed.signature());
		return vo;
	}

	@Test
	void passkeyRegistrationAndAssertion() {
		final var authenticator = new FakeAuthenticator(5);
		final var options = resource.setupPasskey();
		Assertions.assertEquals("localhost", ((Map<?, ?>) options.get("rp")).get("id"));
		Assertions.assertEquals(DEFAULT_USER, ((Map<?, ?>) options.get("user")).get("name"));
		Assertions.assertTrue(((List<?>) options.get("excludeCredentials")).isEmpty());
		// The host sends the transports only to an API advertising it (older APIs reject unknown properties)
		Assertions.assertEquals(Boolean.TRUE, options.get("transportsHint"));
		final var id = resource.createPasskey(registration(authenticator, "macbook", options, "http://localhost:5173"));
		final var device = repository.findOne(id);
		Assertions.assertEquals(SystemMfaDevice.TYPE_PASSKEY, device.getType());
		Assertions.assertTrue(device.isDefaultDevice());
		Assertions.assertFalse(device.getSecret().contains(authenticator.getCredentialId()));

		// The challenge is single-use
		final var replay = registration(authenticator, "again", options, "http://localhost:5173");
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.createPasskey(replay));
		// A registered credential is excluded from a new registration
		Assertions.assertEquals(authenticator.getCredentialId(), ((Map<?, ?>) ((List<?>) resource.setupPasskey().get("excludeCredentials")).getFirst()).get("id"));

		// Assertion with a fresh challenge
		final var request = resource.challengePasskey();
		Assertions.assertEquals("localhost", request.get("rpId"));
		Assertions.assertEquals(authenticator.getCredentialId(), ((Map<?, ?>) ((List<?>) request.get("allowCredentials")).getFirst()).get("id"));
		authenticator.setCounter(6);
		Assertions.assertNull(resource.get().getVerifiedDate());
		resource.verifyPasskey(assertion(authenticator, request, "http://localhost:5173"));
		Assertions.assertNotNull(repository.findOne(id).getLastUsed());
		Assertions.assertNotNull(resource.get().getVerifiedDate());

		// Counter regression (cloned authenticator): rejected
		final var request2 = resource.challengePasskey();
		authenticator.setCounter(6);
		final var replayed = assertion(authenticator, request2, "http://localhost:5173");
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.verifyPasskey(replayed));

		// A TOTP code never matches a passkey device
		final var code = new MfaCodeVo();
		code.setCode("123456");
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.verify(code));
	}

	@Test
	@SuppressWarnings("unchecked")
	void passkeyTransportsHint() {
		// The transports reported by the browser at registration are returned with the challenge, sanitized, so the
		// browser knows where the credential lives (local authenticator, phone...). A passkey registered without
		// them (legacy) has no hint.
		final var authenticator = new FakeAuthenticator(1);
		final var vo = registration(authenticator, "mac", resource.setupPasskey(), "http://localhost:5173");
		vo.setTransports(List.of("internal", "hybrid", "bogus", " ", "internal"));
		resource.createPasskey(vo);
		final var legacy = new FakeAuthenticator(1);
		resource.createPasskey(registration(legacy, "legacy", resource.setupPasskey(), "http://localhost:5173"));
		final var allowed = ((List<Map<String, Object>>) resource.challengePasskey().get("allowCredentials")).stream()
				.collect(java.util.stream.Collectors.toMap(m -> m.get("id"), m -> m));
		Assertions.assertEquals(List.of("internal", "hybrid"), allowed.get(authenticator.getCredentialId()).get("transports"));
		Assertions.assertFalse(allowed.get(legacy.getCredentialId()).containsKey("transports"));
	}

	/**
	 * The device list tells what kind of authenticator a passkey is: the transports and the attachment reported by
	 * the browser, and the authenticator model from its AAGUID when known.
	 */
	@Test
	void passkeyDeviceType() {
		// A YubiKey 5 NFC: cross-platform, USB + NFC, well-known AAGUID
		final var yubikey = new FakeAuthenticator(1);
		yubikey.setAaguid("2fc0579f-8113-47ea-b116-bb5a8db9202a");
		final var vo = registration(yubikey, "yubikey", resource.setupPasskey(), "http://localhost:5173");
		vo.setTransports(List.of("usb", "nfc"));
		vo.setAuthenticatorAttachment("cross-platform");
		final var yubikeyId = resource.createPasskey(vo);

		// A platform authenticator with the zero AAGUID (attestation "none"), bogus attachment ignored
		final var laptop = new FakeAuthenticator(1);
		final var laptopVo = registration(laptop, "laptop", resource.setupPasskey(), "http://localhost:5173");
		laptopVo.setTransports(List.of("internal"));
		laptopVo.setAuthenticatorAttachment("weird");
		final var laptopId = resource.createPasskey(laptopVo);

		final var devices = resource.get().getDevices().stream()
				.collect(java.util.stream.Collectors.toMap(MfaDeviceVo::getId, java.util.function.Function.identity()));
		final var key = devices.get(yubikeyId);
		Assertions.assertEquals(List.of("usb", "nfc"), key.getTransports());
		Assertions.assertEquals("cross-platform", key.getAttachment());
		Assertions.assertEquals("2fc0579f-8113-47ea-b116-bb5a8db9202a", key.getAaguid());
		Assertions.assertEquals("YubiKey 5 NFC", key.getModel());
		final var mac = devices.get(laptopId);
		Assertions.assertEquals(List.of("internal"), mac.getTransports());
		Assertions.assertNull(mac.getAttachment());
		Assertions.assertNull(mac.getAaguid());
		Assertions.assertNull(mac.getModel());

		// A TOTP device carries none of these
		final var secret = TotpHelper.generateSecret();
		final var totpId = resource.createTotp(edition("app", secret, TotpHelper.code(secret, TotpHelper.currentCounter())));
		final var app = resource.get().getDevices().stream().filter(d -> d.getId().equals(totpId)).findFirst().orElseThrow();
		Assertions.assertNull(app.getTransports());
		Assertions.assertNull(app.getModel());
	}

	@Test
	void passkeyRejections() {
		final var authenticator = new FakeAuthenticator(0);
		// Wrong origin
		var options = resource.setupPasskey();
		final var badOrigin = registration(authenticator, "k", options, "https://evil.com");
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.createPasskey(badOrigin));
		// Wrong challenge type
		options = resource.setupPasskey();
		final var badType = registration(authenticator, "k", options, "http://localhost:5173");
		badType.setClientDataJSON(FakeAuthenticator.clientData("webauthn.get", (String) options.get("challenge"), "http://localhost:5173"));
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.createPasskey(badType));
		// Credential id mismatch
		options = resource.setupPasskey();
		final var badId = registration(authenticator, "k", options, "http://localhost:5173");
		badId.setId("other");
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.createPasskey(badId));
		// No challenge at all
		final var noChallenge = registration(authenticator, "k", Map.of("challenge", "x"), "http://localhost:5173");
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.createPasskey(noChallenge));
		Assertions.assertEquals(0, repository.countByUser(DEFAULT_USER));

		// Register, then assertions with a bad signature, an unknown credential, a missing user presence
		options = resource.setupPasskey();
		resource.createPasskey(registration(authenticator, "macbook", options, "http://localhost:5173"));
		var request = resource.challengePasskey();
		final var tampered = assertion(authenticator, request, "http://localhost:5173");
		tampered.setSignature(WebAuthnHelper.base64Url(new byte[] { 1, 2, 3 }));
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.verifyPasskey(tampered));
		request = resource.challengePasskey();
		final var unknown = assertion(new FakeAuthenticator(0), request, "http://localhost:5173");
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.verifyPasskey(unknown));
		request = resource.challengePasskey();
		final var clientData = FakeAuthenticator.clientData("webauthn.get", (String) request.get("challenge"), "http://localhost:5173");
		final var absent = authenticator.assertion("localhost", 0, clientData);
		final var notPresent = new PasskeyAssertionVo();
		notPresent.setId(authenticator.getCredentialId());
		notPresent.setClientDataJSON(clientData);
		notPresent.setAuthenticatorData(absent.authenticatorData());
		notPresent.setSignature(absent.signature());
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.verifyPasskey(notPresent));
	}

	@Test
	void verifySelectedDevice() {
		final var setup = resource.setupTotp();
		final var totp = resource.createTotp(edition("phone", setup.getSecret(), TotpHelper.code(setup.getSecret(), TotpHelper.currentCounter())));
		final var setup2 = resource.setupTotp();
		final var totp2 = resource.createTotp(edition("tablet", setup2.getSecret(), TotpHelper.code(setup2.getSecret(), TotpHelper.currentCounter())));

		// The tablet code against the phone device: rejected; against its own device or without selection: accepted
		final var vo = new MfaCodeVo();
		vo.setCode(TotpHelper.code(setup2.getSecret(), TotpHelper.currentCounter()));
		vo.setDevice(totp);
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.verify(vo));
		vo.setDevice(totp2);
		resource.verify(vo);
		vo.setDevice(null);
		resource.verify(vo);
		// Unknown or foreign device: rejected
		vo.setDevice(-1);
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.verify(vo));
	}

	@Test
	void login() {
		userRepository.deleteAllBy("login", DEFAULT_USER);
		Assertions.assertNull(resource.get().getLastConnection());
		final var status = resource.login();
		Assertions.assertNotNull(status.getLastConnection());
		Assertions.assertFalse(status.isRequired());
		Assertions.assertEquals(status.getLastConnection(), userRepository.findOne(DEFAULT_USER).getLastConnection());

		enroll("phone");
		Assertions.assertTrue(resource.login().isRequired());
	}
}
