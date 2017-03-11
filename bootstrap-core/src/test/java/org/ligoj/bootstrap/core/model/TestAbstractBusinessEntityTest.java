package org.ligoj.bootstrap.core.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link AbstractBusinessEntity} test class.
 */
public class TestAbstractBusinessEntityTest {

	@Test
	public void testIsNewNew() {
		Assert.assertTrue(new AbstractBusinessEntity<Integer>() {

			/**
			 * SID
			 */
			private static final long serialVersionUID = 1L;
		}.isNew());
	}

	@Test
	public void testNamed() {
		AbstractNamedBusinessEntity<Integer> entity = new AbstractNamedBusinessEntity<Integer>() {

			/**
			 * SID
			 */
			private static final long serialVersionUID = 1L;
		};
		entity.setName("Name");
		Assert.assertEquals("AbstractNamedBusinessEntity(name=Name)", entity.toString());
	}

	@Test
	public void testIsNewSet() {
		AbstractBusinessEntity<Integer> entity = new AbstractBusinessEntity<Integer>() {

			/**
			 * SID
			 */
			private static final long serialVersionUID = 1L;
		};
		entity.setId(1);
		Assert.assertFalse(entity.isNew());
	}

	@Test
	public void testEquals() {
		final DummyBusinessEntity entity1 = new DummyBusinessEntity();
		Assert.assertTrue(entity1.equals(entity1));
		final DummyBusinessEntity entity2 = new DummyBusinessEntity();
		Assert.assertTrue(entity1.equals(entity2));
		Assert.assertFalse(entity1.equals(new DummyBusinessEntity2()));
		entity1.setId("a");
		Assert.assertFalse(new DummyBusinessEntity().equals(entity1));
		Assert.assertFalse(entity1.equals(new DummyBusinessEntity()));
		entity2.setId("a");
		Assert.assertTrue(entity1.equals(entity2));
		Assert.assertFalse(entity1.equals(null));
		entity2.setId("b");
		Assert.assertFalse(entity1.equals(entity2));
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
		Assert.assertEquals("AbstractBusinessEntity(id=any)", entity.toString());
	}
}
