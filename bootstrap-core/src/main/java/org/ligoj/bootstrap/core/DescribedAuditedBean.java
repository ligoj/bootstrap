/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;

import lombok.Getter;
import lombok.Setter;

/**
 * An audited and described bean
 * 
 * @param <K>
 *            the type of the identifier
 * @param <U>
 *            the type of the author
 */
@Getter
@Setter
public class DescribedAuditedBean<U extends Serializable, K extends Serializable> extends NamedAuditedBean<U, K> implements IDescribableBean<K> {

	@Length(max = 250)
	private String description;

}
