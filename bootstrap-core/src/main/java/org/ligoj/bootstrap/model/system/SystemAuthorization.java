/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.ligoj.bootstrap.core.model.AbstractAudited;

/**
 * A dynamic authorization for a role on a specified module.
 */
@Entity
@Table(name = "S_AUTHORIZATION")
@Getter
@Setter
public class SystemAuthorization extends AbstractAudited<Integer> {

	/**
	 * Authorization type.
	 */
	public enum AuthorizationType {
		/**
		 * API authorization.
		 */
		API,

		/**
		 * UI authorization.
		 */
		UI
	}

	@Enumerated(EnumType.STRING)
	@NotNull
	private AuthorizationType type;

	/**
	 * Associated role.
	 */
	@ManyToOne
	@NotNull
	private SystemRole role;

	/**
	 * Authorized URL as pattern.
	 */
	@NotNull
	private String pattern;

	/**
	 * Authorized URL method. Can be <code>null</code> for all methods.
	 */
	private String method;

}
