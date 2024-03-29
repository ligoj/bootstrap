package org.ligoj.bootstrap.core.model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

/**
 * Abstract entity having a string key. This fixed type for id is required for some JPQL queries and might be removed
 * with a full support of the genericity of Hibernate.
 */
@Getter
@Setter
@ToString
@MappedSuperclass
public abstract class AbstractStringKeyEntity implements Persistable<String> {

	/**
	 * Business key.
	 */
	@Id
	@NotNull
	private String id;

	/**
	 * Returns if the {@code Persistable} is new or was persisted already.
	 *
	 * @return if {@literal true} the object is new.
	 */
	public boolean isNew() {
		return getId() == null;
	}

}