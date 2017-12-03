package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;

import org.ligoj.bootstrap.core.IDescribableBean;

/**
 * Named, described and audited entity.
 * 
 * @param <K>
 *            Identifier type.
 */
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDescribedAuditedEntity<K extends Serializable> extends AbstractNamedAuditedEntity<K> implements IDescribableBean<K> {

	/**
	 * Human readable description.
	 */
	@Length(max = 250)
	private String description;

}
