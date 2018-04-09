/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.SafeHtml.Attribute;
import org.hibernate.validator.constraints.SafeHtml.Tag;
import org.ligoj.bootstrap.core.IDescribableBean;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
	@SafeHtml(additionalTagsWithAttributes = @Tag(name = "a", attributesWithProtocols = @Attribute(name = "href", protocols = "#")))
	private String description;

}
