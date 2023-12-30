/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.dao.system.SystemUserSettingRepository;
import org.ligoj.bootstrap.model.system.SystemUserSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link UserSettingResource}
 */
@ExtendWith(SpringExtension.class)
class UserSettingResourceTest extends AbstractBootTest {

	@Autowired
	private UserSettingResource resource;

	@Autowired
	private SystemUserSettingRepository repository;

	@Test
	void create() {
		resource.saveOrUpdate("k", "v");
		em.clear();
		final var all = repository.findAll();
		Assertions.assertFalse(all.isEmpty());
		final var setting = all.getFirst();
		Assertions.assertEquals("k", setting.getName());
		Assertions.assertEquals("v", setting.getValue());
		Assertions.assertEquals(DEFAULT_USER, setting.getLogin());
	}

	@Test
	void findAll() {
		newSetting();
		final var all = resource.findAll();
		Assertions.assertFalse(all.isEmpty());
		Assertions.assertEquals("v", all.get("k"));
	}

	@Test
	void findByName() {
		newSetting();
		Assertions.assertEquals("v", resource.findByName("k"));
	}

	@Test
	void findByNameNull() {
		newSetting();
		Assertions.assertNull(resource.findByName("any"));
	}

	@Test
	void update() {
		newSetting();
		resource.saveOrUpdate("k", "w");
		final var all = resource.findAll();
		Assertions.assertFalse(all.isEmpty());
		Assertions.assertEquals("w", all.get("k"));
	}

	@Test
	void delete() {
		newSetting();
		resource.delete("k");
		final var all = resource.findAll();
		Assertions.assertTrue(all.isEmpty());
	}

	@Test
	void findAllEmpty() {
		final var userSetting = new SystemUserSetting();
		userSetting.setLogin("any");
		userSetting.setName("k");
		userSetting.setValue("v");
		repository.saveAndFlush(userSetting);
		em.clear();
		final var all = resource.findAll();
		Assertions.assertTrue(all.isEmpty());
	}

	private void newSetting() {
		final var userSetting = new SystemUserSetting();
		userSetting.setLogin(DEFAULT_USER);
		userSetting.setName("k");
		userSetting.setValue("v");
		repository.saveAndFlush(userSetting);
		em.clear();
	}
}
