/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;

import org.ligoj.bootstrap.core.INamableBean;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Named entity
 * 
 * @param <K>
 *            Identifier type.
 */
@Getter
@Setter
@MappedSuperclass
@ToString(of = "name")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractNamedEntity<K extends Serializable> extends AbstractPersistable<K> implements INamableBean<K> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Human readable name.
	 */
	@NotBlank
	private String name;

}
