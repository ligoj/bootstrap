/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * An audited and named bean
 * 
 * @param <K>
 *            The type of the identifier
 * @param <U>
 *            he type of the author
 */
@Getter
@Setter
@ToString(of = "name")
public class NamedAuditedBean<U extends Serializable, K extends Serializable> extends AuditedBean<U, K> implements INamableBean<K> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@NotBlank
	@NotNull
	private String name;

}
