package org.ligoj.bootstrap.core.csv;

import java.util.Date;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
}
