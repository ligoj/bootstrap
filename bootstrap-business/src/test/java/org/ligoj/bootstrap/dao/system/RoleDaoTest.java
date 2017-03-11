package org.ligoj.bootstrap.dao.system;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.transaction.Transactional;

import org.ligoj.bootstrap.AbstractJpaTest;
import org.ligoj.bootstrap.model.system.SystemRole;

/**
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/META-INF/spring/jpa-context-test.xml")
@Rollback
@Transactional
public class RoleDaoTest extends AbstractJpaTest {

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