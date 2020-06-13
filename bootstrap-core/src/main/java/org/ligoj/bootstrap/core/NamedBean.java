/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A named bean
 * 
 * @param <K> the type of the identifier
 */
@Getter
@Setter
@ToString(of = "name")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamedBean<K extends Serializable> implements INamableBean<K> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Identifier of this bean.
	 */
	private K id;

	@NotBlank
	private String name;

	/**
	 * Copy a bean to another one.
	 * 
	 * @param <T>  Bean type.
	 * @param from the source bean.
	 * @param to   the target bean.
	 */
	public static <T extends Serializable> void copy(final INamableBean<T> from, final INamableBean<T> to) {
		to.setId(from.getId());
		to.setName(from.getName());
	}

}
