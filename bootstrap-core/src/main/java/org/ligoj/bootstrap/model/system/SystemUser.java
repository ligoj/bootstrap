package org.ligoj.bootstrap.model.system;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
	@Temporal(TemporalType.DATE)
	private Date lastConnection;

	/**
	 * Associated roles
	 */
	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
	@JsonIgnore
	private Set<SystemRoleAssignment> roles;
}
