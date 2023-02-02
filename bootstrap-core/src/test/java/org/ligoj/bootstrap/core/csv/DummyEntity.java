/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import lombok.Getter;
import lombok.Setter;

/**
 * Dummy entity.
 */
@Entity
@Getter
@Setter
public class DummyEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private String name;
	private String wneGrpe;
	private String wneCnty;
	private String wneRegn;
	private Integer wneYear;
	private String wnePict;
	
	@Length(max=500)
	@Size(max = 500)
	private String wneDesc;

}
