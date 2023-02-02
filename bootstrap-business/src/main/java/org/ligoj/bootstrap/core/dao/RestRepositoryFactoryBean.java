/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.io.Serializable;

import jakarta.persistence.EntityManager;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import lombok.extern.slf4j.Slf4j;

/**
 * Repository factory.
 * 
 * @param <R>
 *            Repository
 * @param <T>
 *            Entity type.
 * @param <K>
 *            Entity's key type.
 */
@Slf4j
public class RestRepositoryFactoryBean<R extends JpaRepository<T, K>, T, K extends Serializable>
		extends JpaRepositoryFactoryBean<R, T, K> implements ApplicationContextAware {

	/**
	 * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
	 * 
	 * @param repositoryInterface must not be {@literal null}.
	 */
	public RestRepositoryFactoryBean(final Class<? extends R> repositoryInterface) {
		super(repositoryInterface);
	}

	private ApplicationContext applicationContext;
	
	/**
	 * Flag for the last invocations of {@link AfterJpaBeforeSpringDataListener}
	 */
	private static long lastListenerInvocation = 0;

	@Override
	protected RepositoryFactorySupport createRepositoryFactory(final EntityManager entityManager) {
		return new RestRepositoryFactory(entityManager);
	}

	private static class RestRepositoryFactory extends JpaRepositoryFactory {

		/**
		 * Constructor called by Spring-Data
		 * 
		 * @param entityManager
		 *            injected entity manager.
		 */
		RestRepositoryFactory(final EntityManager entityManager) {
			super(entityManager);
		}

		@Override
		protected Class<?> getRepositoryBaseClass(final RepositoryMetadata metadata) {

			// The RepositoryMetadata can be safely ignored, it is used by the
			// JpaRepositoryFactory
			// to check for QueryDslJpaRepository's which is out of scope.
			return RestRepositoryImpl.class;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean#
	 * afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		if (applicationContext.getStartupDate() != lastListenerInvocation) {
			// Invoke pre-spring-data listeners
			log.info("Notify EMF is ready before parsing Spring-Data queries");
			lastListenerInvocation = applicationContext.getStartupDate();
			applicationContext.getBeansOfType(AfterJpaBeforeSpringDataListener.class).values()
					.forEach(AfterJpaBeforeSpringDataListener::callback);
		}
		super.afterPropertiesSet();
	}
	
	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}