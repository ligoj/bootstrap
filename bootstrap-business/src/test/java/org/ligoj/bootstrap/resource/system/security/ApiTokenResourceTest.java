/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import jakarta.persistence.EntityNotFoundException;
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

import java.security.GeneralSecurityException;

/**
 * Test class of {@link ApiTokenResource}
 */
@ExtendWith(SpringExtension.class)
class ApiTokenResourceTest extends AbstractBootTest {

	private static final String TOKEN = "GJqYHFWnnH3Dp9XMVdDV3QkFbjltP2jxodtlvUQXnlLAFvN5pR45WysikOng8nI7A8HS55MhsDS0w6h4UvgnNh3oLb6RpaUI2luzhtC3VhziP03wbMkkZNNfiL3L5wE3";
	private static final String TOKEN_CRYPT = "+CdhL/qhnLv9xLjuTeoJfQN6B+TXBnpjNPu3y6lKWQxIJrVw2qMNyQOKRZ6eUVbvZpuBkgHdIT/Te9Olc5WQVsPsQ2ym9VdvMKH/7MJdElit37yxTlQVjHXmZL4dB83DDGOUs7W1CS2pjgddQT/EkkrDKwL1W4m9ktGdG7h6MVim9xugsGho7A==";
	private static final String TOKEN_HASH = "FBG9Cn/dvk4okgV2svVhGJOQ1ozHgf8SzlaoMt7A6nfJVjsdSv6fRny9dHPW1VNH1JRH6lPeRewvwNMPN45TAQ==";

	private static final String TOKEN2 = "Ol6lNcVl2AXFvj8xNFt3VX0tcMExFYt0J8SrTin7DzQl9qzCvA1xgmsSOaQOuVSm4TnaSqLo3se80pCSLi5Qzez1n3qMT8JJ3gyRzXDHDSegPjOWhufwnEgBi6NQxkn8";
	private static final String TOKEN2_CRYPT = "5c8zRVZh+xcCwDjX8rI4VkynWK4VENxADB27MLi9I+bvA6tiAsnTsfq2U+4Tl32jrEZ1UQaI2Ba/P0KEEXDUaL7oAOTUxYaYL1/Bfra1YJFr+7oAi7kz4ziJ0FjKyfFOsHFcg01jS+6Gm6U1FdEHO/kjJiJL3GVNHBgEMK5RFXGotW+m4lBO8g==";
	private static final String TOKEN2_HASH = "CjQsWSNXHXNt3o6oC7/h2cvVFwQa420o6co93AQMbVdpv/pCKLxkk2kovjmtG7XCuwzunPPnex6sMUpItdte7g==";
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
		
		// Unsecured token
		entity = new SystemApiToken();
		entity.setToken("_plain_TEST");
		entity.setHash("_plain_");
		entity.setName("unsecured");
		entity.setUser(DEFAULT_USER);
		repository.saveAndFlush(entity);

		// Partially unsecured token : SQL injection, hack,...
		entity = new SystemApiToken();
		entity.setToken("TEST");
		entity.setHash("_plain_");
		entity.setName("unsecured_ko_2");
		entity.setUser(DEFAULT_USER);
		repository.saveAndFlush(entity);

