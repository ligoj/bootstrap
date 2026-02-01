/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.ligoj.bootstrap.core.model.AbstractNamedEntity;

import java.time.Instant;

/**
 * API token.
 */
@Entity
@Table(name = "S_API_TOKEN", uniqueConstraints = @UniqueConstraint(columnNames = { "user", "name" }))
@Getter
@Setter
@ToString(of = "user")
public class SystemApiToken extends AbstractNamedEntity<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * User login.
	 */
	@NotNull
	private String user;

	/**
	 * Encrypted API Token.
	 */
	@NotNull
	private String token;

	/**
	 * Hashed API Token.
	 */
	@NotNull
	private String hash;

	/**
	 * Optional maximal usage date of this token.
	 */
	@Column(updatable = false)
	private Instant expiration;
}
