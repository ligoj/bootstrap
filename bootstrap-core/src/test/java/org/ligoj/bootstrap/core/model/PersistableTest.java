/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * Test persistable entity.
 */
class PersistableTest {

	@Test
	void testAudit() {
		final var now = new Date();
		final var entity = new PersistableEntity();
		entity.setCreatedBy("name1");
		entity.setLastModifiedBy("name2");
		entity.setCreatedDate(now);
		entity.setLastModifiedDate(new Date(now.getTime() + 1000));
		Assertions.assertTrue(entity.isNew());

		entity.setId(0);
		Assertions.assertFalse(entity.isNew());
		Assertions.assertEquals("name1", entity.getCreatedBy());
		Assertions.assertEquals("name2", entity.getLastModifiedBy());
		Assertions.assertEquals(now.getTime(), entity.getCreatedDate().getTime());
		Assertions.assertEquals(now.getTime() + 1000, entity.getLastModifiedDate().getTime());
	}

	@Test
	void testStringKeyEntity() {
		final var entity = new AbstractStringKeyEntity() {
		};
		Assertions.assertTrue(entity.isNew());
		entity.setId("assigned");
		Assertions.assertFalse(entity.isNew());
		Assertions.assertEquals("AbstractStringKeyEntity(id=assigned)", entity.toString());
	}

	@Test
	void testAuditNull() {
		final var entity = new PersistableEntity();
		entity.setCreatedBy("name1");
		entity.setLastModifiedBy("name2");
		Assertions.assertEquals("name1", entity.getCreatedBy());
		Assertions.assertEquals("name2", entity.getLastModifiedBy());
		Assertions.assertNull(entity.getCreatedDate());
		Assertions.assertNull(entity.getLastModifiedDate());
	}
}
