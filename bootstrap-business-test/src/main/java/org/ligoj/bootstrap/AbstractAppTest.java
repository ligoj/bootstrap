/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.ligoj.bootstrap.core.security.SecurityHelper;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Basic Application test support.
 */
public abstract class AbstractAppTest extends AbstractJpaTest {

	/**
	 * Persist system user, role and assignment granting an administrative API authorization ({@code .*}) to
	 * {@code DEFAULT_USER}, and reflect that administration access level on the current security context by granting it
	 * the {@value org.ligoj.bootstrap.core.security.SecurityHelper#ADMIN} virtual authority &mdash; exactly as
	 * {@code RbacUserDetailsService} does at authentication time. This keeps the "{@code DEFAULT_USER} is an
	 * administrator" contract working with the {@link org.ligoj.bootstrap.model.system.SystemUser#IS_ADMIN} principal
	 * based check. Tests requiring a non-administrator principal override it afterwards with
	 * {@code initSpringSecurityContext(otherUser)}.
	 */
	protected void persistSystemEntities() {
		final var role = new SystemRole();
		role.setName("some");
		em.persist(role);
		final var user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);
		final var authorization = new SystemAuthorization();
		authorization.setType(AuthorizationType.API);
		authorization.setPattern(".*");
		authorization.setRole(role);
		em.persist(authorization);
		final var assignment = new SystemRoleAssignment();
		assignment.setRole(role);
		assignment.setUser(user);
		em.persist(assignment);

		// Reflect the administration access level on the principal, as the production authentication would
		initSpringSecurityContext(DEFAULT_USER, new SimpleGrantedAuthority(SecurityHelper.ADMIN));
	}

	/**
	 * Destroy the given bean instance (usually a prototype instance obtained from
	 * this factory) according to its bean definition.
	 * <p>
	 * Any exception that arises during destruction should be caught and logged
	 * instead of propagated to the caller of this method.
	 *
	 * @param beanName
	 *            The name of the bean definition
	 */
	protected void destroySingleton(final String beanName) {
		try {
			((DefaultSingletonBeanRegistry) ((ConfigurableApplicationContext) applicationContext).getBeanFactory())
					.destroySingleton(beanName);
		} catch (final NoSuchBeanDefinitionException e) {
			// Ignore
		}
	}

	/**
	 * Register a singleton within the current application context. Don't forget to
	 * destroy this singleton with a try-finally at the end of your tests.
	 *
	 * @param <T> Singleton type.
	 * @param beanName
	 *            the name of the bean definition.
	 * @param singleton
	 *            the bean instance to register
	 * @return The given instance.
	 * @see #destroySingleton(String)
	 */
	protected <T> T registerSingleton(final String beanName, final T singleton) {
		((SingletonBeanRegistry) applicationContext.getAutowireCapableBeanFactory()).registerSingleton(beanName, singleton);
		return singleton;
	}
}
