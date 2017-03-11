/*
 * Copyright 2008-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ligoj.bootstrap.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.DateTime;
import org.springframework.data.domain.Auditable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import org.ligoj.bootstrap.core.AuditedBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for auditable entities. Stores the audition values in persistent fields.
 * 
 * @param <ID>
 *            the type of the auditing type's identifier
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AbstractAudited<ID extends Serializable> extends AbstractPersistable<ID> implements Auditable<String, ID> {

	private static final long serialVersionUID = 141481953116476081L;

	/**
	 * Created author will never be updated
	 */
	@Column(updatable = false)
	@JsonProperty(access = Access.READ_ONLY)
	private String createdBy;

	/**
	 * Created date will never be updated
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	@JsonProperty(access = Access.READ_ONLY)
	private Date createdDate;

	@JsonProperty(access = Access.READ_ONLY)
	private String lastModifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonProperty(access = Access.READ_ONLY)
	private Date lastModifiedDate;

	@Override
	public DateTime getCreatedDate() {
		return AuditedBean.toDatetime(createdDate);
	}

	@Override
	public void setCreatedDate(final DateTime createdDate) {
		this.createdDate = AuditedBean.toDate(createdDate);
	}

	@Override
	public DateTime getLastModifiedDate() {
		return AuditedBean.toDatetime(lastModifiedDate);
	}

	@Override
	public void setLastModifiedDate(final DateTime lastModifiedDate) {
		this.lastModifiedDate = AuditedBean.toDate(lastModifiedDate);
	}
}
