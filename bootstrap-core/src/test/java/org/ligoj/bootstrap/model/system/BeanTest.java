/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

/**
 * Test class of bean.
 */
public class BeanTest {

	@Test
	void testSystemDialect() throws ReflectiveOperationException {
		testPojo(SystemDialect.class);
		testPojo(SystemApiToken.class);
		testPojo(SystemBench.class);
		testPojo(SystemUserSetting.class);
		testPojo(SystemConfiguration.class);
		testPojo(SystemRoleAssignment.class);
		testPojo(SystemAuthorization.class);
		testPojo(SystemRole.class);
		testPojo(SystemUser.class);
	}

	protected void testPojo(Class<?> pojo) throws ReflectiveOperationException {
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
	}

}
