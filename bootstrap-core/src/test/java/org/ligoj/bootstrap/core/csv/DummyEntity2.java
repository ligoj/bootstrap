/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.Getter;
import lombok.Setter;

/**
 * entity used to test database dialect
 */
@Entity
@Getter
@Setter
public class DummyEntity2 {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private String dialChar;
	private Boolean dialBool;
	private Short dialShort;
	private Long dialLong;
	private double dialDouble;
	private Date dialDate;
	private LocalDate localDate;
	private CascadeType dialEnum;
	@ManyToOne
	private DummyEntity2 link;
	@ManyToOne
	private DummyEntity3 user;
	@OneToMany
	private List<DummyEntity2> children;
	@OneToMany(mappedBy = "link")
	private Set<DummyEntity2> linkedChildren;
	@OneToMany
	private Collection<DummyEntity2> linkedChildrenCollection;
}
