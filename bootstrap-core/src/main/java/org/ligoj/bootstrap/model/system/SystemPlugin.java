/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import org.ligoj.bootstrap.core.model.AbstractAudited;

import lombok.Getter;
import lombok.Setter;

/**
 * Data model representing a plug-in and its state. The key corresponds to the feature key. A plug-in may include
 * several services or features.
 */
@Getter
@Setter
@Entity
@Table(name = "S_PLUGIN", uniqueConstraints = @UniqueConstraint(columnNames = "key"))
public class SystemPlugin extends AbstractAudited<Integer> implements Serializable {
	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The currently installed plug-in version. Should follow the <a href="http://semver.org/">semantic versioning</a>
	 */
	@NotNull
	private String version;

	/**
	 * The feature key.
	 */
	@NotNull
	private String key;

	/**
	 * The Maven artifact id.
	 */
	private String artifact;

	/**
	 * The Java base package of this plugin.
	 */
	private String basePackage;

	/**
	 * The plug-in type.
	 */
	@NotNull
	@Column(length = 10)
	private String type;

}
