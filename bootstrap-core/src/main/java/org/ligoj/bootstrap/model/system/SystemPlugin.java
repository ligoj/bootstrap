/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.ligoj.bootstrap.core.model.AbstractAudited;

import lombok.Getter;
import lombok.Setter;

/**
 * Data model representing a plug-in and its state. The key corresponds to the feature key. A plug-in may includes
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
	 * The plug-in type.
	 */
	@NotNull
	@Column(length = 10)
	private String type;

}
