package org.ligoj.bootstrap.core.model;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

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
		entity.setCreatedDate(new DateTime(now.getTime()));
		entity.setLastModifiedDate(new DateTime(now.getTime() + 1000));

		// For coverage and overriding
		entity.setId(0);

		Assert.assertEquals("name1", entity.getCreatedBy());
		Assert.assertEquals("name2", entity.getLastModifiedBy());
		Assert.assertEquals(now.getTime(), entity.getCreatedDate().getMillis());
		Assert.assertEquals(now.getTime() + 1000, entity.getLastModifiedDate().getMillis());
	}

	@Test
	public void testAuditNull() {
		final PersistableEntity entity = new PersistableEntity();
		entity.setCreatedBy("name1");
		entity.setLastModifiedBy("name2");
		Assert.assertEquals("name1", entity.getCreatedBy());
		Assert.assertEquals("name2", entity.getLastModifiedBy());
		Assert.assertEquals(null, entity.getCreatedDate());
		Assert.assertEquals(null, entity.getLastModifiedDate());
	}
}
