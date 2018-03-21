/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link AbstractBusinessEntity} test class.
 */
public class TestAbstractBusinessEntityTest {

	@Test
	public void testIsNewNew() {
		Assertions.assertTrue(new AbstractBusinessEntity<Integer>() {

			// Nothing
		}.isNew());
	}

	@Test
	public void testNamed() {
		AbstractNamedBusinessEntity<Integer> entity = new AbstractNamedBusinessEntity<>() {
			// Nothing
		};
		entity.setName("Name");
		Assertions.assertEquals("AbstractNamedBusinessEntity(name=Name)", entity.toString());
	}

	@Test
	public void testIsNewSet() {
		AbstractBusinessEntity<Integer> entity = new AbstractBusinessEntity<>() {
			// Nothing
		};
		entity.setId(1);
		Assertions.assertFalse(entity.isNew());
	}

	@Test
	public void testEquals() {
		final DummyBusinessEntity entity1 = new DummyBusinessEntity();
		Assertions.assertTrue(entity1.equals(entity1));
		final DummyBusinessEntity entity2 = new DummyBusinessEntity();
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
	public void testHashCode() {
		final DummyBusinessEntity entity1 = new DummyBusinessEntity();
		entity1.hashCode();
		entity1.setId("a");
		entity1.hashCode();
	}

	/**
	 * Test equals and hash code operation with all possible combinations with default identifier.
	 */
	@Test
	public void testToString() {
		final DummyBusinessEntity entity = new DummyBusinessEntity();
		entity.setId("any");
		Assertions.assertEquals("AbstractBusinessEntity(id=any)", entity.toString());
	}
}
