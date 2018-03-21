/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

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
}
