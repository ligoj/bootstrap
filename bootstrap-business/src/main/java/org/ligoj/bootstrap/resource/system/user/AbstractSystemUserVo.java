/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
