/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Simple basic entity.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "login")
@ToString(of = "login")
public class DummyEntity3 {

	@Id
	private String login;

	@Temporal(TemporalType.DATE)
	private Date lastConnection;

	@Transient
	private Map<String, String> map;

	@Transient
	private List<String> list;

	@Transient
	private Set<CascadeType> setEnum;
}
