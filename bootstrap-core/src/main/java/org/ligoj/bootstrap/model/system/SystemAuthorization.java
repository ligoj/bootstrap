package org.ligoj.bootstrap.model.system;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpMethod;

import org.ligoj.bootstrap.core.model.AbstractAudited;
import lombok.Getter;
import lombok.Setter;

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
		 * Business authorization.
		 */
		BUSINESS,

		/**
		 * UI authorization.
		 */
		UI
	}

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.ORDINAL)
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
	 * Authorized URL method. Can be <tt>null</tt> for all methods.
	 */
	private HttpMethod method;

}
