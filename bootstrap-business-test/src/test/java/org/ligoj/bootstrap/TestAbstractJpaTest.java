package org.ligoj.bootstrap;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.collection.internal.PersistentBag;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.ligoj.bootstrap.core.dao.csv.CsvForJpa;
import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Test of {@link AbstractJpaTest}
 */
public class TestAbstractJpaTest extends AbstractJpaTest {

	@Test
	public <T> void persistEntities() throws IOException {
		em = Mockito.mock(EntityManager.class);
		csvForJpa = Mockito.mock(CsvForJpa.class);
		final List<SystemUser> entities = new ArrayList<>();
		entities.add(new SystemUser());
		Mockito.when(csvForJpa.toJpa(ArgumentMatchers.eq(SystemUser.class), ArgumentMatchers.any(Reader.class), ArgumentMatchers.eq(true))).thenReturn(entities);
		super.persistEntities(SystemUser.class, "log4j2.json");
	}

	@Test
	public <T> void persistEntities2() throws IOException {
		csvForJpa = Mockito.mock(CsvForJpa.class);
		super.persistEntities("log4j2.json");
	}

	@Test
	public <T> void isLazyInitializedFalse() {
		Assert.assertFalse(super.isLazyInitialized(new PersistentBag()));
	}

	@Test
	public <T> void isLazyInitializedFalseArrayList() {
		Assert.assertFalse(super.isLazyInitialized(new ArrayList<>()));
	}

	@Test
	public <T> void isLazyInitializedTrue() {
		final PersistentBag bag = new PersistentBag() {
			private static final long serialVersionUID = 1L;

			{
				setInitialized();
			}
		};
		Assert.assertTrue(super.isLazyInitialized(bag));
	}
}
