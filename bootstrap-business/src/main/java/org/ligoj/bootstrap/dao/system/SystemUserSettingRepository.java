/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import java.util.List;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemUserSetting;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * User Settings repository.
 */
public interface SystemUserSettingRepository extends RestRepository<SystemUserSetting, Integer> {

	/**
	 * Return user settings.
	 * 
	 * @param login
	 *            User login.
	 * @return {@link SystemUserSetting} list.
	 */
	List<SystemUserSetting> findByLogin(String login);

	/**
	 * Return user settings.
	 * 
	 * @param login
	 *            User login.
	 * @param name
	 *            The setting name.
	 * @return {@link SystemUserSetting}, may be <code>null</code>.
	 */
	SystemUserSetting findByLoginAndName(String login, String name);

	/**
	 * Delete a user setting.
	 * 
	 * @param login
	 *            User login.
	 * @param name
	 *            Setting name.
	 */
	@Query("DELETE SystemUserSetting WHERE login=:login AND name=:name")
	@Modifying
	void delete(String login, String name);

}
