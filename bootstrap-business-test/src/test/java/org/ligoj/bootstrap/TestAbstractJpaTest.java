package org.ligoj.bootstrap;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.collection.internal.PersistentBag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.dao.csv.CsvForJpa;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Test of {@link AbstractJpaTest}
 */
public class TestAbstractJpaTest extends AbstractJpaTest {

	@Test
	public void persistEntities() throws IOException {
		em = Mockito.mock(EntityManager.class);
		csvForJpa = Mockito.mock(CsvForJpa.class);
		final List<SystemUser> entities = new ArrayList<>();
		entities.add(new SystemUser());
		Mockito.when(csvForJpa.toJpa(ArgumentMatchers.eq(SystemUser.class), ArgumentMatchers.any(Reader.class), ArgumentMatchers.eq(true))).thenReturn(entities);
		super.persistEntities(SystemUser.class, "log4j2.json");
	}

	@Test
	public void persistEntities2() throws IOException {
		csvForJpa = Mockito.mock(CsvForJpa.class);
		super.persistEntities("log4j2.json");
	}

	@Test
	public void isLazyInitializedFalse() {
		Assertions.assertFalse(super.isLazyInitialized(new PersistentBag()));
	}

	@Test
	public void isLazyInitializedFalseArrayList() {
		Assertions.assertFalse(super.isLazyInitialized(new ArrayList<>()));
	}

	@Test
	public void isLazyInitializedTrue() {
		final PersistentBag bag = new PersistentBag() {
			private static final long serialVersionUID = 1L;

			{
				setInitialized();
			}
		};
		Assertions.assertTrue(super.isLazyInitialized(bag));
	}
}
