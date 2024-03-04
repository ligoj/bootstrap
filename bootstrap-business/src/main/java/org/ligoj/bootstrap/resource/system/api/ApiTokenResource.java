/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.ligoj.bootstrap.core.NamedBean;
import org.ligoj.bootstrap.core.resource.OnNullReturn404;
import org.ligoj.bootstrap.core.security.SecurityHelper;
import org.ligoj.bootstrap.dao.system.SystemApiTokenRepository;
import org.ligoj.bootstrap.model.system.SystemApiToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * API Token resource. A user can have several tokens, each one associated to a unique name (user's scope). The
 * general behavior is:
 * <ul>
 * <li>In the database, are stored user (owner), logical name of the key, hashed key (SHA-512+), encrypted key.</li>
 * <li>Cipher key column is used to display the plain token value for the user. One by one.</li>
 * <li>Hashed key column is used to match the key, as we would do it for password.</li>
 * <li>The salt used for hashed value is only user name. SHA-512+ strength and the key length (&gt;128) reduce slightly
 * the issues.</li>
 * <li>Secret key used for ciphering is based on SHA-1 of the key plus key's name, plus user's login, plus a secret key,
 * the whole with 30+ iterations. So SHA-1 is not used there to hash a password, but to build a secret key.</li>
 * </ul>
 */
@Path("/api/token")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ApiTokenResource {

	/**
	 * Special prefix for plain/unsecured hash API token. Useful for generated API token from external tool.
	 */
	private static final String PREFIX_TOKEN = "_plain_";
	/**
	 * Special value for plain/unsecured hash API token. Useful for generated API token from external tool.
	 */
	private static final String PLAIN_HASH = "_plain_";

	/**
	 * Az09 string generator.
	 */
	private static final RandomStringGenerator GENERATOR = new RandomStringGenerator.Builder().filteredBy(c -> CharUtils.isAsciiAlphanumeric(Character.toChars(c)[0])).build();

	@Autowired
	protected SystemApiTokenRepository repository;

	@Autowired
	private SecurityHelper securityHelper;

	/**
	 * Amount of digest iterations applied to original message to produce the target hash.
	 */
	@Value("${api.token.iterations:31}")
	private int tokenIterations;

	@Setter
	@Value("${api.token.digest:SHA-512}")
	private String tokenDigest;

	@Value("${api.token.length:128}")
	private int tokenLength;

	/**
	 * Cipher implementation.
	 */
	@Value("${api.token.crypt:DESede}")
	private String tokenCrypt;

	/**
	 * Secret key of DES algorithm used to generate the SSO token. This not really a secret and could be public.
	 * This only an additional layer to hash the randomly generated token.
	 */
	@Value("${api.token.secret:K%ë£/L@_§z3-Àçñ?}")
	private String tokenSecret;

	/**
	 * Check the given token.
	 *
	 * @param user  The username. Will be used to build the hash.
	 * @param token The user password or token.
	 * @return <code>true</code> if the token matches.
	 */
	public boolean check(final String user, final String token) {
		try {
			if (StringUtils.startsWith(token, PREFIX_TOKEN)) {
				// Unsecured token, only null hash can match
				return repository.checkByUserAndToken(user, token);
			}
			// Check the API token from database
			return repository.checkByUserAndHash(user, hash(token));
		} catch (final GeneralSecurityException e) {
			log.error("Unable to validate a token for user : " + user, e);
		}

		// Credential has not been validated, the user is invalid
		return false;
	}

	/**
	 * Return all API names owned by the current user.
	 *
	 * @return All API token names the current user owns.
	 */
	@GET
	public List<String> getTokenNames() {
		return repository.findAllByUser(securityHelper.getLogin());
	}

	/**
	 * Return raw token value corresponding to the requested name and owned by current user.
	 *
	 * @param name The token's name.
	 * @return raw token value corresponding to the requested name and owned by current user.
	 */
	@GET
	@Path("{name:[\\w.-]+}")
	@OnNullReturn404
	@Produces(MediaType.TEXT_PLAIN)
	public String getToken(@PathParam("name") final String name) {
		final var entity = repository.findByUserAndName(securityHelper.getLogin(), name);
		if (entity == null) {
			return null;
		}
		if (entity.getHash().equals(PLAIN_HASH) && entity.getToken().startsWith(PREFIX_TOKEN)) {
			// Unsecured plain token. Useful for initial SQL injected token
			return entity.getToken();
		}
		try {
			return decrypt(entity.getToken(), newSecretKey(entity.getUser(), entity.getName()));
		} catch (Exception e) {
			log.error("Unable to decrypt token {}", name, e);
			return null;
		}
	}

	/**
	 * Check a token exists for given user.
	 *
	 * @param name Token name to check existence
	 * @param user The owner user.
	 * @return <code>true</code> when the user has a token key having this name.
	 */
	public boolean hasToken(final String user, final String name) {
		return repository.findByUserAndName(user, name) != null;
	}

	/**
	 * Decrypt the message with the given key.
	 *
	 * @param encryptedMessage Encrypted message.
	 * @param secretKey        The secret key.
	 * @return the original message.
	 * @throws GeneralSecurityException When there is a security issue.
	 */
	private String decrypt(final String encryptedMessage, final byte[] secretKey) throws GeneralSecurityException {
		final var message = Base64.decodeBase64(encryptedMessage.getBytes(StandardCharsets.UTF_8));
		final var md = MessageDigest.getInstance(tokenDigest);
		final var digestOfPassword = md.digest(secretKey);
		final var keyBytes = Arrays.copyOf(digestOfPassword, 24);
		final SecretKey key = new SecretKeySpec(keyBytes, tokenCrypt);
		final var decipher = Cipher.getInstance(tokenCrypt);
		decipher.init(Cipher.DECRYPT_MODE, key);
		final var plainText = decipher.doFinal(message);
		return new String(plainText, StandardCharsets.UTF_8);
	}

	/**
	 * Encrypt the message with the given key.
	 *
	 * @param message   Ciphered message.
	 * @param secretKey The secret key.
	 * @return the original message.
	 * @throws GeneralSecurityException When there is a security issue.
	 */
	private String encrypt(final String message, final byte[] secretKey) throws GeneralSecurityException {
		final var digest = MessageDigest.getInstance(tokenDigest);
		digest.reset();
		final var digestOfPassword = digest.digest(secretKey);
		final var keyBytes = Arrays.copyOf(digestOfPassword, 24);
		final SecretKey key = new SecretKeySpec(keyBytes, tokenCrypt);
		final var cipher = Cipher.getInstance(tokenCrypt);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		final var plainTextBytes = message.getBytes(StandardCharsets.UTF_8);
		final var buf = cipher.doFinal(plainTextBytes);
		final var base64Bytes = Base64.encodeBase64(buf);
		return new String(base64Bytes, StandardCharsets.UTF_8);
	}

	/**
	 * Hash without salt the given token.
	 *
	 * @param token The user token.
	 * @return the hash without salt.
	 */
	private String hash(final String token) throws NoSuchAlgorithmException {
		final var digest = MessageDigest.getInstance(tokenDigest);
		digest.reset();
		return Base64.encodeBase64String(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * From a password, an amount of iterations, returns the corresponding digest
	 *
	 * @param iterations The amount of iterations of the algorithm.
	 * @param password   String The password to encrypt
	 * @return byte[] The digested password
	 * @throws NoSuchAlgorithmException If the algorithm doesn't exist
	 */
	protected byte[] simpleHash(final int iterations, final String password) throws NoSuchAlgorithmException {
		// This is not a single hash
		final var digest = MessageDigest.getInstance("SHA-1"); // NOSONAR
		digest.reset();
		var input = digest.digest(password.getBytes(StandardCharsets.UTF_8));
		for (var i = 0; i < iterations; i++) {
			digest.reset();
			input = digest.digest(input);
		}
		return input;
	}

	private byte[] newSecretKey(final String login, final String name) throws NoSuchAlgorithmException {
		// Digest computation
		return simpleHash(tokenIterations, login + tokenSecret + name);
	}

	/**
	 * Create a new token for current user.
	 *
	 * @param name New token name.
	 * @return the generated token.
	 * @throws GeneralSecurityException When there is a security issue.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{name:[\\w.-]+}")
	public NamedBean<String> create(@PathParam("name") final String name) throws GeneralSecurityException {
		return create(securityHelper.getLogin(), name);
	}

	/**
	 * Create a new token for given user.
	 *
	 * @param name New token name.
	 * @param user The target user
	 * @return the generated token.
	 * @throws GeneralSecurityException When there is a security issue.
	 */
	public NamedBean<String> create(final String user, final String name) throws GeneralSecurityException {
		final var entity = new SystemApiToken();
		entity.setName(name);
		entity.setUser(user);
		final var token = newToken(entity);
		repository.saveAndFlush(entity);
		return new NamedBean<>(token, name);
	}

	/**
	 * Update the token with a new one.
	 */
	private String newToken(final SystemApiToken entity) throws GeneralSecurityException {
		final var token = newToken();
		entity.setHash(hash(token));
		entity.setToken(encrypt(token, newSecretKey(entity.getUser(), entity.getName())));
		return token;
	}

	/**
	 * Generate a new token.
	 */
	private String newToken() {
		return GENERATOR.generate(tokenLength);
	}

	/**
	 * Update a named token with a new generated one.
	 *
	 * @param name Token to update.
	 * @return the new generated token.
	 * @throws GeneralSecurityException When there is a security issue.
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{name:[\\w.-]+}")
	@Produces(MediaType.TEXT_PLAIN)
	public String update(@PathParam("name") final String name) throws GeneralSecurityException {
		final var entity = repository.findByUserAndName(securityHelper.getLogin(), name);
		if (entity == null) {
			// No token with given name
			throw new EntityNotFoundException();
		}

		// Token has been found, update it
		final var token = newToken(entity);
		repository.saveAndFlush(entity);
		return token;
	}

	/**
	 * Delete an API token by its name for current user.
	 *
	 * @param name The API token's name.
	 */
	@DELETE
	@Path("{name:[\\w.-]+}")
	public void remove(@PathParam("name") final String name) {
		repository.deleteByUserAndName(securityHelper.getLogin(), name);
	}

	/**
	 * Remove all API keys associated to given user.
	 *
	 * @param login The related username.
	 */
	public void removeAll(final String login) {
		repository.deleteAllBy("user", login);
	}
}
