package org.ligoj.bootstrap.model.system;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

/**
 * System configuration. The name property corresponds to the key.
 */
@Entity
@Table(name = "S_CONFIGURATION", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
public class SystemConfiguration extends AbstractNamedValue<Integer> {

	// Nothing

}
