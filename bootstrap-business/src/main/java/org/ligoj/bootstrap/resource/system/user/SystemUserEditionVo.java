/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Pattern;
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

	/**
	 * When defined, an API token is created with this name.
	 */
	@Pattern(regexp = "[\\w\\-.]+")
	private String apiToken;
}
