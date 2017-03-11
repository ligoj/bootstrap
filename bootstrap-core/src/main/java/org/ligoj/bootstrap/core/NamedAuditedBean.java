package org.ligoj.bootstrap.core;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * An audited and named bean
 * 
 * @param <ID>
 *            the type of the identifier
 * @param <U>
 *            the type of the author
 */
@Getter
@Setter
@ToString(of = "name")
public class NamedAuditedBean<U extends Serializable, ID extends Serializable> extends AuditedBean<U, ID> implements INamableBean<ID> {

	@NotBlank
	@NotNull
	private String name;

}
