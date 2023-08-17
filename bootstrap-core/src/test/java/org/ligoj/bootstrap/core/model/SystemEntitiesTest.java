/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemApiToken;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemBench;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.ligoj.bootstrap.model.system.SystemDialect;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.model.system.SystemUserSetting;

import jodd.bean.BeanCopy;
import lombok.EqualsAndHashCode;

/**
 * Test all system entities. (not really test)
 */
class SystemEntitiesTest {

	@Test
    void testSystemEntities()
			throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		testEntity(SystemRole.class);
		testEntity(SystemBench.class);
		testEntity(SystemRoleAssignment.class);
		testEntity(SystemUser.class);
		testEntity(SystemAuthorization.class);
		testEntity(SystemDialect.class);
		testEntity(SystemUserSetting.class);
		testEntity(SystemConfiguration.class);
		testEntity(SystemApiToken.class);
	}

	@Test
    void testSystemRole() {
		final var systemRole = new SystemRole();
		systemRole.setName("name");
		Assertions.assertSame("name", systemRole.getAuthority());
		SystemAuthorization.AuthorizationType.values();
		SystemAuthorization.AuthorizationType.valueOf(SystemAuthorization.AuthorizationType.API.name());
	}

	@Test
    void testSystemUser() {
		final var systemUser = new SystemUser();
		Assertions.assertFalse(systemUser.equals(null));
		Assertions.assertTrue(systemUser.equals(new SystemUser()));
		Assertions.assertTrue(systemUser.equals(systemUser));
		Assertions.assertEquals("SystemUser(login=null)", systemUser.toString());
		Assertions.assertTrue(systemUser.hashCode() != 0);
		systemUser.setLogin("name");
		Assertions.assertFalse(systemUser.equals(null));
		Assertions.assertFalse(systemUser.equals(new SystemUser()));
		Assertions.assertFalse(new SystemUser().equals(systemUser));
		Assertions.assertEquals("SystemUser(login=name)", systemUser.toString());
		Assertions.assertFalse(systemUser.hashCode() == 0);

		final var other = new SystemUser();
		other.setLogin("other");
		Assertions.assertFalse(systemUser.equals(other));
		other.setLogin("name");
		Assertions.assertTrue(systemUser.equals(other));
		Assertions.assertFalse(systemUser.equals(new DummySystemUser()));
	}

	@EqualsAndHashCode(of = "other", callSuper = true)
	private static class DummySystemUser extends SystemUser {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String other;
	}

	/**
	 * Test entity getter/setter and toString.
	 */
	private <T> void testEntity(final Class<T> clazz)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final var entity = clazz.getDeclaredConstructor().newInstance();
		if (entity instanceof Auditable) {
			@SuppressWarnings("unchecked")
			final var auditable = (Auditable<?, ?, Date>) entity;
			auditable.setLastModifiedDate(new Date());
			auditable.setCreatedDate(new Date());
		}
		final var clone = entity.getClass().getDeclaredConstructor().newInstance();
		BeanCopy.fromBean(entity).toBean(clone);
	}

}
