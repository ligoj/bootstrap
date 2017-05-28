package org.ligoj.bootstrap.resource.system;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.sf.ehcache.CacheManager;

/**
 * Test class of {@link ConfigurationResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ConfigurationResourceTest extends AbstractBootTest {

	@Autowired
	private ConfigurationResource resource;

	@Autowired
	private CryptoHelper cryptoHelper;

	@BeforeClass
	public static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");
	}

	@Before
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

	@After
	public void clean() {
		CacheManager.getInstance().getCache("configuration").removeAll();
	}

	@Test
	public void get() {
		Assert.assertEquals("value0", resource.get("test-key0"));
		Assert.assertEquals("value1", resource.get("test-key1"));
		Assert.assertEquals("value2", resource.get("test-key2"));
		Assert.assertEquals("value3", resource.get("test-key3"));
		Assert.assertEquals("value-db4", resource.get("test-key4"));
		Assert.assertEquals("value-db5", resource.get("test-key5"));
		Assert.assertNull(resource.get("test-any"));

		Assert.assertNotEquals("value-db5", em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key5").getSingleResult().getValue());
	}

	@Test
	public void getInt() {
		Assert.assertEquals(99, resource.get("test-key-any", 99));
		Assert.assertEquals(54, resource.get("test-key-int", 77));
	}

	@Test
	public void getDefault() {
		Assert.assertNull(resource.get("test-key-any"));
		Assert.assertEquals("99", resource.get("test-key-any", "99"));
		Assert.assertEquals("54", resource.get("test-key-int"));
		Assert.assertEquals("54", resource.get("test-key-int", "77"));
	}

	@Test
	public void update() {
		final SystemConfiguration value = em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key5").getSingleResult();
		em.clear();
		Assert.assertNotEquals("new-value-db5", value.getValue());
		Assert.assertEquals("value-db5", resource.get("test-key5"));
		resource.saveOrUpdate("test-key5", "new-value-db5");

		// Check the data from the cache
		Assert.assertEquals("new-value-db5", resource.get("test-key5"));
		SystemConfiguration newValue = em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key5").getSingleResult();

		// Check data is encrypted
		Assert.assertNotEquals("new-value-db5", newValue.getValue());
		Assert.assertEquals("new-value-db5", cryptoHelper.decrypt(newValue.getValue()));

		// Check previous id is kept
		Assert.assertEquals(value.getId(), newValue.getId());

		// Check persistence of data and cache
		CacheManager.getInstance().getCache("configuration").removeAll();
		Assert.assertEquals("new-value-db5", resource.get("test-key5"));
	}

	@Test
	public void create() {
		resource.saveOrUpdate("test-key6", "new-value-db6");

		// Check the data from the cache
		Assert.assertEquals("new-value-db6", resource.get("test-key6"));
		SystemConfiguration newValue = em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key6").getSingleResult();

		// Check data is encrypted
		Assert.assertNotEquals("new-value-db6", newValue.getValue());
		Assert.assertEquals("new-value-db6", cryptoHelper.decrypt(newValue.getValue()));

		// Check persistence of data and cache
		CacheManager.getInstance().getCache("configuration").removeAll();
		Assert.assertEquals("new-value-db6", resource.get("test-key6"));
	}

	@Test
	public void delete() {
		Assert.assertEquals("value-db5", resource.get("test-key5"));
		resource.delete("test-key5");

		// Check the data from the cache
		Assert.assertNull(resource.get("test-key5"));

		// Check persistence of data and cache
		CacheManager.getInstance().getCache("configuration").removeAll();
		Assert.assertNull(resource.get("test-key5"));
		Assert.assertTrue(em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class).setParameter("name", "test-key6")
				.getResultList().isEmpty());
	}

}
