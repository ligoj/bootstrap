package org.ligoj.bootstrap.resource.system.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.ligoj.bootstrap.dao.system.SystemUserSettingRepository;
import org.ligoj.bootstrap.model.system.SystemUserSetting;

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
	 * Return all user's settings.
	 * 
	 * @return all user's settings.
	 */
	@GET
	public Map<String, Object> findAll() {
		return findAll(SecurityContextHolder.getContext().getAuthentication().getName());
	}

	/**
	 * Return a specific setting of current user.
	 * 
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
	 * @param login
	 *            the user login owning the setting.
	 * @return a specific user's setting. May be <code>null</code>
	 */
	public String findByName(final String login, final String name) {
		final SystemUserSetting setting = repository.findByLoginAndName(login, name);
		return setting == null ? null : setting.getValue();
	}

	/**
	 * Return all settings of given user.
	 * 
	 * @param login
	 *            USer login to check.
	 * @return all user's settings.
	 */
	public Map<String, Object> findAll(final String login) {
		final List<SystemUserSetting> settings = repository.findByLogin(login);
		final Map<String, Object> result = new HashMap<>();
		for (final SystemUserSetting setting : settings) {
			result.put(setting.getName(), setting.getValue());
		}
		return result;
	}

	/**
	 * Save or update a setting and return the corresponding identifier.
	 * 
	 * @param name
	 *            the setting name.
	 * @param value
	 *            the initial value.
	 */
	@POST
	@PUT
	@Path("{name}/{value}")
	public void saveOrUpdate(@PathParam("name") final String name, @PathParam("value") final String value) {
		final String user = SecurityContextHolder.getContext().getAuthentication().getName();
		final SystemUserSetting setting = repository.findByLoginAndName(user, name);
		if (setting == null) {
			final SystemUserSetting entity = new SystemUserSetting();
			entity.setLogin(SecurityContextHolder.getContext().getAuthentication().getName());
			entity.setName(name);
			entity.setValue(value);
			repository.saveAndFlush(entity);
		} else {
			setting.setValue(value);
		}
	}

	/**
	 * Delete an {@link SystemUserSetting}
	 * 
	 * @param name
	 *            the user setting name to update.
	 */
	@DELETE
	@Path("{name}")
	public void delete(@PathParam("name") final String name) {
		repository.delete(name, SecurityContextHolder.getContext().getAuthentication().getName());
	}

}