		// Partially unsecured token : SQL injection, hack,...
		entity = new SystemApiToken();
		entity.setToken("_plain_TEST");
		entity.setHash("_plain_hash");
		entity.setName("unsecured_ko_3");
		entity.setUser(DEFAULT_USER);
		repository.saveAndFlush(entity);
		em.clear();
	}

	@Test
	void getTokenPlain() {
		final var tokens = resource.getToken("unsecured");
		Assertions.assertEquals("_plain_TEST", tokens);
	}

	@Test
	void getTokenNotExist() {
		Assertions.assertNull( resource.getToken("any"));
	}

	@Test
	void getToken() {
		final var tokens = resource.getToken("name");
		Assertions.assertEquals(TOKEN, tokens);
	}

	@Test
	void getTokenNames() {
		final var tokensName = resource.getTokenNames();
		Assertions.assertEquals(4, tokensName.size());
		Assertions.assertEquals("name", tokensName.getFirst());
	}

	@Test
	void check() {
		Assertions.assertTrue(resource.check(DEFAULT_USER, TOKEN));
	}

	@Test
	void checkPlain() {
		Assertions.assertTrue(resource.check(DEFAULT_USER, "_plain_TEST"));
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
	void checkWrongPlain1() {
		Assertions.assertFalse(resource.check(DEFAULT_USER, "unsecured_ko_2"));
	}

	@Test
	void getTokenWrongPlain1() {
		Assertions.assertNull(resource.getToken( "unsecured_ko_2"));
	}
	@Test
	void getTokenWrongPlain2() {
		Assertions.assertNull(resource.getToken( "unsecured_ko_3"));
	}

	@Test
	void checkWrongPlain2() {
		Assertions.assertFalse(resource.check(DEFAULT_USER, "unsecured_ko_3"));
	}
	@Test
	void checkInvalidDigest() {
		final var resource = new ApiTokenResource();
		resource.setTokenDigest("any");
		Assertions.assertFalse(resource.check(DEFAULT_USER, TOKEN));
	}

	@Test
	void create() throws GeneralSecurityException {
		Assertions.assertEquals(4, repository.findAllByUser(DEFAULT_USER).size());
		final var tokenObj = resource.create("new-api");
		Assertions.assertEquals("new-api", tokenObj.getName());
		final var token = tokenObj.getId();

		// Check new state
		final var newToken = repository.findByNameExpected("new-api");
		Assertions.assertNotNull(token);
		Assertions.assertEquals(DEFAULT_USER, newToken.getUser());
		Assertions.assertNotNull(newToken.getHash());
		final var tokens = resource.getTokenNames();
		Assertions.assertEquals(5, tokens.size());
		Assertions.assertEquals("new-api", tokens.get(1));
		Assertions.assertEquals(token, resource.getToken("new-api"));
		Assertions.assertTrue(resource.hasToken(DEFAULT_USER, "new-api"));
	}

	@Test
	void updateNotExist() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> resource.update("any"));
	}

	@Test
	void update() throws GeneralSecurityException {
        var tokens = resource.getTokenNames();
		Assertions.assertEquals(4, tokens.size());
		Assertions.assertEquals("name", tokens.getFirst());
		final var token = resource.update("name");

		// Check new state
		Assertions.assertNotNull(token);
		tokens = resource.getTokenNames();
		Assertions.assertEquals(4, tokens.size());
		Assertions.assertEquals("name", tokens.getFirst());
		Assertions.assertEquals(token, resource.getToken("name"));
		final var newToken = repository.findByUserAndName(DEFAULT_USER, "name");
		Assertions.assertNotNull(newToken);
		Assertions.assertEquals(DEFAULT_USER, newToken.getUser());
		Assertions.assertNotNull(newToken.getToken());
		Assertions.assertNotNull(newToken.getHash());
	}

	@Test
	void remove() {
		Assertions.assertEquals(4, repository.findAllByUser(DEFAULT_USER).size());
		Assertions.assertTrue(resource.hasToken(DEFAULT_USER, "name"));

		resource.remove("name");

		// Check new state
		repository.findByNameExpected("name");
		Assertions.assertEquals(1, repository.findAllByUser("other").size());
		Assertions.assertEquals(3, repository.findAllByUser(DEFAULT_USER).size());
		Assertions.assertFalse(resource.hasToken(DEFAULT_USER, "new-api"));
	}


	@Test
	void removeAll() {
		Assertions.assertEquals(1, repository.findAllByUser("other").size());
		Assertions.assertEquals(4, repository.findAllByUser(DEFAULT_USER).size());
		Assertions.assertTrue(resource.hasToken(DEFAULT_USER, "name"));

		resource.removeAll(DEFAULT_USER);

		// Check new state
		repository.findByNameExpected("name");
		Assertions.assertEquals(1, repository.findAllByUser("other").size());
		Assertions.assertEquals(0, repository.findAllByUser(DEFAULT_USER).size());
		Assertions.assertFalse(resource.hasToken(DEFAULT_USER, "new-api"));
	}
}
