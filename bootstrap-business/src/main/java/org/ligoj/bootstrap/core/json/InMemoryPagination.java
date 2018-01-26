package org.ligoj.bootstrap.core.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.ligoj.bootstrap.core.json.datatable.DataTableAttributes;
import org.ligoj.bootstrap.core.json.jqgrid.UiPageRequest;
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
	 * Create a page from the pagination and the total result
	 */
	public <T> Page<T> newPage(final Collection<T> result, final Pageable pageable) {
		// Check bounds
		final int start = Math.min(Math.max(pageable.getPageNumber() * pageable.getPageSize(), 0), result.size());
		final int end = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), result.size());
		int index = 0;
		final List<T> page = new ArrayList<>(end - start);
		final Iterator<T> iterator = result.iterator();

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
	 * @param uriInfo
	 *            filter data.
	 * @param items
	 *            the items to paginate in memory.
	 * @param transformer
	 *            the paginated item transformer.
	 * @return the paginated items with pagination information.
	 */
	public <E, T> TableItem<T> applyPagination(final UriInfo uriInfo, final Collection<E> items, final Function<E, T> transformer) {
		final UiPageRequest uiPageRequest = paginationJson.getUiPageRequest(uriInfo);
		final PageRequest pageRequest = PageRequest.of(uiPageRequest.getPage() - 1, uiPageRequest.getPageSize());
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
						.collect(Collectors.toList()),
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
