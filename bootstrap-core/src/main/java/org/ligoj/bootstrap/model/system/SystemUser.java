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
	 * Administrator implicit criteria. Reflects the <strong>truthful</strong> administration access level of the
	 * current principal: the {@value org.ligoj.bootstrap.core.security.SecurityHelper#ADMIN} virtual authority granted
	 * by {@code RbacUserDetailsService} when one of the resolved authorities holds an administrative API authorization.
	 * <p>
	 * This is a Spring Data SpEL bind parameter ({@code :#{...}}): it is evaluated against the security context on
	 * <em>each</em> query execution and bound as a regular parameter, so — unlike a render-time constant — it is safe
	 * with Hibernate's query plan cache and includes authorities that are not stored in database (e.g.
	 * {@code ISessionSettingsProvider} contributions).
	 *
	 * @see org.ligoj.bootstrap.core.security.SecurityHelper#isAdmin()
	 */
	public static final String IS_ADMIN = "(:#{@securityHelper.isAdmin()} = TRUE)";

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
