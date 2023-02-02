/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import org.ligoj.bootstrap.core.model.AbstractNamedEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
}
