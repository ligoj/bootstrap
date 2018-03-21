/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Corporate user for edition.
 */
@Getter
@Setter
public class SystemUserEditionVo extends AbstractSystemUserVo {

	/**
	 * Roles identifiers
	 */
	private List<Integer> roles = new ArrayList<>();

}
