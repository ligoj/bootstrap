/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Test class of {@link AbstractAppTest}
 */
public class TestAbstractAppTest extends AbstractAppTest {

	/**
	 * Only there fore coverage, no Spring involved.
	 */
	@Test
	public void coverage() {
		em = Mockito.mock(EntityManager.class);
		persistSystemEntities();
	}

	@Test
	public void testRegisterSingleton() {
		final ConfigurableApplicationContext applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
		final DefaultListableBeanFactory registry = Mockito.mock(DefaultListableBeanFactory.class);
		Mockito.when(applicationContext.getBeanFactory()).thenReturn(registry);
		Mockito.when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(registry);
		this.applicationContext = applicationContext;
		registerSingleton("my_dynamical_bean", null);
		destroySingleton("my_dynamical_bean");
	}

	@Test
	public void testDestroySingletonNotExist() {
		final ConfigurableApplicationContext applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
		final DefaultListableBeanFactory registry = Mockito.mock(DefaultListableBeanFactory.class);
		Mockito.when(applicationContext.getBeanFactory()).thenReturn(registry);
		Mockito.doThrow(NoSuchBeanDefinitionException.class).when(registry).destroySingleton("my_dynamical_bean");
		this.applicationContext = applicationContext;
		destroySingleton("my_dynamical_bean");
	}

	@Test
	public void testClearAllCache() {
		cacheManager = Mockito.mock(org.springframework.cache.CacheManager.class);
		final Cache cache = Mockito.mock(Cache.class);
		final Collection<String> caches = Collections.singletonList("sample");
		Mockito.doReturn(cache).when(cacheManager).getCache("sample");
		Mockito.doReturn(caches).when(cacheManager).getCacheNames();
		clearAllCache();
		Mockito.verify(cache).clear();
	}

}
