package org.ligoj.bootstrap.model;

import org.junit.Test;

import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Test business keyed entities basic ORM operations : hash code and equals.
 */
public class TestAbstractBusinessEntityTest extends AbstractBusinessEntityTest {

	/**
	 * Test equals and hash code operation with all possible combinations with only one identifier.
	 */
	@Test
	public void testEqualsAndHash() throws Exception {
		testEqualsAndHash(SystemUser.class, "login");
	}

	/**
	 * Test equals and hash code operation with all possible combinations with default identifier.
	 */
	@Test
	public void testEqualsAndHashId() throws Exception {
		testEqualsAndHash(DummyBusinessEntity.class);
	}

}
