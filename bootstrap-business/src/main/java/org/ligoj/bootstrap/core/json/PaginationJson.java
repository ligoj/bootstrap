/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.ObjectUtils;
import org.ligoj.bootstrap.core.json.datatable.DataTableAttributes;
import org.ligoj.bootstrap.core.json.jqgrid.UiFilter;
import org.ligoj.bootstrap.core.json.jqgrid.UiPageRequest;
import org.ligoj.bootstrap.core.json.jqgrid.UiSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Pagination management utility class for DataTables.
 */
@Component
@Slf4j
public class PaginationJson {

	/**
	 * Default page size when not provided.
	 */
	public static final int DEFAULT_PAGE_SIZE = 10;

	/**
	 * Default page size when not provided.
	 */
	protected static final Map<String, String> IDENTITY_MAPPING = Collections.singletonMap("*", "*");

	/**
	 * Get JQGrid pagination information from query parameters.
	 *
	 * @param uriInfo
	 *            query parameters.
	 * @return a page request build with sort and filter.
	 */
	public UiPageRequest getUiPageRequest(final UriInfo uriInfo) {
		return getUiPageRequest(uriInfo, IDENTITY_MAPPING);
	}

	/**
	 * Get JQGrid pagination information from query parameters.
	 *
	 * @param uriInfo
	 *            query parameters.
	 * @param ormMapping
	 *            Optional JSon to ORM property mapping.
	 * @return a page request build with sort and filter.
	 */
	public UiPageRequest getUiPageRequest(final UriInfo uriInfo, final Map<String, String> ormMapping) {
		final var parameters = uriInfo.getQueryParameters();

		// Build the page request object
		final var request = new UiPageRequest();
		request.setUiFilter(buildFilter(parameters.getFirst("filters")));
		request.setUiSort(buildSort(getOrmColumn(ormMapping, getSortColumn(parameters)), getSortDirection(parameters)));
		request.setPage(ObjectUtils.defaultIfNull(getPage(parameters), 1));
		request.setPageSize(getPageLength(parameters));
		return request;
	}

	/**
	 * Return the sort object.
	 *
	 * @param sortColumn
	 *            the optional ordered column.
	 * @param sorDirection
	 *            the optional sort order. Default is {@link Direction#ASC}.
	 * @return <code>null</code> or sort object.
	 */
	private UiSort buildSort(final String sortColumn, final String sorDirection) {
		if (sortColumn == null) {
			return null;
		}
		final var sort = new UiSort();
		sort.setColumn(sortColumn);
		sort.setDirection(Optional.ofNullable(sorDirection).map(d -> Direction.valueOf(d.toUpperCase(Locale.ENGLISH)))
				.orElse(Direction.ASC));
		return sort;
	}

	/**
	 * Get DataTable pagination information from query parameters
	 *
	 * @param uriInfo
	 *            query parameters.
	 * @param ormMapping
	 *            Optional JSon to ORM property mapping.
	 * @return a {@link PageRequest} instance containing sort and page sizes.
	 */
	public PageRequest getPageRequest(final UriInfo uriInfo, final Map<String, String> ormMapping) {
		return getPageRequest(uriInfo, ormMapping, null);
	}

	/**
	 * Get DataTable pagination information from query parameters.
	 *
	 * @param uriInfo
	 *            Query parameters.
	 * @param ormMapping
	 *            Optional JSon to ORM property mapping.
	 * @param caseSensitiveColumns
	 *            Optional JSon columns name where the case sensitive ordering is requested. The "lower" function will
	 *            not be used for the "ORDER BY" in this case.
	 * @return a {@link PageRequest} instance containing sort and page sizes.
	 */
	public PageRequest getPageRequest(final UriInfo uriInfo, final Map<String, String> ormMapping,
			final Collection<String> caseSensitiveColumns) {
		// Update pagination information
		if (uriInfo == null) {
			return PageRequest.of(0, 10);
		}
		final var parameters = uriInfo.getQueryParameters();
		final var pageLength = getPageLength(parameters);
		final int firstPage = Optional.ofNullable(getPage(parameters)).map(p -> p - 1)
				.orElse(getStart(parameters) / pageLength);

		// Update sort information
		return buildOrderedPageRequest(ormMapping, parameters, pageLength, firstPage, caseSensitiveColumns);
	}

	/**
	 * Return the zero index page.
	 *
	 * @param parameters
	 *            The available query parameters.
	 * @return The page index or default length 10.
	 */
	public int getPageLength(final MultivaluedMap<String, String> parameters) {
		return Optional.ofNullable(parameters.getFirst("rows")).map(Integer::parseInt)
				.orElse(Optional.ofNullable(parameters.getFirst(DataTableAttributes.PAGE_LENGTH)).map(Integer::parseInt)
						.orElse(DEFAULT_PAGE_SIZE));
	}

	/**
	 * Return the 1 based index page.
	 *
	 * @param parameters
	 *            The available query parameters.
	 * @return The page index or <code>null</code>. Starts from 1.
	 */
	public Integer getPage(final MultivaluedMap<String, String> parameters) {
		return Optional.ofNullable(parameters.getFirst("page")).map(Integer::parseInt).map(p -> Math.max(1, p))
				.orElse(null);
	}

	/**
	 * Return the zero based start.
	 *
	 * @param parameters
	 *            The available query parameters.
	 * @return The page index or<code>0</code>. Starts from 0.
	 */
	private int getStart(final MultivaluedMap<String, String> parameters) {
		return Optional.ofNullable(parameters.getFirst(DataTableAttributes.START)).map(Integer::parseInt)
				.map(s -> Math.max(0, s)).orElse(0);
	}

