/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemDialect;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.model.system.SystemUserSetting;

/**
 * Test business keyed entities basic ORM operations : hash code and equals.
 */
class TestAbstractBusinessEntityTest extends AbstractBusinessEntityTest {

	/**
	 * Test equals and hash code operation with all possible combinations with only one identifier.
	 */
	@Test
	void testEqualsAndHash() throws Exception {
		testEqualsAndHash(SystemUser.class, "login");
	}

	/**
	 * Test equals and hash code operation with all possible combinations with default identifier.
	 */
	@Test
	void testEqualsAndHashId() throws Exception {
		testEqualsAndHash(DummyBusinessEntity.class);
	}

	@Test
	void testPojo() throws ReflectiveOperationException {
		testPojo(SystemDialect.class);
		testPojo(SystemUserSetting.class);
		testPojo(SystemUser.class);
		testPojo(Generated.class);
	}

}
