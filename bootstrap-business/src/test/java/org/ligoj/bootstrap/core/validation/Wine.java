package org.ligoj.bootstrap.core.validation;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import lombok.Getter;
import lombok.Setter;

/**
 * Win demo resource data.
 */
@Getter
@Setter
public class Wine {

	@Min(0)
	private int id;

	@NotEmpty
	@UpperCase
	@Length(max = 50)
	private String name;

	@NotEmpty
	@Length(max = 50)
	private String grapes;

	@NotEmpty
	@Length(max = 20)
	private String country;

	@NotEmpty
	@Length(max = 50)
	private String region;

	@NotNull
	@Range(min = 1900, max = 2050)
	private Integer year;

	@Length(max = 255)
	private String picture;

	@Length(max = 500)
	private String description;
}
