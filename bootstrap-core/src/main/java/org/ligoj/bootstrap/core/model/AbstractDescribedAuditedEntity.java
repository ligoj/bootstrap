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
 * @param <PK>
 *            Identifier type.
 */
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDescribedAuditedEntity<PK extends Serializable> extends AbstractNamedAuditedEntity<PK> implements IDescribableBean<PK> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Human readable description.
	 */
	@Length(max = 250)
	private String description;

}
