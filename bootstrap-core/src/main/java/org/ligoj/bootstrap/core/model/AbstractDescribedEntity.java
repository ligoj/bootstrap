/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.ligoj.bootstrap.core.IDescribableBean;
import org.ligoj.bootstrap.core.validation.SafeHtml;

import java.io.Serializable;

/**
 * Named and described entity.
 * @param <K>
 *            Identifier type.
 */
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDescribedEntity<K extends Serializable> extends AbstractNamedEntity<K> implements IDescribableBean<K> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Object description. Accepting safe HTML content.
	 */
	@Length(max = 250)
	@SafeHtml
	private String description;

}
