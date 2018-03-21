/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import java.util.GregorianCalendar;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemDialect;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * JDBC bench test.
 */
@ExtendWith(SpringExtension.class)
public class DialectDaoTest extends AbstractBootTest {

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
		Assertions.assertEquals(dial.getDialChar(), dialFromDb.getDialChar());
		Assertions.assertEquals(dial.getDialBool(), dialFromDb.getDialBool());
		Assertions.assertEquals(dial.getDialDate(), dialFromDb.getDialDate());
		Assertions.assertEquals(dial.getDialDouble(), dialFromDb.getDialDouble(),0.00001);
		Assertions.assertEquals(dial.getDialEnum(), dialFromDb.getDialEnum());
		Assertions.assertEquals(dial.getDialLong(), dialFromDb.getDialLong());
		Assertions.assertEquals(dial.getDialShort(), dialFromDb.getDialShort());
	}

}
