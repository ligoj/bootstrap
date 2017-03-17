package org.ligoj.bootstrap.core.dao;

import java.io.Serializable;

import javax.persistence.EntityManager;

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
public class RestRepositoryFactoryBean<R extends JpaRepository<T, K>, T, K extends Serializable> extends JpaRepositoryFactoryBean<R, T, K> {

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

			// The RepositoryMetadata can be safely ignored, it is used by the JpaRepositoryFactory
			// to check for QueryDslJpaRepository's which is out of scope.
			return RestRepositoryImpl.class;
		}
	}
}