/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.configuration;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link ConfigurationResource}
 */
@ExtendWith(SpringExtension.class)
public class ConfigurationResourceTest extends AbstractBootTest {

	@Autowired
	private ConfigurationResource resource;

	@Autowired
	private CryptoHelper cryptoHelper;

	@Autowired
	private CacheManager cacheManager;

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
		System.setProperty("test-key6", "  ");

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

		final SystemConfiguration entity6 = new SystemConfiguration();
		entity6.setName("test-key6");
		entity6.setValue("value-db6");
		em.persist(entity6);

		em.flush();
		cacheManager.getCache("configuration").clear();
	}

	@AfterEach
	public void clean() {
		cacheManager.getCache("configuration").clear();
	}

	@Test
	public void get() {
		Assertions.assertEquals("value0", resource.get("test-key0"));
		Assertions.assertEquals("value1", resource.get("test-key1"));
		Assertions.assertEquals("value2", resource.get("test-key2"));
		Assertions.assertEquals("value3", resource.get("test-key3"));
		Assertions.assertEquals("value-db4", resource.get("test-key4"));
		Assertions.assertEquals("value-db5", resource.get("test-key5"));
		Assertions.assertEquals("value-db6", resource.get("test-key6"));
		Assertions.assertNull(resource.get("test-any"));

		Assertions.assertNotEquals("value-db5",
				em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
						.setParameter("name", "test-key5").getSingleResult().getValue());
	}

	@Test
	public void findAll() {
		final Map<String, ConfigurationVo> result = new HashMap<>();
		resource.findAll().forEach(vo -> result.put(vo.getName(), vo));
		Assertions.assertNull(result.get("test-key0").getValue());
		Assertions.assertTrue(result.get("test-key0").isSecured());
		Assertions.assertFalse(result.get("test-key0").isPersisted());
		Assertions.assertTrue(result.get("test-key0").isOverride());
		Assertions.assertEquals("systemProperties", result.get("test-key0").getSource());
		Assertions.assertNull(result.get("test-key1").getValue());
		Assertions.assertFalse(result.get("test-key1").isOverride());
		Assertions.assertEquals("value2", result.get("test-key2").getValue());
		Assertions.assertFalse(result.get("test-key2").isPersisted());
		Assertions.assertFalse(result.get("test-key2").isSecured());
		Assertions.assertEquals("value3", result.get("test-key3").getValue());
		Assertions.assertEquals("value-db4", result.get("test-key4").getValue());
		Assertions.assertEquals("database", result.get("test-key4").getSource());
		Assertions.assertTrue(result.get("test-key4").isPersisted());
		Assertions.assertFalse(result.get("test-key4").isSecured());
		Assertions.assertNull(result.get("test-key5").getValue());
		Assertions.assertTrue(result.get("test-key5").isPersisted());
		Assertions.assertEquals("database", result.get("test-key5").getSource());
		Assertions.assertNull(resource.get("test-any"));
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
	public void put() {
		final SystemConfiguration value = em
				.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key5").getSingleResult();
		em.clear();
		Assertions.assertNotEquals("new-value-db5", value.getValue());
		Assertions.assertEquals("value-db5", resource.get("test-key5"));
		resource.put("test-key5", "new-value-db5");

		// Check the data from the cache
		Assertions.assertEquals("new-value-db5", resource.get("test-key5"));
		SystemConfiguration newValue = em
				.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-key5").getSingleResult();

		// Check data is encrypted
		Assertions.assertNotEquals("new-value-db5", newValue.getValue());
		Assertions.assertEquals("new-value-db5", cryptoHelper.decrypt(newValue.getValue()));

		// Check previous id is kept
		Assertions.assertEquals(value.getId(), newValue.getId());

		// Check persistence of data and cache
		cacheManager.getCache("configuration").clear();
		Assertions.assertEquals("new-value-db5", resource.get("test-key5"));
	}

	@Test
	public void putOverrideSystem() {
		// Initial value
		Assertions.assertNull(resource.get("test-key44"));
		Assertions.assertNull(System.getProperty("test-key44"));

		try {
			// Change the value
			resource.put("test-key44", "new-value-db4");

			// Check the data from the cache
			Assertions.assertEquals("new-value-db4", resource.get("test-key44"));
			Assertions.assertEquals("new-value-db4", System.getProperty("test-key44"));
		} finally {
			System.clearProperty("test-key44");
		}
	}

	@Test
	public void create() {
		resource.put("test-keyX", "new-value-dbX");

		// Check the data from the cache
		Assertions.assertEquals("new-value-dbX", resource.get("test-keyX"));
		SystemConfiguration newValue = em
				.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-keyX").getSingleResult();

		// Check data is encrypted
		Assertions.assertNotEquals("new-value-dbX", newValue.getValue());
		Assertions.assertEquals("new-value-dbX", cryptoHelper.decrypt(newValue.getValue()));

		// Check persistence of data and cache
		cacheManager.getCache("configuration").clear();
		Assertions.assertEquals("new-value-dbX", resource.get("test-keyX"));
	}

	@Test
	public void delete() {
		Assertions.assertEquals("value-db5", resource.get("test-key5"));
		resource.delete("test-key5");

		// Check the data from the cache
		Assertions.assertNull(resource.get("test-key5"));

		// Check persistence of data and cache
		cacheManager.getCache("configuration").clear();
		Assertions.assertNull(resource.get("test-key5"));
		Assertions.assertTrue(em.createQuery("FROM SystemConfiguration WHERE name=:name", SystemConfiguration.class)
				.setParameter("name", "test-keyX").getResultList().isEmpty());
	}

	/**
	 * Not enumerable test
	 */
	@Test
	public void findAllNotEnumerable() {
		final ConfigurationResource resource = new ConfigurationResource();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
		resource.env = Mockito.mock(ConfigurableEnvironment.class);
		final MutablePropertySources mutablePropertySources = new MutablePropertySources();
		mutablePropertySources.addFirst(Mockito.mock(PropertySource.class));
		Mockito.when(resource.env.getPropertySources()).thenReturn(mutablePropertySources);
		final Map<String, ConfigurationVo> result = new HashMap<>();
		resource.findAll().forEach(vo -> result.put(vo.getName(), vo));
		Assertions.assertEquals("value-db0", result.get("test-key0").getValue());
		Assertions.assertEquals("value-db2", result.get("test-key2").getValue());
		Assertions.assertEquals("value-db4", result.get("test-key4").getValue());
		Assertions.assertNull(result.get("test-key5").getValue());
		Assertions.assertEquals("value-db6", result.get("test-key6").getValue());
		Assertions.assertEquals(5, result.size());
	}

}
