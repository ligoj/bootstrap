/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test persistable entity.
 */
public class PersistableTest {

	@Test
	public void testAudit() {
		final Date now = new Date();
		final PersistableEntity entity = new PersistableEntity();
		entity.setCreatedBy("name1");
		entity.setLastModifiedBy("name2");
		entity.setCreatedDate(now);
		entity.setLastModifiedDate(new Date(now.getTime() + 1000));

		// For coverage and overriding
		entity.setId(0);

		Assertions.assertEquals("name1", entity.getCreatedBy());
		Assertions.assertEquals("name2", entity.getLastModifiedBy());
		Assertions.assertEquals(now.getTime(), entity.getCreatedDate().getTime());
		Assertions.assertEquals(now.getTime() + 1000, entity.getLastModifiedDate().getTime());
	}

	@Test
	public void testAuditNull() {
		final PersistableEntity entity = new PersistableEntity();
		entity.setCreatedBy("name1");
		entity.setLastModifiedBy("name2");
		Assertions.assertEquals("name1", entity.getCreatedBy());
		Assertions.assertEquals("name2", entity.getLastModifiedBy());
		Assertions.assertEquals(null, entity.getCreatedDate());
		Assertions.assertEquals(null, entity.getLastModifiedDate());
	}
}
