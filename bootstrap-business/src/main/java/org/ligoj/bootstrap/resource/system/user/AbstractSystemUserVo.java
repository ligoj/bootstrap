package org.ligoj.bootstrap.resource.system.user;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

/**
 * Corporate user.
 */
@Getter
@Setter
public abstract class AbstractSystemUserVo {

	/**
	 * Corporate user login.
	 */
	@NotEmpty
	@NotNull
	private String login;


}
