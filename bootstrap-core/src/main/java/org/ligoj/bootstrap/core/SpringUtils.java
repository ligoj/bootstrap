/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Utility class to get Spring context (and instantiate bean without auto wiring).
 */
public class SpringUtils implements ApplicationContextAware { // NOPMD -- bug PMD

	/**
	 * Spring application context.
	 */
	@Getter
	private static ApplicationContext applicationContext;

	@Override
	@Autowired
	public void setApplicationContext(final ApplicationContext applicationContext) {
		setSharedApplicationContext(applicationContext);
	}

	/**
	 * Set shared application context.
	 *
	 * @param applicationContext Shared Spring application context.
	 */
	public static void setSharedApplicationContext(final ApplicationContext applicationContext) {
		SpringUtils.applicationContext = applicationContext;
	}

	/**
	 * Get a bean in the spring factory by name and class.
	 * Return an instance, which may be shared or independent, of the specified bean.
	 *
	 * @param beanClass the bean class.
	 * @param <T>       the bean type.
	 * @return the initialized spring bean.
	 */
	public static <T> T getBean(final Class<T> beanClass) {
		return applicationContext.getBean(beanClass);
	}

}
