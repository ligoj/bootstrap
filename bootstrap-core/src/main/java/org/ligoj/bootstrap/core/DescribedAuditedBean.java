package org.ligoj.bootstrap.core;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;

import lombok.Getter;
import lombok.Setter;

/**
 * An audited and described bean
 * 
 * @param <ID>
 *            the type of the identifier
 * @param <U>
 *            the type of the author
 */
@Getter
@Setter
public class DescribedAuditedBean<U extends Serializable, ID extends Serializable> extends NamedAuditedBean<U, ID> implements IDescribableBean<ID> {

	@Length(max = 250)
	private String description;

}
