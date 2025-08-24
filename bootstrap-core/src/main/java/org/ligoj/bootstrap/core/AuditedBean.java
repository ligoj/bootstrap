/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

import jakarta.persistence.Column;
import org.ligoj.bootstrap.core.model.Auditable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class for audited objects.
 *
 * @param <K> the type of the identifier
 * @param <U> the type of the author
 */
@Getter
@Setter
@ToString(of = "id")
public class AuditedBean<U extends Serializable, K extends Serializable> {

	private K id;

	/**
	 * Creation date.
	 */
	@JsonProperty(access = Access.READ_ONLY)
	private Instant createdDate;

	/**
	 * Last update date.
	 */
	@JsonProperty(access = Access.READ_ONLY)
	private Instant lastModifiedDate;

	/**
	 * The author of this data.
	 */
	@JsonProperty(access = Access.READ_ONLY)
	private U createdBy;

	/**
	 * The last update author of this data.
	 */
	@JsonProperty(access = Access.READ_ONLY)
	private U lastModifiedBy;

	/**
	 * A free form text at creation time only.
	 */
	@Column(length = 1024, updatable = false)
	private String creationContext;

	/**
	 * Define the modifiable state of a valued object.
	 *
	 * @param <T>  Bean type.
	 * @param from The source object to copy to current one.
	 */
	public <T extends Auditable<U, K, Instant>> void copyAuditData(final T from) {
		copyAuditData(from, Function.identity());
	}

	/**
	 * Define the modifiable state of a valued object.
	 *
	 * @param from          The source object to copy to current one.
	 * @param userConverter the user converter.
	 * @param <S>           Bean type of source parameter.
	 * @param <T>           User type of source parameter.
	 */
	public <T, S extends Auditable<T, K, Instant>> void copyAuditData(final S from,
			final Function<T, ? extends U> userConverter) {
		if (from != null) {
			// Copy audit dates
			this.createdDate = from.getCreatedDate();
			this.lastModifiedDate = from.getLastModifiedDate();
			this.createdBy = userConverter.apply(from.getCreatedBy());
			this.lastModifiedBy = userConverter.apply(from.getLastModifiedBy());
			this.creationContext = from.getCreationContext();
		}
	}

	/**
	 * Copy auditable data .
	 *
	 * @param <T>  Bean source type.
	 * @param <U>  Bean target type.
	 * @param <S>  "Source" type.
	 * @param <D>  "Destination" type.
	 * @param <L>  Date type.
	 * @param from The source object.
	 * @param to   The target object.
	 */
	public static <L, U extends Serializable, T extends Serializable, S extends Auditable<U, T, L>, D extends Auditable<U, T, L>> void copyAuditData(
			final S from, final D to) {
		if (from != null) {
			// Copy audit properties
			to.setCreatedDate(from.getCreatedDate());
			to.setLastModifiedDate(from.getLastModifiedDate());
			to.setCreatedBy(from.getCreatedBy());
			to.setLastModifiedBy(from.getLastModifiedBy());
			to.setCreationContext(from.getCreationContext());
		}
	}
}
