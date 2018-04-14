/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json.jqgrid;

import lombok.Getter;
import lombok.Setter;

/**
 * A page request containing filters in addition of the Spring Data variables.
 */
@Getter
@Setter
public class UiPageRequest {

	/**
	 * Base 1 page number.
	 */
	private int page = 1;

	/**
	 * Page size.
	 */
	private int pageSize = 10;

	/**
	 * UI filters.
	 */
	private UiFilter uiFilter;

	/**
	 * UI sort.
	 */
	private UiSort uiSort;

}
