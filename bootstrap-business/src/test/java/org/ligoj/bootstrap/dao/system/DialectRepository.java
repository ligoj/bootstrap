/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemDialect;

/**
 * {@link SystemDialect} repository.
 */
public interface DialectRepository extends RestRepository<SystemDialect, Integer> {
	// All is delegated
}
