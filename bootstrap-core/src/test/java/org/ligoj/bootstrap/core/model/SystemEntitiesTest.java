package org.ligoj.bootstrap.core.model;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Auditable;

import org.ligoj.bootstrap.model.system.SystemApiToken;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemBench;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.ligoj.bootstrap.model.system.SystemDialect;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.model.system.SystemUserSetting;
import lombok.EqualsAndHashCode;

/**
 * Test all system entities. (not really test)
 */
public class SystemEntitiesTest {

	@Test
	public void testSystemEntities() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
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
	public void testSystemRole() {
		final SystemRole systemRole = new SystemRole();
		systemRole.setName("name");
		Assert.assertSame("name", systemRole.getAuthority());
		SystemAuthorization.AuthorizationType.values();
		SystemAuthorization.AuthorizationType.valueOf(SystemAuthorization.AuthorizationType.BUSINESS.name());
	}

	@Test
	public void testSystemUser() {
		final SystemUser systemUser = new SystemUser();
		Assert.assertFalse(systemUser.equals(null));
		Assert.assertTrue(systemUser.equals(new SystemUser()));
		Assert.assertTrue(systemUser.equals(systemUser));
		Assert.assertEquals("SystemUser(login=null)", systemUser.toString());
		Assert.assertTrue(systemUser.hashCode() != 0);
		systemUser.setLogin("name");
		Assert.assertFalse(systemUser.equals(null));
		Assert.assertFalse(systemUser.equals(new SystemUser()));
		Assert.assertFalse(new SystemUser().equals(systemUser));
		Assert.assertEquals("SystemUser(login=name)", systemUser.toString());
		Assert.assertTrue(systemUser.hashCode() != 0);

		final SystemUser other = new SystemUser();
		other.setLogin("other");
		Assert.assertFalse(systemUser.equals(other));
		other.setLogin("name");
		Assert.assertTrue(systemUser.equals(other));
		Assert.assertFalse(systemUser.equals(new DummySystemUser()));
	}

	@EqualsAndHashCode(of = "other", callSuper = true)
	private class DummySystemUser extends SystemUser {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String other;
	}

	/**
	 * Test entity getter/setter and toString.
	 */
	private <T> void testEntity(final Class<T> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		final T entity = clazz.newInstance();
		if (entity instanceof Auditable) {
			final Auditable<?, ?> auditable = (Auditable<?, ?>) entity;
			auditable.setLastModifiedDate(new DateTime());
			auditable.setCreatedDate(new DateTime());
		}
		BeanUtilsBean.getInstance().cloneBean(entity).toString();
	}

}
