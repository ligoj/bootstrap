/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.ligoj.bootstrap.core.model.AbstractNamedAuditedEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * System configuration. The name property corresponds to the key.
 */
@Entity
@Table(name = "S_CONFIGURATION", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
public class SystemConfiguration extends AbstractNamedAuditedEntity<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Value as string.
	 */
	@NotBlank
	@Size(max = 8192)
	private String value;

}
