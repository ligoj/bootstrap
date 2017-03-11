package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.hibernate.validator.constraints.Length;

import org.ligoj.bootstrap.core.IDescribableBean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Named and described entity.
 * @param <PK>
 *            Identifier type.
 */
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDescribedEntity<PK extends Serializable> extends AbstractNamedEntity<PK> implements IDescribableBean<PK> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Object description.
	 */
	@Length(max = 250)
	private String description;

}
