/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Test class of {@link AbstractAppTest}
 */
class TestAbstractAppTest extends AbstractAppTest {

	/**
	 * Only there for coverage, no Spring involved.
	 */
	@Test
	void coverage() {
		em = mock(EntityManager.class);
		persistSystemEntities();
	}

	@Test
	void testRegisterSingleton() {
		final var applicationContext = mock(ConfigurableApplicationContext.class);
		final var registry = mock(DefaultListableBeanFactory.class);
		when(applicationContext.getBeanFactory()).thenReturn(registry);
		when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(registry);
		this.applicationContext = applicationContext;
		registerSingleton("my_dynamical_bean", null);
		destroySingleton("my_dynamical_bean");
	}

	@Test
	void testDestroySingletonNotExist() {
		final var applicationContext = mock(ConfigurableApplicationContext.class);
		final var registry = mock(DefaultListableBeanFactory.class);
		when(applicationContext.getBeanFactory()).thenReturn(registry);
		doThrow(NoSuchBeanDefinitionException.class).when(registry).destroySingleton("my_dynamical_bean");
		this.applicationContext = applicationContext;
		destroySingleton("my_dynamical_bean");
	}

	@Test
	void testClearAllCache() {
		cacheManager = mock(org.springframework.cache.CacheManager.class);
		final var cache = mock(Cache.class);
		final Collection<String> caches = Collections.singletonList("sample");
		doReturn(cache).when(cacheManager).getCache("sample");
		doReturn(caches).when(cacheManager).getCacheNames();
		clearAllCache();
		verify(cache).clear();
	}

}
