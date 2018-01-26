package org.ligoj.bootstrap.resource.system.security;

import javax.validation.constraints.NotNull;

import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple authorization for creation and update operations.
 */
@Getter
@Setter
public class AuthorizationEditionVo {

	/**
	 * technical id
	 */
	private Integer id;
	
	/**
	 * Identifier of authorized role.
	 */
	@NotNull
	private Integer role;

	@NotNull
	private AuthorizationType type;

	/**
	 * Authorized URL.
	 */
	private String pattern;

}
