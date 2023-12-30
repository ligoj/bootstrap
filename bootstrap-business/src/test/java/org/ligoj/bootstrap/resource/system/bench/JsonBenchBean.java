/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.bench;

import java.util.List;

/**
 * A simple collection wrapper in order to perform a performance test with Jackson and Jettison providers.
 */
class JsonBenchBean {

	/**
	 * Bean collection (some items for performance test)
	 */
	private List<SimpleJsonBean> beans;

	/**
	 * Return the {@link #beans} value.
	 * 
	 * @return the {@link #beans} value.
	 */
	List<SimpleJsonBean> getBeans() {
		return beans;
	}

	/**
	 * Set the {@link #beans} value.
	 * 
	 * @param beans
	 *            the {@link #beans} to set.
	 */
	void setBeans(final List<SimpleJsonBean> beans) {
		this.beans = beans;
	}

}
