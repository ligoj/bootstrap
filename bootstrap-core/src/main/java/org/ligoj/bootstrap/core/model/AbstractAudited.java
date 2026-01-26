/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.Instant;

/**
 * Abstract base class for auditable entities. Stores the audition values in
 * persistent fields.
 * 
 * @param <K>
 *            The type of the auditing type's identifier
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AbstractAudited<K extends Serializable> extends AbstractPersistable<K> implements Auditable<String, K, Instant> {

	/**
	 * Created author will never be updated
	 */
	@Column(updatable = false)
	@JsonProperty(access = Access.READ_ONLY)
	@CreatedBy
	private String createdBy;

	/**
	 * Created date will never be updated
	 */
	@Column(updatable = false)
	@JsonProperty(access = Access.READ_ONLY)
	@CreatedDate
	private Instant createdDate;

	@JsonProperty(access = Access.READ_ONLY)
	@LastModifiedBy
	private String lastModifiedBy;

	@JsonProperty(access = Access.READ_ONLY)
	@LastModifiedDate
	private Instant lastModifiedDate;

	/**
	 * A free form text at creation time only.
	 */
	@Column(length = 1024, updatable = false)
	private String creationContext;

	/**
	 * An optional free form meta-data
	 */
	@Column(length = 2048)
	private String metadata;

}
