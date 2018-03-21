/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

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
