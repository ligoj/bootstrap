package org.ligoj.bootstrap.resource.system.user;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.dao.system.SystemUserSettingRepository;
import org.ligoj.bootstrap.model.system.SystemUserSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class of {@link UserSettingResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class UserSettingResourceTest extends AbstractBootTest {

	@Autowired
	private UserSettingResource resource;

	@Autowired
	private SystemUserSettingRepository repository;

	@Test
	public void create() {
		resource.saveOrUpdate("k", "v");
		em.clear();
		final List<SystemUserSetting> all = repository.findAll();
		Assert.assertFalse(all.isEmpty());
		final SystemUserSetting setting = all.get(0);
		Assert.assertEquals("k", setting.getName());
		Assert.assertEquals("v", setting.getValue());
		Assert.assertEquals(DEFAULT_USER, setting.getLogin());
	}

	@Test
	public void findAll() {
		newSetting();
		final Map<String, Object> all = resource.findAll();
		Assert.assertFalse(all.isEmpty());
		Assert.assertEquals("v", all.get("k"));
	}

	@Test
	public void findByName() {
		newSetting();
		Assert.assertEquals("v", resource.findByName("k"));
	}

	@Test
	public void findByNameNull() {
		newSetting();
		Assert.assertNull(resource.findByName("any"));
	}

	@Test
	public void update() {
		newSetting();
		resource.saveOrUpdate("k", "w");
		final Map<String, Object> all = resource.findAll();
		Assert.assertFalse(all.isEmpty());
		Assert.assertEquals("w", all.get("k"));
	}

	@Test
	public void delete() {
		newSetting();
		resource.delete("k");
		final Map<String, Object> all = resource.findAll();
		Assert.assertTrue(all.isEmpty());
	}

	@Test
	public void findAllEmpty() {
		final SystemUserSetting userSetting = new SystemUserSetting();
		userSetting.setLogin("any");
		userSetting.setName("k");
		userSetting.setValue("v");
		repository.saveAndFlush(userSetting);
		em.clear();
		final Map<String, Object> all = resource.findAll();
		Assert.assertTrue(all.isEmpty());
	}

	private void newSetting() {
		final SystemUserSetting userSetting = new SystemUserSetting();
		userSetting.setLogin(DEFAULT_USER);
		userSetting.setName("k");
		userSetting.setValue("v");
		repository.saveAndFlush(userSetting);
		em.clear();
	}
}
