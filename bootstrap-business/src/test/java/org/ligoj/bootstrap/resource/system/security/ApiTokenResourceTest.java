/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import java.security.GeneralSecurityException;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.dao.system.SystemApiTokenRepository;
import org.ligoj.bootstrap.model.system.SystemApiToken;
import org.ligoj.bootstrap.resource.system.api.ApiTokenResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link ApiTokenResource}
 */
@ExtendWith(SpringExtension.class)
class ApiTokenResourceTest extends AbstractBootTest {

	private static final String TOKEN = "BWo9iEky2tpPX7RPhovNy5SywYI2fmacfRnLhJSZtfhCclj7IdP0uDZdLzqhUKnBu5svgKbkZS4eeLQVgu5Li2aMTOt9Fr1dLF8zMt7SNiMYyWv6YCFIsEUmeDjswFlf";
	private static final String TOKEN_CRYPT = "s0RscSFywiK2rU9L5bovd3nSVPx9HGP33IGIbWnhdxADs5MdzQS+ml6T84O2SdtGGexkocaug7He2SxPJhb4J3CRbvDRDuD5Qeq76MPA83x9hL4kZs/mNfW7BW1yV/iHNPXtbO4bURNbnZMPuA+sILYJmRIv2A5I9Vp5OiJy+QGzpE7uVM6wYg==";
	private static final String TOKEN_HASH = "0X73AfPjOJevD9d7acZ7swxjRJcDGASVcYeBf2AseSrhI3y9gMeVwdwotLVGcKJJTnfPFZYBsH6npUA13kL8Wg==";

	private static final String TOKEN2 = "G51xg2we70Ffd2YikudjpfvQszt63hXgjOMRqR713gWlswErfVlsXGRkujqKAz4Jgnxl5OrF3KZU8gaFhYwlEKmIcqkd2kT1TZjKNDNjsz77yakfybL5Bo1213b4Yt4Y";
	private static final String TOKEN2_CRYPT = "qZr9bHmhxu3s7zznK2vKuyvz4L7HRfngFvfRewegEJUeVQUXVF3niedDKBFz1iO6G5IMnD6vVXJbyVVNqXPj+kolK1Y9gZdG3JwXXHchQszGOxfW3BK6xO98sNUoS/KkIYAu4yoz+33dpFM3BHCKEEZyvVe5377dIgI8opH3a1l2GMkzJ0Apxg==";
	private static final String TOKEN2_HASH = "g3RAG1pLtkz6+HgwHJJWooQ9AK1mvt+nt4i41xXhRH+Hw9uOO4HFheFGjaH64GZsRXJ2XDlZFDm8C25SUKgLjg==";
	@Autowired
	private ApiTokenResource resource;

	@Autowired
	private SystemApiTokenRepository repository;

	@BeforeEach
	void setUp2() {
        var entity = new SystemApiToken();
		entity.setToken(TOKEN_CRYPT);
		entity.setHash(TOKEN_HASH);
		entity.setName("name");
		entity.setUser(DEFAULT_USER);
		repository.saveAndFlush(entity);

		entity = new SystemApiToken();
		entity.setToken(TOKEN2_CRYPT);
		entity.setHash(TOKEN2_HASH);
		entity.setName("name");
		entity.setUser("other");
		repository.saveAndFlush(entity);
		em.clear();
	}

	@Test
	void getToken() throws GeneralSecurityException {
		final var tokens = resource.getToken("name");
		Assertions.assertEquals(TOKEN, tokens);
	}

	@Test
	void getTokenNames() {
		final var tokensName = resource.getTokenNames();
		Assertions.assertEquals(1, tokensName.size());
		Assertions.assertEquals("name", tokensName.get(0));
	}

	@Test
	void check() {
		Assertions.assertTrue(resource.check(DEFAULT_USER, TOKEN));
	}

	@Test
	void checkNoUser() {
		Assertions.assertFalse(resource.check("any", TOKEN));
	}

	@Test
	void checkWrongUser() {
		Assertions.assertFalse(resource.check("other", TOKEN));
	}

	@Test
	void checkWrongKey() {
		Assertions.assertFalse(resource.check(DEFAULT_USER, TOKEN2));
	}

	@Test
	void checkInvalidDigest() {
		final var resource = new ApiTokenResource();
		resource.setTokenDigest("any");
		Assertions.assertFalse(resource.check(DEFAULT_USER, TOKEN));
	}

	@Test
	void create() throws GeneralSecurityException {
		Assertions.assertEquals(1, repository.findAllByUser(DEFAULT_USER).size());
		final var token = resource.create("new-api");

		// Check new state
		final var newToken = repository.findByNameExpected("new-api");
		Assertions.assertNotNull(token);
		Assertions.assertEquals(DEFAULT_USER, newToken.getUser());
		Assertions.assertNotNull(newToken.getHash());
		final var tokens = resource.getTokenNames();
		Assertions.assertEquals(2, tokens.size());
		Assertions.assertEquals("new-api", tokens.get(1));
		Assertions.assertEquals(token, resource.getToken("new-api"));
	}

	@Test
	void updateNotExist() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> resource.update("any"));
	}

	@Test
	void update() throws GeneralSecurityException {
        var tokens = resource.getTokenNames();
		Assertions.assertEquals(1, tokens.size());
		Assertions.assertEquals("name", tokens.get(0));
		final var token = resource.update("name");

		// Check new state
		Assertions.assertNotNull(token);
		tokens = resource.getTokenNames();
		Assertions.assertEquals(1, tokens.size());
		Assertions.assertEquals("name", tokens.get(0));
		Assertions.assertEquals(token, resource.getToken("name"));
		final var newToken = repository.findByUserAndName(DEFAULT_USER, "name");
		Assertions.assertNotNull(newToken);
		Assertions.assertEquals(DEFAULT_USER, newToken.getUser());
		Assertions.assertNotNull(newToken.getToken());
		Assertions.assertNotNull(newToken.getHash());
	}

	@Test
	void remove() {
		Assertions.assertEquals(1, repository.findAllByUser(DEFAULT_USER).size());
		resource.remove("name");

		// Check new state
		repository.findByNameExpected("name");
		Assertions.assertEquals(1, repository.findAllByUser("other").size());
		Assertions.assertEquals(0, repository.findAllByUser(DEFAULT_USER).size());
	}
}
