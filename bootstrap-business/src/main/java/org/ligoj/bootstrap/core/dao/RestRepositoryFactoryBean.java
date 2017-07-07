package org.ligoj.bootstrap.core.dao;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

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
public class RestRepositoryFactoryBean<R extends JpaRepository<T, K>, T, K extends Serializable>
		extends JpaRepositoryFactoryBean<R, T, K> implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static boolean listenersCalled = false;

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
		if (!listenersCalled) {
			// Invoke pre-spring-data listeners
			listenersCalled = true;
			applicationContext.getBeansOfType(AfterJpaBeforeSpringDataListener.class).values()
					.forEach(AfterJpaBeforeSpringDataListener::callback);
		}
		super.afterPropertiesSet();
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}