/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

/**
 * Test persistable entity.
 */
class PersistableTest {

	@Test
	void testAudit() {
		final var now =  Instant.now();
		final var entity = new PersistableEntity();
		entity.setCreatedBy("name1");
		entity.setLastModifiedBy("name2");
		entity.setCreatedDate(now);
		entity.setLastModifiedDate(Instant.ofEpochMilli(now.toEpochMilli()+ 1000));
		Assertions.assertTrue(entity.isNew());

		entity.setId(0);
		Assertions.assertFalse(entity.isNew());
		Assertions.assertEquals("name1", entity.getCreatedBy());
		Assertions.assertEquals("name2", entity.getLastModifiedBy());
		Assertions.assertEquals(now.toEpochMilli(), entity.getCreatedDate().toEpochMilli());
		Assertions.assertEquals(now.toEpochMilli() + 1000, entity.getLastModifiedDate().toEpochMilli());
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
