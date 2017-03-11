package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import org.ligoj.bootstrap.core.INamableBean;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract entity with business key and name.
 * 
 * @param <ID>
 *            the type of the identifier
 */
@Getter
@Setter
@MappedSuperclass
@ToString(of = "name")
public abstract class AbstractNamedBusinessEntity<ID extends Serializable> extends AbstractBusinessEntity<ID> implements INamableBean<ID> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@NotNull
	@NotBlank
	private String name;

}
