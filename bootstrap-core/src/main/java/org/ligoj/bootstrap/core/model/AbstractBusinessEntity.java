package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract entity with business key. No auto generated key.
 * 
 * @param <K>
 *            The type of the identifier
 */
@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(value = "new")
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
