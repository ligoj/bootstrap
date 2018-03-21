/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemConfiguration;

/**
 * System configuration {@link SystemConfiguration} repository.
 */
public interface SystemConfigurationRepository extends RestRepository<SystemConfiguration, Integer> {

	// Everything is delegated

}
