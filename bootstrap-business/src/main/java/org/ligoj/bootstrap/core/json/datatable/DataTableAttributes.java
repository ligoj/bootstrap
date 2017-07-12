/**
 * 
 */
package org.ligoj.bootstrap.core.json.datatable;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * The DataTables' attributes
 */
@Component
public final class DataTableAttributes {

	/**
	 * Echo SID used by DataTable.
	 */
	public static final String ECHO = "draw";

	/**
	 * Offset start.
	 */
	public static final String START = "start";

	/**
	 * Page length.
	 */
	public static final String PAGE_LENGTH = "length";

	/**
	 * Query parameter format to access to a property.
	 */
	public static final String DATA_PROP = "columns[%s][data]";

	/**
	 * Index of sorted column.
	 */
	public static final String SORTED_COLUMN = "order[0][column]";

	/**
	 * Sorting direction.
	 */
	public static final String SORT_DIRECTION = "order[0][dir]";

	/**
	 * Global dataTable filter.
	 */
	public static final String SEARCH = "search[value]";

	private DataTableAttributes() {
		// empty constructor
	}

	/**
	 * Return the search filter from the query parameters.
	 * 
	 * @param uriInfo
	 *            filter data.
	 * @return the search filter from the query parameters. May be
	 *         <code>null</code>.
	 */
	public static String getSearch(final UriInfo uriInfo) {
		return StringUtils.trimToEmpty(ObjectUtils.defaultIfNull(uriInfo.getQueryParameters().getFirst("q"),
				uriInfo.getQueryParameters().getFirst(SEARCH)));
	}

}
