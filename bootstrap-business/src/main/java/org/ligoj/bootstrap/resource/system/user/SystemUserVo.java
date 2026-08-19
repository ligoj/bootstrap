/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import lombok.Getter;
import lombok.Setter;
import org.ligoj.bootstrap.core.NamedBean;
import org.ligoj.bootstrap.resource.system.security.SystemRoleVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Corporate user details.
 */
@Getter
@Setter
public class SystemUserVo extends AbstractSystemUserVo {

	/**
	 * Human-readable assigned roles
	 */
	private List<SystemRoleVo> roles = new ArrayList<>();

	/**
	 * Optional first name, filled by the available {@link ISystemUserDetailsProvider} implementations. May be
	 * <code>null</code> when there is no provider or no match.
	 */
	private String firstName;

	/**
	 * Optional last name, filled by the available {@link ISystemUserDetailsProvider} implementations. May be
	 * <code>null</code> when there is no provider or no match.
	 */
	private String lastName;

	/**
	 * Optional mails, filled by the available {@link ISystemUserDetailsProvider} implementations. May be
	 * <code>null</code> when there is no provider or no match.
	 */
	private List<String> mails;

	/**
	 * Federated roles, not directly assigned.
	 */
	private List<NamedBean<String>> federatedRoles;

}
