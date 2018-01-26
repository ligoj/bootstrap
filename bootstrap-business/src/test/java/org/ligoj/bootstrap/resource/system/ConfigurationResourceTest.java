package org.ligoj.bootstrap.resource.system;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import net.sf.ehcache.CacheManager;

/**
 * Test class of {@link ConfigurationResource}
 */
@ExtendWith(SpringExtension.class)
public class ConfigurationResourceTest extends AbstractBootTest {

	@Autowired
	private ConfigurationResource resource;

	@Autowired
	private CryptoHelper cryptoHelper;

	@BeforeAll
	public static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");
	}

	@BeforeEach
	public void prepare() {
		System.setProperty("test-key0", cryptoHelper.encrypt("value0"));
		System.setProperty("test-key1", cryptoHelper.encrypt("value1"));
		System.setProperty("test-key2", "value2");
		System.setProperty("test-key3", "value3");
		System.setProperty("test-key-int", "54");

		final SystemConfiguration entity0 = new SystemConfiguration();
		entity0.setName("test-key0");
		entity0.setValue("value-db0");
		em.persist(entity0);

		final SystemConfiguration entity2 = new SystemConfiguration();
		entity2.setName("test-key2");
		entity2.setValue("value-db2");
		em.persist(entity2);

		final SystemConfiguration entity4 = new SystemConfiguration();
		entity4.setName("test-key4");
		entity4.setValue("value-db4");
		em.persist(entity4);

		final SystemConfiguration entity5 = new SystemConfiguration();
		entity5.setName("test-key5");
		entity5.setValue(cryptoHelper.encrypt("value-db5"));
		em.persist(entity5);

		em.flush();
		CacheManager.getInstance().getCache("configuration").removeAll();
	}

	@AfterEach
	public void clean() {
		CacheManager.getInstance().getCache("configuration").removeAll();
	}

	@Test
	public void get() {
		Assertions.assertEquals("value0", resource.get("test-key0"));
		Assertions.assertEquals("value1", resource.get("test-key1"));
		Assertions.assertEquals("value2", resource.get("test-key2"));
		Assertions.assertEquals("value3", resource.get("test-key3"));
		Assertions.assertEquals("value-db4", resource.get("test-key4"));
		Assertions.assertEquals("value-db5", resource.get("test-key5"));
		Assertions.assertNull(resource.get("test-any"));

		Assertions.assertNotEquals("value-db5", em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key5").getSingleResult().getValue());
	}

	@Test
	public void getInt() {
		Assertions.assertEquals(99, resource.get("test-key-any", 99));
		Assertions.assertEquals(54, resource.get("test-key-int", 77));
	}

	@Test
	public void getDefault() {
		Assertions.assertNull(resource.get("test-key-any"));
		Assertions.assertEquals("99", resource.get("test-key-any", "99"));
		Assertions.assertEquals("54", resource.get("test-key-int"));
		Assertions.assertEquals("54", resource.get("test-key-int", "77"));
	}

	@Test
	public void update() {
		final SystemConfiguration value = em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key5").getSingleResult();
		em.clear();
		Assertions.assertNotEquals("new-value-db5", value.getValue());
		Assertions.assertEquals("value-db5", resource.get("test-key5"));
		resource.saveOrUpdate("test-key5", "new-value-db5");

		// Check the data from the cache
		Assertions.assertEquals("new-value-db5", resource.get("test-key5"));
		SystemConfiguration newValue = em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key5").getSingleResult();

		// Check data is encrypted
		Assertions.assertNotEquals("new-value-db5", newValue.getValue());
		Assertions.assertEquals("new-value-db5", cryptoHelper.decrypt(newValue.getValue()));

		// Check previous id is kept
		Assertions.assertEquals(value.getId(), newValue.getId());

		// Check persistence of data and cache
		CacheManager.getInstance().getCache("configuration").removeAll();
		Assertions.assertEquals("new-value-db5", resource.get("test-key5"));
	}

	@Test
	public void create() {
		resource.saveOrUpdate("test-key6", "new-value-db6");

		// Check the data from the cache
		Assertions.assertEquals("new-value-db6", resource.get("test-key6"));
		SystemConfiguration newValue = em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key6").getSingleResult();

		// Check data is encrypted
		Assertions.assertNotEquals("new-value-db6", newValue.getValue());
		Assertions.assertEquals("new-value-db6", cryptoHelper.decrypt(newValue.getValue()));

		// Check persistence of data and cache
		CacheManager.getInstance().getCache("configuration").removeAll();
		Assertions.assertEquals("new-value-db6", resource.get("test-key6"));
	}

	@Test
	public void delete() {
		Assertions.assertEquals("value-db5", resource.get("test-key5"));
		resource.delete("test-key5");

		// Check the data from the cache
		Assertions.assertNull(resource.get("test-key5"));

		// Check persistence of data and cache
		CacheManager.getInstance().getCache("configuration").removeAll();
		Assertions.assertNull(resource.get("test-key5"));
		Assertions.assertTrue(em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class).setParameter("name", "test-key6")
				.getResultList().isEmpty());
	}

}
