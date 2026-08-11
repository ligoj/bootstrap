/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

/**
 * Abstract entity with business key. No auto generated key.
 * 
 * @param <K>
 *            The type of the identifier
 */
@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties("new")
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public abstract class AbstractBusinessEntity<K extends Serializable> implements Persistable<K> {

	/**
	 * Business key.
	 */
	@Id
	@NotNull
	private K id;

	@Override
	public boolean isNew() {
		return id == null;
	}
}
