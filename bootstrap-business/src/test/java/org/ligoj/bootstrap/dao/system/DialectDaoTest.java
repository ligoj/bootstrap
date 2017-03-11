package org.ligoj.bootstrap.dao.system;

import java.util.GregorianCalendar;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.transaction.Transactional;

import org.ligoj.bootstrap.model.system.SystemDialect;

/**
 * JDBC bench test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/jpa-context-test.xml")
@Rollback
@Transactional
public class DialectDaoTest {

	/**
	 * Entity manager.
	 */
	@PersistenceContext(type = PersistenceContextType.TRANSACTION)
	private EntityManager em;

	@Test
	public void testJavaTypes() {
		// create an entity with multiple java types
		final SystemDialect dial = new SystemDialect();
		dial.setDialBool(true);
		dial.setDialChar("char");
		dial.setDialDate(new GregorianCalendar(2012, 02, 10).getTime());
		dial.setDialDouble(15.0);
		dial.setDialEnum(CascadeType.PERSIST);
		dial.setDialLong(15L);
		dial.setDialShort((short) 1);
		// persist and flush
		em.persist(dial);
		em.flush();
		em.clear();
		// read entity from database and test values
		final SystemDialect dialFromDb = em.find(SystemDialect.class, dial.getId());
		Assert.assertEquals(dial.getDialChar(), dialFromDb.getDialChar());
		Assert.assertEquals(dial.getDialBool(), dialFromDb.getDialBool());
		Assert.assertEquals(dial.getDialDate(), dialFromDb.getDialDate());
		Assert.assertEquals(dial.getDialDouble(), dialFromDb.getDialDouble(),0.00001);
		Assert.assertEquals(dial.getDialEnum(), dialFromDb.getDialEnum());
		Assert.assertEquals(dial.getDialLong(), dialFromDb.getDialLong());
		Assert.assertEquals(dial.getDialShort(), dialFromDb.getDialShort());
	}

}
