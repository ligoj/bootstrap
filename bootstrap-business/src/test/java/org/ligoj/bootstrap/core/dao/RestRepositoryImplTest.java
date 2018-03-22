/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.JoinType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.dao.system.DialectRepository;
import org.ligoj.bootstrap.dao.system.SystemRoleAssignmentRepository;
import org.ligoj.bootstrap.dao.system.SystemRoleRepository;
import org.ligoj.bootstrap.model.system.SystemDialect;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.svenjacobs.loremipsum.LoremIpsum;

/**
 * {@link RestRepositoryImpl} class test.
 */
@ExtendWith(SpringExtension.class)
public class RestRepositoryImplTest extends AbstractBootTest {

	@Autowired
	private DialectRepository repository;

	@Autowired
	private SystemRoleRepository roleRepository;

	@Autowired
	private SystemRoleAssignmentRepository roleAssignmentRepository;

	/**
	 * Last know identifier.
	 */
	private int lastKnownEntity;

	@BeforeEach
	public void setup() {
		final LoremIpsum loremIpsum = new LoremIpsum();
		SystemDialect dial1 = new SystemDialect();
		for (int i = 0; i < 10; i++) {
			SystemDialect dial2 = new SystemDialect();
			dial2.setDialLong((long) i);
			dial2.setDialChar(loremIpsum.getWords(1, i % 50));
			dial2.setDialDate(new Date(System.currentTimeMillis() + i));
			em.persist(dial2);

			dial1 = new SystemDialect();
			dial1.setDialLong((long) i);
			dial1.setDialChar(loremIpsum.getWords(1, i % 50));
			dial1.setDialDate(new Date(System.currentTimeMillis() + i));
			dial1.setLink(dial2);
			em.persist(dial1);
		}
		em.flush();
		lastKnownEntity = dial1.getId();
		em.clear();
	}

	/**
	 * Default find, and found.
	 */
	@Test
	public void findOne() {
		final SystemDialect dialect = repository.findOne(lastKnownEntity);
		Assertions.assertNotNull(dialect);
		Assertions.assertNotNull(dialect.getLink());
		Assertions.assertFalse(isLazyInitialized(dialect.getLink().getChildren()));
	}

	/**
	 * Default find, and not found.
	 */
	@Test
	public void findOneNotFound() {
		Assertions.assertNull(repository.findOne(-1));
	}

	/**
	 * Default find one with expected result.
	 */
	@Test
	public void findOneExpected() {
		final SystemDialect dialect = repository.findOneExpected(lastKnownEntity);
		Assertions.assertNotNull(dialect);
		Assertions.assertNotNull(dialect.getLink());
		Assertions.assertFalse(isLazyInitialized(dialect.getLink().getChildren()));
	}

