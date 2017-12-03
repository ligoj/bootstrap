package org.ligoj.bootstrap.model.system;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
	 * Corporate user login.
	 */
	@NotEmpty
	@NotNull
	private String login;

	/**
	 * When true, the value will be be exposed as a JSON property, but a cookie.
	 */
	private boolean cookie;
}
