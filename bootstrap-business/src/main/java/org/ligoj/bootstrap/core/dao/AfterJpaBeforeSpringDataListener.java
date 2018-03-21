/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

/**
 * Listener invoked after JPA is configured, but before Spring data validate the queries.
 */
@FunctionalInterface
public interface AfterJpaBeforeSpringDataListener {

	/**
	 * Invoked after JPA is configured, but before Spring data validate the queries.
	 */
	void callback();
}
