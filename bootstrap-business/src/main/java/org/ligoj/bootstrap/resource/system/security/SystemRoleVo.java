/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import org.ligoj.bootstrap.core.NamedBean;

import lombok.Getter;
import lombok.Setter;

/**
 * Corporate user.
 */
@Getter
@Setter
public class SystemRoleVo extends NamedBean<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * authorizations.
	 */
	@NotEmpty
	private transient List<AuthorizationEditionVo> authorizations = new ArrayList<>();

}
