/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.ligoj.bootstrap.dao.system.SystemUserSettingRepository;
import org.ligoj.bootstrap.model.system.AbstractNamedValue;
import org.ligoj.bootstrap.model.system.SystemUserSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Manage {@link SystemUserSetting}. User settings are scoped by current user.
 */
@Service
@Path("/system/setting")
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class UserSettingResource {

	@Autowired
	private SystemUserSettingRepository repository;

	/**
	 * Delete a {@link SystemUserSetting}
	 * 
	 * @param name the user setting name to delete.
	 */
	@DELETE
	@Path("{name}")
	public void delete(@PathParam("name") final String name) {
		repository.delete(SecurityContextHolder.getContext().getAuthentication().getName(), name);
	}

	/**
	 * Return all user's settings.
	 * 
	 * @return all user's settings.
	 */
	@GET
	public Map<String, Object> findAll() {
		return findAll(SecurityContextHolder.getContext().getAuthentication().getName());
	}

	/**
	 * Return all settings of given user.
	 * 
	 * @param login USer login to check.
	 * @return all user's settings.
	 */
	public Map<String, Object> findAll(final String login) {
		return repository.findByLogin(login).stream()
				.collect(Collectors.toMap(AbstractNamedValue::getName, AbstractNamedValue::getValue));
	}

	/**
	 * Return a specific setting of current user.
	 * 
	 * @param name The setting's name.
	 * @return a specific user's setting. May be <code>null</code>
	 */
	@GET
	@Path("{name}")
	public Object findByName(final String name) {
		return findByName(SecurityContextHolder.getContext().getAuthentication().getName(), name);
	}

	/**
	 * Return a specific user's setting.
	 * 
	 * @param login the user login owning the setting.
	 * @param name  The setting's name.
	 * @return a specific user's setting. May be <code>null</code>.
	 */
	public String findByName(final String login, final String name) {
		return Optional.ofNullable(repository.findByLoginAndName(login, name)).map(SystemUserSetting::getValue)
				.orElse(null);
	}

	/**
	 * Save or update a setting of a given user.
	 * 
	 * @param user  the related user name.
	 * @param name  the setting name.
	 * @param value the new value.
	 */
	@POST
	@PUT
	@Path("/system/admin-setting/{user}/{name}/{value}")
	public void saveOrUpdate(@PathParam("name") final String user, @PathParam("name") final String name,
			@PathParam("value") final String value) {
		final var setting = repository.findByLoginAndName(user, name);
		if (setting == null) {
			final var entity = new SystemUserSetting();
			entity.setLogin(user);
			entity.setName(name);
			entity.setValue(value);
			repository.saveAndFlush(entity);
		} else {
			setting.setValue(value);
		}
	}

	/**
	 * Save or update a setting of current given user.
	 * 
	 * @param name  the setting name.
	 * @param value the new value.
	 */
	@POST
	@PUT
	@Path("{name}/{value}")
	public void saveOrUpdate(@PathParam("name") final String name, @PathParam("value") final String value) {
		final var user = SecurityContextHolder.getContext().getAuthentication().getName();
		saveOrUpdate(user, name, value);
	}

}
