/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;

import org.ligoj.bootstrap.core.INamableBean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract entity with business key and name.
 * 
 * @param <K>
 *            the type of the identifier
 */
@Getter
@Setter
@MappedSuperclass
@ToString(of = "name")
public abstract class AbstractNamedBusinessEntity<K extends Serializable> extends AbstractBusinessEntity<K> implements INamableBean<K> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@NotBlank
	private String name;

}
