package org.ligoj.bootstrap.resource.system.security;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

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
	 * authorizations.
	 */
	@NotEmpty
	private List<AuthorizationEditionVo> authorizations = new ArrayList<>();

}
