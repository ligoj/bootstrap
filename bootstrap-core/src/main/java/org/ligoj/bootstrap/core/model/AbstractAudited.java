/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.util.Date;

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
public abstract class AbstractAudited<K extends Serializable> extends AbstractPersistable<K> implements Auditable<String, K, Date> {

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
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	@JsonProperty(access = Access.READ_ONLY)
	@CreatedDate
	private Date createdDate;

	@JsonProperty(access = Access.READ_ONLY)
	@LastModifiedBy
	private String lastModifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonProperty(access = Access.READ_ONLY)
	@LastModifiedDate
	private Date lastModifiedDate;

}
