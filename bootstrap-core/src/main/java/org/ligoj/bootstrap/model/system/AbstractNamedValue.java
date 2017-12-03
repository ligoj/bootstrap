package org.ligoj.bootstrap.model.system;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ligoj.bootstrap.core.model.AbstractNamedAuditedEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An audited, named and valued entity
 * 
 * @param <K>
 *            Identifier type.
 */
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractNamedValue<K extends Serializable> extends AbstractNamedAuditedEntity<K> {

	/**
	 * Value as string.
	 */
	@NotEmpty
	@NotNull
	@Size(max = 1023)
	private String value;

}
