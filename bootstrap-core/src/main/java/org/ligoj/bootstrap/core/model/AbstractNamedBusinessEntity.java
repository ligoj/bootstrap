/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

	@NotNull
	@NotBlank
	private String name;

}
