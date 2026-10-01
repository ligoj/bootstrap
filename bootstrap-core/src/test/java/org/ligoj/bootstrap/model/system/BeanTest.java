/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.model.AbstractStringKeyEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;

/**
 * Test class of bean.
 */
class BeanTest {

	@Test
	void beans() throws ReflectiveOperationException {
		testPojo(SystemDialect.class).toString();
		testPojo(SystemApiToken.class).toString();
		testPojo(SystemBench.class).toString();
		testPojo(SystemUserSetting.class).toString();
		testPojo(SystemConfiguration.class).toString();
		testPojo(SystemRoleAssignment.class).toString();
		testPojo(SystemAuthorization.class).toString();
		testPojo(SystemRole.class).toString();
		testPojo(SystemUser.class).toString();
		testPojo(SystemHook.class).toString();
		testPojo(HookMatch.class).toString();
		Assertions.assertTrue(new AbstractStringKeyEntity(){}.isNew());

	}

	@Test
	void mfaDevice() {
		// Custom boolean accessors of a nullable column: not handled by "testPojo"
		final var device = new SystemMfaDevice();
		Assertions.assertEquals(SystemMfaDevice.TYPE_TOTP, device.getType());
		Assertions.assertFalse(device.isDefaultDevice());
		device.setDefaultDevice(true);
		Assertions.assertTrue(device.isDefaultDevice());
		device.setDefaultDevice(false);
		Assertions.assertFalse(device.isDefaultDevice());
		device.setUser("user");
		device.setName("phone");
		device.setType(SystemMfaDevice.TYPE_PASSKEY);
		device.setSecret("secret");
		device.setLastUsed(Instant.EPOCH);
		Assertions.assertEquals("user", device.getUser());
		Assertions.assertEquals(SystemMfaDevice.TYPE_PASSKEY, device.getType());
		Assertions.assertEquals("secret", device.getSecret());
		Assertions.assertEquals(Instant.EPOCH, device.getLastUsed());
		Assertions.assertEquals("SystemMfaDevice(user=user)", device.toString());
	}

	protected <T> T testPojo(Class<T> pojo) throws ReflectiveOperationException {
		var bean = pojo.getConstructor().newInstance();
		for (final Field field : FieldUtils.getAllFields(pojo)) {
			if (field.getName().contains("$") || Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			final var setter = "set" + StringUtils.capitalize(field.getName());
			var getter = "get" + StringUtils.capitalize(field.getName());
			Object param = null;
			var type = field.getType();
			if (type.equals(double.class)) {
				param = 0D;
			} else if (type.equals(boolean.class)) {
				getter = "is" + StringUtils.capitalize(field.getName());
				param = false;
			}

			pojo.getMethod(setter, type).invoke(bean, param);
			pojo.getMethod(getter).invoke(bean);
		}
		return bean;
	}

}
