package org.ligoj.bootstrap.dao.system;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemUserSetting;

/**
 * User Settings repository.
 */
public interface SystemUserSettingRepository extends RestRepository<SystemUserSetting, Integer> {

	/**
	 * Return user settings.
	 * 
	 * @param user
	 *            user login.
	 * @return {@link SystemUserSetting} list.
	 */
	List<SystemUserSetting> findByLogin(String login);

	/**
	 * Return user settings.
	 * 
	 * @param user
	 *            user login.
	 * @param name
	 *            the setting name.
	 * @return {@link SystemUserSetting}, may be <code>null</code>.
	 */
	SystemUserSetting findByLoginAndName(String login, String name);

	/**
	 * Delete a setting.
	 * 
	 * @param name
	 *            setting name.
	 * @param login
	 *            user login.
	 */
	@Query("DELETE SystemUserSetting WHERE login=:login AND name=:name")
	@Modifying
	void delete(@Param("name") String name, @Param("login") String login);

}
