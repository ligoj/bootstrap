/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private LocalDateTime lastConnection;

	@Transient
	private Map<String, String> map;

	@Transient
	private List<String> list;

	@Transient
	private Set<CascadeType> setEnum;
}
