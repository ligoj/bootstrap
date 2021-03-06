/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link AbstractBusinessEntity} test class.
 */
class TestAbstractBusinessEntityTest {

	@Test
    void testIsNewNew() {
		Assertions.assertTrue(new AbstractBusinessEntity<Integer>() {

			// Nothing
		}.isNew());
	}

	@Test
    void testNamed() {
		AbstractNamedBusinessEntity<Integer> entity = new AbstractNamedBusinessEntity<>() {

			/**
			 * SID
			 */
			private static final long serialVersionUID = 1L;
			// Nothing
		};
		entity.setName("Name");
		Assertions.assertEquals("AbstractNamedBusinessEntity(name=Name)", entity.toString());
	}

	@Test
    void testIsNewSet() {
		AbstractBusinessEntity<Integer> entity = new AbstractBusinessEntity<>() {
			// Nothing
		};
		entity.setId(1);
		Assertions.assertFalse(entity.isNew());
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
    void testEquals() {
		final var entity1 = new DummyBusinessEntity();
		Assertions.assertTrue(entity1.equals(entity1));
		final var entity2 = new DummyBusinessEntity();
		Assertions.assertTrue(entity1.equals(entity2));
		Assertions.assertFalse(entity1.equals(new DummyBusinessEntity2()));
		entity1.setId("a");
		Assertions.assertFalse(new DummyBusinessEntity().equals(entity1));
		Assertions.assertFalse(entity1.equals(new DummyBusinessEntity()));
		entity2.setId("a");
		Assertions.assertTrue(entity1.equals(entity2));
		Assertions.assertFalse(entity1.equals(null));
		entity2.setId("b");
		Assertions.assertFalse(entity1.equals(entity2));
	}

	@Test
    void testHashCode() {
		final var entity1 = new DummyBusinessEntity();
		entity1.hashCode();
		entity1.setId("a");
		entity1.hashCode();
	}

	/**
	 * Test equals and hash code operation with all possible combinations with default identifier.
	 */
	@Test
    void testToString() {
		final var entity = new DummyBusinessEntity();
		entity.setId("any");
		Assertions.assertEquals("AbstractBusinessEntity(id=any)", entity.toString());
	}
}
