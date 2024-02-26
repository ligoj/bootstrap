/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Simple wrapper item for test.
 *
 * @param <K> Wrapped data type
 */
@Getter
@Setter
public class TableItem<K> {
	/**
	 * Records in data.
	 */
	private long recordsTotal;

	/**
	 * Records in database when filtering has been applied. Useless while filtering is not enabled; since is equal to
	 * {@link #recordsTotal}.
	 */
	private long recordsFiltered;

	/**
	 * Draw
	 */
	private String draw;

	/**
	 * data
	 */
	private List<K> data;

	/**
	 * Optional extensions.
	 */
	private Map<String, ?> extensions;

}
