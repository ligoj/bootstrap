package org.ligoj.bootstrap.core.json;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.json.datatable.DataTableAttributes;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * JSon pagination test of {@link InMemoryPagination}.
 */
@ExtendWith(SpringExtension.class)
public class InMemoryPaginationTest extends AbstractBootTest {

	private InMemoryPagination inMemoryPagination;

	@BeforeEach
	public void prepareObject() {
		inMemoryPagination = new InMemoryPagination();
		inMemoryPagination.paginationJson = new PaginationJson();
	}

	private List<String> newItems(final int count) {
		final List<String> result = new ArrayList<>();
		for (int i = count; i-- > 0;) {
			result.add("item" + i);
		}
		return result;
	}

	@Test
	public void testSimple() {
		final TableItem<String> items = inMemoryPagination.getFilteredStringList(newUriInfo(), newItems(15));
		Assertions.assertEquals(10, items.getData().size());
	}

	@Test
	public void testFilterGlobalSearch() {
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.SEARCH, "Tem5");
		final TableItem<String> items = inMemoryPagination.getFilteredStringList(uriInfo, newItems(15));
		Assertions.assertEquals(1, items.getData().size());
	}

	@Test
	public void testFilterSelect2Search() {
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add("q", "Tem5");
		final TableItem<String> items = inMemoryPagination.getFilteredStringList(uriInfo, newItems(15));
		Assertions.assertEquals(1, items.getData().size());
	}

	@Test
	public void testFilterSelect2SearchOffset() {
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add("q", "iTem");
		uriInfo.getQueryParameters().add("page", "2");
		uriInfo.getQueryParameters().add("rows", "11");
		final TableItem<String> items = inMemoryPagination.getFilteredStringList(uriInfo, newItems(15));

		Assertions.assertEquals(4, items.getData().size());
		Assertions.assertEquals(15, items.getRecordsTotal());
		Assertions.assertTrue(items.getData().contains("item0"));
		Assertions.assertTrue(items.getData().contains("item1"));
		Assertions.assertTrue(items.getData().contains("item2"));
		Assertions.assertTrue(items.getData().contains("item3"));
	}
}
