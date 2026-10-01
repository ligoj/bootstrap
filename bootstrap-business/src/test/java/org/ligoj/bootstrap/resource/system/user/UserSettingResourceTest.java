/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import jakarta.ws.rs.PathParam;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.dao.system.SystemUserSettingRepository;
import org.ligoj.bootstrap.model.system.SystemUserSetting;
import org.ligoj.bootstrap.core.security.SecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

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
	void saveOrUpdateOtherUserNotAdmin() {
		Assertions.assertThrows(AccessDeniedException.class, () -> resource.saveOrUpdate("other", "k", "v"));
		Assertions.assertNull(repository.findByLoginAndName("other", "k"));
	}

	@Test
	void saveOrUpdateOtherUserAdmin() {
		initSpringSecurityContext(DEFAULT_USER, new SimpleGrantedAuthority(SecurityHelper.ADMIN));
		resource.saveOrUpdate("other", "k", "v");
		Assertions.assertEquals("v", repository.findByLoginAndName("other", "k").getValue());
	}

	@Test
	void saveOrUpdateOtherUserBinding() throws NoSuchMethodException {
		// Each path parameter is bound to its own argument
		final var parameters = UserSettingResource.class.getMethod("saveOrUpdate", String.class, String.class, String.class)
				.getParameters();
		Assertions.assertEquals(List.of("user", "name", "value"),
				Arrays.stream(parameters).map(p -> p.getAnnotation(PathParam.class).value()).toList());
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
