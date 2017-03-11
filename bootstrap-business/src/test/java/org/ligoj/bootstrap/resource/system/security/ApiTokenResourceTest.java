package org.ligoj.bootstrap.resource.system.security;

import java.security.GeneralSecurityException;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.transaction.Transactional;

import org.ligoj.bootstrap.AbstractJpaTest;
import org.ligoj.bootstrap.dao.system.SystemApiTokenRepository;
import org.ligoj.bootstrap.model.system.SystemApiToken;
import org.ligoj.bootstrap.resource.system.api.ApiTokenResource;

/**
 * Test class of {@link ApiTokenResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/jpa-context-test.xml", "classpath:/META-INF/spring/business-context-test.xml" })
@Rollback
@Transactional
public class ApiTokenResourceTest extends AbstractJpaTest {

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

	@Before
	public void setUp2() {
		SystemApiToken entity = new SystemApiToken();
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
	public void getToken() throws GeneralSecurityException {
		final String tokens = resource.getToken("name");
		Assert.assertEquals(TOKEN, tokens);
	}

	@Test
	public void getTokenNames() {
		final List<String> tokensName = resource.getTokenNames();
		Assert.assertEquals(1, tokensName.size());
		Assert.assertEquals("name", tokensName.get(0));
	}

	@Test
	public void check() {
		Assert.assertTrue("name", resource.check(DEFAULT_USER, TOKEN));
	}

	@Test
	public void checkNoUser() {
		Assert.assertFalse("name", resource.check("any", TOKEN));
	}

	@Test
	public void checkWrongUser() {
		Assert.assertFalse("name", resource.check("other", TOKEN));
	}

	@Test
	public void checkWrongKey() {
		Assert.assertFalse("name", resource.check(DEFAULT_USER, TOKEN2));
	}

	@Test
	public void checkInvalidDigest() {
		final ApiTokenResource resource = new ApiTokenResource();
		resource.setTokenDigest("any");
		Assert.assertFalse("name", resource.check(DEFAULT_USER, TOKEN));
	}

	@Test
	public void create() throws GeneralSecurityException {
		Assert.assertEquals(1, repository.findAllByUser(DEFAULT_USER).size());
		final String token = resource.create("new-api");

		// Check new state
		final SystemApiToken newToken = repository.findByNameExpected("new-api");
		Assert.assertNotNull(token);
		Assert.assertEquals(DEFAULT_USER, newToken.getUser());
		Assert.assertNotNull(newToken.getHash());
		final List<String> tokens = resource.getTokenNames();
		Assert.assertEquals(2, tokens.size());
		Assert.assertEquals("new-api", tokens.get(1));
		Assert.assertEquals(token, resource.getToken("new-api"));
	}

	@Test(expected = EntityNotFoundException.class)
	public void updateNotExist() throws GeneralSecurityException {
		resource.update("any");
	}

	@Test
	public void update() throws GeneralSecurityException {
		List<String> tokens = resource.getTokenNames();
		Assert.assertEquals(1, tokens.size());
		Assert.assertEquals("name", tokens.get(0));
		final String token = resource.update("name");

		// Check new state
		Assert.assertNotNull(token);
		tokens = resource.getTokenNames();
		Assert.assertEquals(1, tokens.size());
		Assert.assertEquals("name", tokens.get(0));
		Assert.assertEquals(token, resource.getToken("name"));
		final SystemApiToken newToken = repository.findByUserAndName(DEFAULT_USER, "name");
		Assert.assertNotNull(newToken);
		Assert.assertEquals(DEFAULT_USER, newToken.getUser());
		Assert.assertNotNull(newToken.getToken());
		Assert.assertNotNull(newToken.getHash());
	}

	@Test
	public void remove() {
		Assert.assertEquals(1, repository.findAllByUser(DEFAULT_USER).size());
		resource.remove("name");

		// Check new state
		repository.findByNameExpected("name");
		Assert.assertEquals(1, repository.findAllByUser("other").size());
		Assert.assertEquals(0, repository.findAllByUser(DEFAULT_USER).size());
	}
}
