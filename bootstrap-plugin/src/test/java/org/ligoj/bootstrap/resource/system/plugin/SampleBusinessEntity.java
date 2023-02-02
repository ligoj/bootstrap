/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.ligoj.bootstrap.core.model.AbstractBusinessEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * Sample entity extending {@link AbstractBusinessEntity}
 */
@Entity
@Table(name = "SAMPLE_BUSINESS_ENTITY")
@Getter
@Setter
public class SampleBusinessEntity extends AbstractBusinessEntity<String> {

	// No extension
}
