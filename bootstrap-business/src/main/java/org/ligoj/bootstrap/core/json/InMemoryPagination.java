/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.ligoj.bootstrap.core.json.datatable.DataTableAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * In memory pagination.
 */
@Component
public class InMemoryPagination {

	@Autowired
	protected PaginationJson paginationJson;

	/**
	 * Create a page from the pagination and the total result.
	 *
	 * @param result
	 *            The raw collection item to paginate.
	 * @param pageable
	 *            The page request.
	 * @param <T>
	 *            The collection item type.
	 * @return The selected page.
	 */
	public <T> Page<T> newPage(final Collection<T> result, final Pageable pageable) {
		// Check bounds
		final var start = Math.clamp((long)pageable.getPageNumber() * pageable.getPageSize(), 0, result.size());
		final var end = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), result.size());
        var index = 0;
		final List<T> page = new ArrayList<>(end - start);
		final var iterator = result.iterator();

		// Skip the previous page
		while (index < start) {
			index++;
			iterator.next();
		}

		// Add the page
		while (index < end) {
			page.add(iterator.next());
			index++;
		}
		return new PageImpl<>(page, pageable, result.size());
	}

	/**
	 * Return a {@link TableItem} instance containing the given items and the pagination information.
	 *
	 * @param uriInfo
	 *            filter data.
	 * @param items
	 *            the items to paginate in memory.
	 * @param transformer
	 *            the paginated item transformer.
	 * @return the paginated items with pagination information.
	 * @param <T>
	 *            The collection target item type.
	 * @param <E>
	 *            The collection source item type.
	 */
	public <E, T> TableItem<T> applyPagination(final UriInfo uriInfo, final Collection<E> items,
			final Function<E, T> transformer) {
		final var uiPageRequest = paginationJson.getUiPageRequest(uriInfo);
		final var pageRequest = PageRequest.of(uiPageRequest.getPage() - 1, uiPageRequest.getPageSize());
		return paginationJson.applyPagination(uriInfo, newPage(items, pageRequest), transformer);
	}

	/**
	 * Return a filtered sublist where item match the criteria (query parameter "q")
	 *
	 * @param uriInfo
	 *            filter data.
	 * @param items
	 *            the items as {@link Stream} to filter and paginate in memory.
	 * @return the filtered items with pagination information.
	 */
	public TableItem<String> getFilteredStringList(final UriInfo uriInfo, final Stream<String> items) {
		return applyPagination(uriInfo,
				items.filter(input -> StringUtils.containsIgnoreCase(input, DataTableAttributes.getSearch(uriInfo)))
						.toList(),
				Function.identity());
	}

	/**
	 * Return a filtered sublist where item match the criteria (query parameter "q")
	 *
	 * @param uriInfo
	 *            filter data.
	 * @param items
	 *            the items to filter and paginate in memory.
	 * @return the filtered items with pagination information.
	 */
	public TableItem<String> getFilteredStringList(final UriInfo uriInfo, final Collection<String> items) {
		return getFilteredStringList(uriInfo, items.stream());
	}

}
