/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

/**
 * Corporate user.
 */
@Entity
@Table(name = "S_USER")
@Getter
@Setter
@EqualsAndHashCode(of = "login")
@ToString(of = "login")
public class SystemUser implements Serializable {

	/**
	 * Administrator role implicit criteria.
	 */
	public static final String IS_ADMIN = """
			 (EXISTS(SELECT 1 FROM SystemRoleAssignment ra INNER JOIN ra.role r WHERE ra.user.id = :user
			         AND EXISTS(SELECT 1 FROM SystemAuthorization a WHERE a.role = r AND a.pattern = '.*'
			                AND a.type = org.ligoj.bootstrap.model.system.SystemAuthorization$AuthorizationType.API)
			 ))
			""";

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Corporate user login.
	 */
	@Id
	@NotEmpty
	private String login;

	/**
	 * Last known connection.
	 */
	private Instant lastConnection;

	/**
	 * Associated roles
	 */
	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
	@JsonIgnore
	private Set<SystemRoleAssignment> roles;
}
