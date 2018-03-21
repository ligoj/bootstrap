/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.ligoj.bootstrap.core.model.AbstractAudited;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Role.
 */
@Entity
@Getter
@Setter
@Table(name = "S_ROLE", uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
@ToString(of = "name")
public class SystemRole extends AbstractAudited<Integer> implements GrantedAuthority {

	/**
	 * The default role, all authenticated users get it.
	 */
	public static final String DEFAULT_ROLE = "USER";

	/**
	 * SID
	 */
	private static final long serialVersionUID = -7118550223873607955L;

	/**
	 * Role name.
	 */
	@NotNull
	@NotEmpty
	@Length(max = 200)
	private String name;

	@Override
	@JsonIgnore
	public String getAuthority() {
		return getName();
	}
}
