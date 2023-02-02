/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.test;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.ligoj.bootstrap.core.model.AbstractPersistable;

import lombok.Getter;
import lombok.Setter;

/**
 * Wine entity.
 */
@Entity
@Table(name = "DEMO_WINE")
@Getter
@Setter
public class Wine extends AbstractPersistable<Integer> {

	@Size(max = 50)
	private String name;

	@Size(max = 50)
	private String wneGrpe;

	@Size(max = 20)
	private String wneCnty;

	@Size(max = 50)
	private String wneRegn;

	@Min(1700)
	@Max(2100)
	private Integer wneYear;

	@Size(max = 255)
	private String wnePict;

	@Length(max = 500)
	private String wneDesc;

}