	/**
	 * Return the sorted column.
	 *
	 * @param parameters
	 *            The available query parameters.
	 * @return the sorted column or <code>null</code>.
	 */
	private String getSortColumn(final MultivaluedMap<String, String> parameters) {
		return Optional.ofNullable(parameters.getFirst("sidx"))
				.orElse(Optional.ofNullable(parameters.getFirst(DataTableAttributes.SORTED_COLUMN))
						.map(i -> parameters.getFirst(String.format(DataTableAttributes.DATA_PROP, i))).orElse(null));
	}

	/**
	 * Return the sorted direction.
	 *
	 * @param parameters
	 *            The available query parameters.
	 * @return the sorted column or <code>ASC</code>.
	 */
	private String getSortDirection(final MultivaluedMap<String, String> parameters) {
		return Optional.ofNullable(parameters.getFirst("sord"))
				.orElse(ObjectUtils.defaultIfNull(parameters.getFirst(DataTableAttributes.SORT_DIRECTION), "ASC"));
	}

	/**
	 * Build the {@link PageRequest} with ordering information.
	 */
	private PageRequest buildOrderedPageRequest(final Map<String, String> ormMapping,
			final MultivaluedMap<String, String> parameters, final int pageLength, final int firstPage,
			final Collection<String> caseSensitiveColumns) {
		return Optional.ofNullable(getSortColumn(parameters))
				.map(c -> newSortedPageRequest(ormMapping, parameters, pageLength, firstPage, c, caseSensitiveColumns))
				.orElse(PageRequest.of(firstPage, pageLength));
	}

	/**
	 * Build and return a new {@link PageRequest} from the page information and ordering.
	 *
	 * @param ormMapping
	 *            The mapping from JSon name to ORM property or function.
	 * @param parameters
	 *            The available query parameters.
	 * @param pageLength
	 *            The resolved page length.
	 * @param firstPage
	 *            The page index.
	 * @param column
	 *            The sorted column name.
	 * @param caseSensitiveColumns
	 *            Optional JSon columns name where the case sensitive ordering is requested. The "lower" function will
	 *            not be used for the "ORDER BY" in this case.
	 * @return The new {@link PageRequest} with pagination and order.
	 */
	@NotNull
	private PageRequest newSortedPageRequest(final Map<String, String> ormMapping,
			final MultivaluedMap<String, String> parameters, final int pageLength, final int firstPage,
			final String column, final Collection<String> caseSensitiveColumns) {
		final var direction = getSortDirection(parameters);
		final PageRequest pageRequest;
		final var ormProperty = getOrmColumn(ormMapping, column);
		if (ormProperty == null) {
			// Not enough information for build an ORDER BY
			pageRequest = PageRequest.of(firstPage, pageLength);
		} else {
			// Ordering query can be built
			final Sort sort;
			if ((caseSensitiveColumns == null || !caseSensitiveColumns.contains(column))
					&& ormProperty.indexOf('(') == -1) {
				if (ormProperty.indexOf('.') == -1) {
					sort = Sort.by(new Sort.Order(Direction.valueOf(direction.toUpperCase(Locale.ENGLISH)), ormProperty)
							.ignoreCase());
				} else {
					sort = JpaSort.unsafe(Direction.valueOf(direction.toUpperCase(Locale.ENGLISH)),
							"UPPER(" + ormProperty + ")");
				}
			} else {
				sort = JpaSort.unsafe(Direction.valueOf(direction.toUpperCase(Locale.ENGLISH)), ormProperty);
			}
			pageRequest = PageRequest.of(firstPage, pageLength, sort);
		}
		return pageRequest;
	}

	private String getOrmColumn(final Map<String, String> ormMapping, final String key) {
		return Optional.ofNullable(ormMapping).map(m -> m.getOrDefault(key, m.containsKey("*") ? key : null))
				.orElse(null);
	}

	/**
	 * Apply pagination for DataTable component.
	 *
	 * @param <T>
	 *            Entity type.
	 * @param <E>
	 *            Business Object type.
	 * @param uriInfo
	 *            query parameters.
	 * @param items
	 *            page items.
	 * @param converter
	 *            E (entity) to T (table item type)
	 * @return a paginated table instance containing items and page sizes.
	 */
	public <T, E> TableItem<T> applyPagination(final UriInfo uriInfo, final Page<E> items,
			final Function<E, T> converter) {
		// update JSon component with pagination information
		final var result = new TableItem<T>();

		// Force the content to be fetched
		result.setData(items.getContent().stream().map(converter).collect(Collectors.toList()));

		// Need to be updated when the filters are supported.
		result.setRecordsFiltered(items.getTotalElements());
		result.setRecordsTotal(items.getTotalElements());

		// Token value
		result.setDraw(uriInfo == null ? null : uriInfo.getQueryParameters().getFirst(DataTableAttributes.ECHO));
		return result;
	}

	/**
	 * Return a filter structure from the JSon string.
	 *
	 * @param jsonString
	 *            the JSon string generated by JQGrid component.
	 * @return a filter structure.
	 */
	public UiFilter buildFilter(final String jsonString) {

		if (jsonString != null) {
			final ObjectMapper mapper = new org.ligoj.bootstrap.core.json.ObjectMapperTrim();
			try {
				return mapper.readValue(jsonString, UiFilter.class);
			} catch (final IOException e) {
				// Ignore invalid UI filter, considered as no UI filter
				log.error(String.format("Unable to parse JSon data :%s", jsonString), e);
			}
		}

		return new UiFilter();
	}
}
