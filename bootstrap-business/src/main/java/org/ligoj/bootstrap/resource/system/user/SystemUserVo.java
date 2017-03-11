package org.ligoj.bootstrap.resource.system.user;

import java.util.ArrayList;
import java.util.List;

import org.ligoj.bootstrap.resource.system.security.SystemRoleVo;
import lombok.Getter;
import lombok.Setter;

/**
 * Corporate user details.
 */
@Getter
@Setter
public class SystemUserVo extends AbstractSystemUserVo {

	/**
	 * Human readable roles
	 */
	private List<SystemRoleVo> roles = new ArrayList<>();

}
