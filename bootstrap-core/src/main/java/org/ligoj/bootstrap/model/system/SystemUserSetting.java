package org.ligoj.bootstrap.model.system;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import org.ligoj.bootstrap.core.model.AbstractNamedAuditedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * User setting.
 */
@Entity
@Table(name = "S_USER_SETTING")
@Getter
@Setter
public class SystemUserSetting extends AbstractNamedAuditedEntity<Integer> {

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
	 * Value as string.
	 */
	@NotEmpty
	@NotNull
	@Size(max = 511)
	private String value;

	/**
	 * When true, the value will be be exposed as a JSON property, but a cookie.
	 */
	private boolean cookie;
}
