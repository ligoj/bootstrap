/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.util.Collection;
import java.util.Collections;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Test class of {@link AbstractAppTest}
 */
class TestAbstractAppTest extends AbstractAppTest {

	/**
	 * Only there fore coverage, no Spring involved.
	 */
	@Test
	void coverage() {
		em = Mockito.mock(EntityManager.class);
		persistSystemEntities();
	}

	@Test
	void testRegisterSingleton() {
		final var applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
		final var registry = Mockito.mock(DefaultListableBeanFactory.class);
		Mockito.when(applicationContext.getBeanFactory()).thenReturn(registry);
		Mockito.when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(registry);
		this.applicationContext = applicationContext;
		registerSingleton("my_dynamical_bean", null);
		destroySingleton("my_dynamical_bean");
	}

	@Test
	void testDestroySingletonNotExist() {
		final var applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
		final var registry = Mockito.mock(DefaultListableBeanFactory.class);
		Mockito.when(applicationContext.getBeanFactory()).thenReturn(registry);
		Mockito.doThrow(NoSuchBeanDefinitionException.class).when(registry).destroySingleton("my_dynamical_bean");
		this.applicationContext = applicationContext;
		destroySingleton("my_dynamical_bean");
	}

	@Test
	void testClearAllCache() {
		cacheManager = Mockito.mock(org.springframework.cache.CacheManager.class);
		final var cache = Mockito.mock(Cache.class);
		final Collection<String> caches = Collections.singletonList("sample");
		Mockito.doReturn(cache).when(cacheManager).getCache("sample");
		Mockito.doReturn(caches).when(cacheManager).getCacheNames();
		clearAllCache();
		Mockito.verify(cache).clear();
	}

}
