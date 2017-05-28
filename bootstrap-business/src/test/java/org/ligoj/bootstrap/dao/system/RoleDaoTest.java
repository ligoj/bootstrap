package org.ligoj.bootstrap.dao.system;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class of {@link SystemRoleRepository}
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class RoleDaoTest extends AbstractBootTest {

	/**
	 * Factory DAO.
	 */
	@Autowired
	private SystemRoleRepository repository;

	@Before
	public void prepareData() throws IOException {
		persistEntities(SystemRole.class, "csv/system-test/role.csv");
	}

	@Test
	public void testFindAllSN1() {
		Assert.assertFalse(repository.findAll().isEmpty());
	}
}