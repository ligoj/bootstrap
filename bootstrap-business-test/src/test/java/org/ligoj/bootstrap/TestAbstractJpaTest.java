/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import jakarta.persistence.EntityManager;
import org.hibernate.collection.spi.PersistentBag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.dao.csv.CsvForJpa;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Test of {@link AbstractJpaTest}
 */
class TestAbstractJpaTest extends AbstractJpaTest {

	@Test
	void persistEntities() throws IOException {
		em = Mockito.mock(EntityManager.class);
		csvForJpa = Mockito.mock(CsvForJpa.class);
		final var entities = new ArrayList<SystemUser>();
		entities.add(new SystemUser());
		Mockito.when(csvForJpa.toJpa(ArgumentMatchers.eq(SystemUser.class), ArgumentMatchers.any(Reader.class), ArgumentMatchers.eq(true), ArgumentMatchers.eq(true))).thenReturn(entities);
		Assertions.assertEquals(entities, super.persistEntities(SystemUser.class, "log4j2.json"));
	}

	@Test
	void persistEntities2() throws IOException {
		csvForJpa = Mockito.mock(CsvForJpa.class);
		super.persistEntities("log4j2.json");
	}

	@Test
	@Deprecated
	void persistEntitiesDeprecated() throws IOException {
		csvForJpa = Mockito.mock(CsvForJpa.class);
		super.persistEntities(".", new Class[]{SystemUser.class}, StandardCharsets.UTF_8.name());
	}

	@Test
	void isLazyInitializedFalse() {
		Assertions.assertFalse(super.isLazyInitialized(new PersistentBag<>()));
	}

	@Test
	void isLazyInitializedFalseArrayList() {
		Assertions.assertFalse(super.isLazyInitialized(new ArrayList<>()));
	}

	@Test
	void isLazyInitializedTrue() {
		final var bag = new PersistentBag<>() {
			private static final long serialVersionUID = 1L;

			{
				setInitialized();
			}
		};
		Assertions.assertTrue(super.isLazyInitialized(bag));
	}
}
