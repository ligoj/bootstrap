/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.security.SecurityHelper;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Validates the {@link SystemUser#IS_ADMIN} SpEL bind parameter: it resolves the {@code @securityHelper} bean and binds
 * the current principal administrator status on <em>each</em> execution, so it is correct even though Hibernate
 * memoizes the translated SQL of a query plan.
 */
@ExtendWith(SpringExtension.class)
class SystemUserIsAdminTest extends AbstractBootTest {

	@Autowired
	private SystemUserTestRepository repository;

	private void persistUser() {
		final var user = new SystemUser();
		user.setLogin("admin-test");
		em.persist(user);
		em.flush();
		em.clear();
	}

	/**
	 * Same query, same JVM (cached query plan), but a different principal between the two executions: the second
	 * execution must re-evaluate the bind parameter and return the opposite result. This is what a render-time
	 * Hibernate function could not guarantee.
	 */
	@Test
	void isAdminReevaluatedPerPrincipal() {
		persistUser();

		initSpringSecurityContext("admin-test", new SimpleGrantedAuthority("USER"),
				new SimpleGrantedAuthority(SecurityHelper.ADMIN));
		Assertions.assertTrue(repository.isAdmin("admin-test"));

		initSpringSecurityContext("admin-test", new SimpleGrantedAuthority("USER"));
		Assertions.assertFalse(repository.isAdmin("admin-test"));
	}

	/**
	 * The administrator clause composes with the other named parameters of the query.
	 */
	@Test
	void isAdminWithUnmatchedUser() {
		persistUser();
		initSpringSecurityContext("admin-test", new SimpleGrantedAuthority("USER"),
				new SimpleGrantedAuthority(SecurityHelper.ADMIN));
		Assertions.assertFalse(repository.isAdmin("unknown"));
	}
}
