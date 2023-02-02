/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.ligoj.bootstrap.core.model.AbstractPersistable;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;

import lombok.Getter;
import lombok.Setter;

/**
 * entity used to test database dialect
 */
@Entity
@Table(name = "S_DIALECT")
@Getter
@Setter
public class SystemDialect extends AbstractPersistable<Integer> {

	@Column(name = "DIA_CHAR")
	private String dialChar;

	@Column(name = "DIA_BOOL")
	private Boolean dialBool;

	@Column(name = "DIA_SHOR")
	private Short dialShort;

	@Column(name = "DIA_LONG")
	private Long dialLong;

	@Column(name = "DIA_DOUB")
	private double dialDouble;

	@Column(name = "DIA_DATE")
	private Date dialDate;

	@Column(name = "DIA_ENUM")
	private CascadeType dialEnum;

	@ManyToOne
	private SystemDialect link;

	@ManyToOne
	private SystemUser user;

	private AuthorizationType authorization;

	@OneToMany
	private List<SystemDialect> children;

	@OneToMany(mappedBy = "link")
	private Set<SystemDialect> linkedChildren;

}
