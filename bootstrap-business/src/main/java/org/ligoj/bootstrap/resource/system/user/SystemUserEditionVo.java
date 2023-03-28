/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Corporate user for edition.
 */
@Getter
@Setter
public class SystemUserEditionVo extends AbstractSystemUserVo {

	/**
	 * Roles identifiers
	 */
	private Set<Integer> roles = new HashSet<>();

	/**
	 * When defined, an API token is created with this name.
	 */
	@Pattern(regexp = "[\\w.-]+")
	private String apiToken;
}
