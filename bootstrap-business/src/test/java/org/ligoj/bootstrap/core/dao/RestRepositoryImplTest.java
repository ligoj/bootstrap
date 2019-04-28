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
class RestRepositoryImplTest extends AbstractBootTest {

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
	void setup() {
		final var loremIpsum = new LoremIpsum();
        var dial1 = new SystemDialect();
		for (var i = 0; i < 10; i++) {
            var dial2 = new SystemDialect();
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
	void findOne() {
		final var dialect = repository.findOne(lastKnownEntity);
		Assertions.assertNotNull(dialect);
		Assertions.assertNotNull(dialect.getLink());
		Assertions.assertFalse(isLazyInitialized(dialect.getLink().getChildren()));
	}

	/**
	 * Default find, and not found.
	 */
	@Test
	void findOneNotFound() {
		Assertions.assertNull(repository.findOne(-1));
	}

	/**
	 * Default find one with expected result.
	 */
	@Test
	void findOneExpected() {
		final var dialect = repository.findOneExpected(lastKnownEntity);
		Assertions.assertNotNull(dialect);
		Assertions.assertNotNull(dialect.getLink());
		Assertions.assertFalse(isLazyInitialized(dialect.getLink().getChildren()));
	}

	/**
	 * Find one without expected result.
	 */
	@Test
	void findOneExpectedError() {
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> repository.findOneExpected(-1));
	}

	/**
	 * Default find one with expected result and fetch association.
	 */
	@Test
	void findOneExpectedFetch() {
		final Map<String, JoinType> fetch = new HashMap<>();
		fetch.put("link", JoinType.LEFT);
		fetch.put("link.children", JoinType.LEFT);
		final var dialect = repository.findOneExpected(lastKnownEntity, fetch);
		Assertions.assertNotNull(dialect);
		Assertions.assertNotNull(dialect.getLink());
		Assertions.assertTrue(isLazyInitialized(dialect.getLink().getChildren()));
	}

	/**
	 * Default find one with expected result and empty fetch association.
	 */
	@Test
	void findOneExpectedEmptyFetch() {
		final var dialect = repository.findOneExpected(lastKnownEntity, null);
		Assertions.assertNotNull(dialect);
		Assertions.assertNotNull(dialect.getLink());
		Assertions.assertFalse(isLazyInitialized(dialect.getLink().getChildren()));
	}

	/**
	 * Default find by name success.
	 */
	@Test
	void findByName() {
        var role = new SystemRole();
		role.setName("name");
		em.persist(role);
		em.flush();
		em.clear();

		role = roleRepository.findByName("name");
		Assertions.assertNotNull(role);
		Assertions.assertEquals("name", role.getName());
	}

	@Test
	void findByMoreProperties() {
        var role = new SystemRole();
		role.setName("role");
		em.persist(role);
		em.flush();
		em.clear();

		role = roleRepository.findBy("name", "role", new String[] { "id" }, role.getId());
		Assertions.assertNotNull(role);
		Assertions.assertEquals("role", role.getName());
	}

	@Test
	void findByNull() {
		final var dial1 = new SystemDialect();
		dial1.setDialLong(1L);
		em.persist(dial1);
		final var dial2 = new SystemDialect();
		dial2.setDialLong(2L);
		dial2.setDialDate(new Date(System.currentTimeMillis()));
		em.persist(dial2);
		Assertions.assertEquals(1L, repository.findBy("dialDate", null).getDialLong().longValue());
		Assertions.assertEquals(1, repository.findAllBy("dialDate", null).size());
		Assertions.assertEquals(1, repository.deleteAllBy("dialDate", null));
		Assertions.assertEquals(0, repository.findAllBy("dialDate", null).size());
	}

	@Test
	void findByDeepPathMoreProperties() {
        var role = new SystemRole();
		role.setName("role");
		em.persist(role);
        var user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);

        var assignment = new SystemRoleAssignment();
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
	void countBy() {
        var role = new SystemRole();
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
	void findAllByName() {
        var role = new SystemRole();
		role.setName("value2");
		em.persist(role);
        var role2 = new SystemRole();
		role2.setName("value1");
		em.persist(role2);
		em.flush();
		em.clear();

        var roles = roleRepository.findAllBy("name", "value1");
		Assertions.assertNotNull(roles);
		Assertions.assertEquals(1, roles.size());
		Assertions.assertEquals("value1", roles.get(0).getName());
	}

	/**
	 * Default find by name not found.
	 */
	@Test
	void findByNameNull() {
		Assertions.assertNull(roleRepository.findByName("any"));
	}

	/**
	 * Default find by name with expected result.
	 */
	@Test
	void findByNameExpected() {
        var role = new SystemRole();
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
	void findByNameExpectedError() {
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> roleRepository.findByNameExpected("any"));
	}

	@Test
	void deleteAllNoFetch() {
        var systemDialect = repository.findAll().get(1);
		Assertions.assertFalse(repository.findAll().isEmpty());
		Assertions.assertTrue(repository.deleteAllNoFetch() > 2);
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		Assertions.assertTrue(repository.findAll().isEmpty());
	}

	@Test
	void deleteAllIdentifiers() {
        var systemDialect = repository.findAll().get(1);
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
	void deleteAllBy() {
        var systemDialect = repository.findAll().get(1);
		Assertions.assertFalse(repository.findAll().isEmpty());
		Assertions.assertEquals(1, repository.deleteAllBy("id", systemDialect.getId()));
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
	}

	@Test
	void deleteAllByMoreProperties() {
        var systemDialect = repository.findAll().get(1);
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

        var role = new SystemRole();
		role.setName("role");
		em.persist(role);
		em.flush();
		em.clear();

		role = roleRepository.findBy("name", "role", new String[] { "id" }, role.getId());
		Assertions.assertNotNull(role);
		Assertions.assertEquals("role", role.getName());
	}

	@Test
	void deleteAllByDeepPathMoreProperties() {
        var role = new SystemRole();
		role.setName("role");
		em.persist(role);
        var user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);

        var assignment = new SystemRoleAssignment();
		assignment.setRole(role);
		assignment.setUser(user);
		em.persist(assignment);

		em.flush();
		em.clear();

		final var nb = roleAssignmentRepository.deleteAllBy("role.id", role.getId(), new String[] { "user.login" },
				DEFAULT_USER);
		Assertions.assertNotNull(assignment);
		Assertions.assertEquals(1, nb);
	}

	@Test
	void deleteAllExpected() {
		final var systemDialect = repository.findAll().get(1);
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
	void deleteAllExpectedFailed() {
		final var systemDialect = repository.findAll().get(1);
		Assertions.assertFalse(repository.findAll().isEmpty());
		final List<Integer> list = new ArrayList<>();
		list.add(systemDialect.getId());
		list.add(-1);
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> repository.deleteAllExpected(list));
	}

	@Test
	void deleteAllIdentifiersEmpty() {
        var size = repository.findAll().size();
		Assertions.assertEquals(0, repository.deleteAll(new ArrayList<Integer>()));
		Assertions.assertEquals(size, repository.findAll().size());
		em.flush();
		em.clear();
		Assertions.assertEquals(size, repository.findAll().size());
	}

	@Test
	void deleteAllIdentifiersNull() {
        var size = repository.findAll().size();
		Assertions.assertEquals(0, repository.deleteAll((Collection<Integer>) null));
		Assertions.assertEquals(size, repository.findAll().size());
		em.flush();
		em.clear();
		Assertions.assertEquals(size, repository.findAll().size());
	}

	@Test
	void deleteExpected() {
        var systemDialect = repository.findAll().get(1);
		repository.deleteById(systemDialect.getId());
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
	}

	@Test
	void deleteExpectedError() {
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> repository.deleteById(-1));
	}

	@Test
	void deleteNoFetch() {
        var systemDialect = repository.findAll().get(1);
		repository.deleteNoFetch(systemDialect.getId());
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
		em.flush();
		em.clear();
		Assertions.assertFalse(repository.existsById(systemDialect.getId()));
	}

	@Test
	void deleteNoFetchError() {
        var systemDialect = repository.findAll().get(1);
		repository.deleteNoFetch(systemDialect.getId());
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class,
				() -> repository.deleteNoFetch(systemDialect.getId()));
	}

	@Test
	void existExpected() {
        var systemDialect = repository.findAll().get(1);
		repository.existExpected(systemDialect.getId());
	}

	@Test
	void existExpectedFail() {
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> repository.existExpected(-1));
	}

}