	/**
	 * Find one without expected result.
	 */
	@Test
	public void findOneExpectedError() {
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> repository.findOneExpected(-1));
	}

	/**
	 * Default find one with expected result and fetch association.
	 */
	@Test
	public void findOneExpectedFetch() {
		final Map<String, JoinType> fetch = new HashMap<>();
		fetch.put("link", JoinType.LEFT);
		fetch.put("link.children", JoinType.LEFT);
		final SystemDialect dialect = repository.findOneExpected(lastKnownEntity, fetch);
		Assertions.assertNotNull(dialect);
		Assertions.assertNotNull(dialect.getLink());
		Assertions.assertTrue(isLazyInitialized(dialect.getLink().getChildren()));
	}

	/**
	 * Default find one with expected result and empty fetch association.
	 */
	@Test
	public void findOneExpectedEmptyFetch() {
		final SystemDialect dialect = repository.findOneExpected(lastKnownEntity, null);
		Assertions.assertNotNull(dialect);
		Assertions.assertNotNull(dialect.getLink());
		Assertions.assertFalse(isLazyInitialized(dialect.getLink().getChildren()));
	}

	/**
	 * Default find by name success.
	 */
	@Test
	public void findByName() {
		SystemRole role = new SystemRole();
		role.setName("name");
		em.persist(role);
		em.flush();
		em.clear();

		role = roleRepository.findByName("name");
		Assertions.assertNotNull(role);
		Assertions.assertEquals("name", role.getName());
	}

	@Test
	public void findByMoreProperties() {
		SystemRole role = new SystemRole();
		role.setName("role");
		em.persist(role);
		em.flush();
		em.clear();

		role = roleRepository.findBy("name", "role", new String[] { "id" }, role.getId());
		Assertions.assertNotNull(role);
		Assertions.assertEquals("role", role.getName());
	}

	@Test
	public void findByDeepPathMoreProperties() {
		SystemRole role = new SystemRole();
		role.setName("role");
		em.persist(role);
		SystemUser user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);

		SystemRoleAssignment assignment = new SystemRoleAssignment();
		assignment.setRole(role);
		assignment.setUser(user);
		em.persist(assignment);

		em.flush();
		em.clear();

		assignment = roleAssignmentRepository.findBy("role.name", "role", new String[] { "user.login" }, DEFAULT_USER);
		Assertions.assertNotNull(assignment);
		Assertions.assertEquals("role", assignment.getRole().getName());
	}

	@Test
	public void countBy() {
		SystemRole role = new SystemRole();
		role.setName("john");
		em.persist(role);
		em.flush();
		em.clear();

		Assertions.assertEquals(1, roleRepository.countBy("name", "john"));
		Assertions.assertEquals(0, roleRepository.countBy("name", "any"));
	}

	/**
	 * Default find all by name success.
	 */
	@Test
	public void findAllByName() {
		SystemRole role = new SystemRole();
		role.setName("value2");
		em.persist(role);
		SystemRole role2 = new SystemRole();
		role2.setName("value1");
		em.persist(role2);
		em.flush();
		em.clear();

		List<SystemRole> roles = roleRepository.findAllBy("name", "value1");
		Assertions.assertNotNull(roles);
		Assertions.assertEquals(1, roles.size());
		Assertions.assertEquals("value1", roles.get(0).getName());
	}

	/**
	 * Default find by name not found.
	 */
	@Test
	public void findByNameNull() {
		Assertions.assertNull(roleRepository.findByName("any"));
	}

	/**
	 * Default find by name with expected result.
	 */
	@Test
	public void findByNameExpected() {
		SystemRole role = new SystemRole();
		role.setName("name");
		em.persist(role);
		em.flush();
		em.clear();

		role = roleRepository.findByNameExpected("name");
		Assertions.assertNotNull(role);
		Assertions.assertEquals("name", role.getName());
	}

	/**
	 * Find by name without expected result.
	 */
	@Test
	public void findByNameExpectedError() {
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> {
			roleRepository.findByNameExpected("any");
		});
	}

	@Test
	public void deleteAllNoFetch() {
		SystemDialect systemDialect = repository.findAll().get(1);
		Assertions.assertFalse(repository.findAll().isEmpty());
		Assertions.assertTrue(repository.deleteAllNoFetch() > 2);
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		Assertions.assertTrue(repository.findAll().isEmpty());
	}

	@Test
	public void deleteAllIdentifiers() {
		SystemDialect systemDialect = repository.findAll().get(1);
		Assertions.assertFalse(repository.findAll().isEmpty());
		final List<Integer> list = new ArrayList<>();
		list.add(systemDialect.getId());
		list.add(-1);
		Assertions.assertEquals(1, repository.deleteAll(list));
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
	}

	@Test
	public void deleteAllBy() {
		SystemDialect systemDialect = repository.findAll().get(1);
		Assertions.assertFalse(repository.findAll().isEmpty());
		Assertions.assertEquals(1, repository.deleteAllBy("id", systemDialect.getId()));
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
	}

	@Test
	public void deleteAllByMoreProperties() {
		SystemDialect systemDialect = repository.findAll().get(1);
		Assertions.assertFalse(repository.findAll().isEmpty());
		Assertions.assertEquals(0,
				repository.deleteAllBy("id", systemDialect.getId(), new String[] { "dialChar" }, "some"));
		Assertions.assertEquals(1, repository.deleteAllBy("id", systemDialect.getId(), new String[] { "dialChar" },
				systemDialect.getDialChar()));
		Assertions.assertEquals(0, repository.deleteAllBy("id", systemDialect.getId(), new String[] { "dialChar" },
				systemDialect.getDialChar()));
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));

		SystemRole role = new SystemRole();
		role.setName("role");
		em.persist(role);
		em.flush();
		em.clear();

		role = roleRepository.findBy("name", "role", new String[] { "id" }, role.getId());
		Assertions.assertNotNull(role);
		Assertions.assertEquals("role", role.getName());
	}

	@Test
	public void deleteAllByDeepPathMoreProperties() {
		SystemRole role = new SystemRole();
		role.setName("role");
		em.persist(role);
		SystemUser user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);

		SystemRoleAssignment assignment = new SystemRoleAssignment();
		assignment.setRole(role);
		assignment.setUser(user);
		em.persist(assignment);

		em.flush();
		em.clear();

		final int nb = roleAssignmentRepository.deleteAllBy("role.id", role.getId(), new String[] { "user.login" },
				DEFAULT_USER);
		Assertions.assertNotNull(assignment);
		Assertions.assertEquals(1, nb);
	}

	@Test
	public void deleteAllExpected() {
		final SystemDialect systemDialect = repository.findAll().get(1);
		Assertions.assertFalse(repository.findAll().isEmpty());
		final List<Integer> list = new ArrayList<>();
		list.add(systemDialect.getId());
		repository.deleteAllExpected(list);
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
	}

	@Test
	public void deleteAllExpectedFailed() {
		final SystemDialect systemDialect = repository.findAll().get(1);
		Assertions.assertFalse(repository.findAll().isEmpty());
		final List<Integer> list = new ArrayList<>();
		list.add(systemDialect.getId());
		list.add(-1);
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> repository.deleteAllExpected(list));
	}

	@Test
	public void deleteAllIdentifiersEmpty() {
		int size = repository.findAll().size();
		Assertions.assertEquals(0, repository.deleteAll(new ArrayList<Integer>()));
		Assertions.assertEquals(size, repository.findAll().size());
		em.flush();
		em.clear();
		Assertions.assertEquals(size, repository.findAll().size());
	}

	@Test
	public void deleteAllIdentifiersNull() {
		int size = repository.findAll().size();
		Assertions.assertEquals(0, repository.deleteAll((Collection<Integer>) null));
		Assertions.assertEquals(size, repository.findAll().size());
		em.flush();
		em.clear();
		Assertions.assertEquals(size, repository.findAll().size());
	}

	@Test
	public void deleteExpected() {
		SystemDialect systemDialect = repository.findAll().get(1);
		repository.deleteById(systemDialect.getId());
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
	}

	@Test
	public void deleteExpectedError() {
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> repository.deleteById(-1));
	}

	@Test
	public void deleteNoFetch() {
		SystemDialect systemDialect = repository.findAll().get(1);
		repository.deleteNoFetch(systemDialect.getId());
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
	}

	@Test
	public void deleteNoFetchError() {
		SystemDialect systemDialect = repository.findAll().get(1);
		repository.deleteNoFetch(systemDialect.getId());
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class,
				() -> repository.deleteNoFetch(systemDialect.getId()));
	}

	@Test
	public void existExpected() {
		SystemDialect systemDialect = repository.findAll().get(1);
		repository.existExpected(systemDialect.getId());
	}

	@Test
	public void existExpectedFail() {
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> repository.existExpected(-1));
	}

}
