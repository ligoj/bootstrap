/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * User setting.
 */
@Entity
@Table(name = "S_USER_SETTING", uniqueConstraints = @UniqueConstraint(columnNames = { "login", "name" }))
@Getter
@Setter
public class SystemUserSetting extends AbstractNamedValue<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Corporate user login.
	 */
	@NotEmpty
	@NotNull
	private String login;

	/**
	 * When true, the value will be exposed as a JSON property, but a cookie.
	 */
	private boolean cookie;
}
