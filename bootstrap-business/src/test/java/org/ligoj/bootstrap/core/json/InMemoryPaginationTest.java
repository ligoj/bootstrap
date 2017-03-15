package org.ligoj.bootstrap.core.json;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.ligoj.bootstrap.AbstractDataGeneratorTest;
import org.ligoj.bootstrap.core.json.datatable.DataTableAttributes;

/**
 * JSon pagination test of {@link InMemoryPagination}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public class InMemoryPaginationTest extends AbstractDataGeneratorTest {

	private InMemoryPagination inMemoryPagination;

	@Before
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
		Assert.assertEquals(10, items.getData().size());
	}

	@Test
	public void testFilterGlobalSearch() {
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.SEARCH, "Tem5");
		final TableItem<String> items = inMemoryPagination.getFilteredStringList(uriInfo, newItems(15));
		Assert.assertEquals(1, items.getData().size());
	}

	@Test
	public void testFilterSelect2Search() {
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add("q", "Tem5");
		final TableItem<String> items = inMemoryPagination.getFilteredStringList(uriInfo, newItems(15));
		Assert.assertEquals(1, items.getData().size());
	}

	@Test
	public void testFilterSelect2SearchOffset() {
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add("q", "iTem");
		uriInfo.getQueryParameters().add("page", "2");
		uriInfo.getQueryParameters().add("rows", "11");
		final TableItem<String> items = inMemoryPagination.getFilteredStringList(uriInfo, newItems(15));

		Assert.assertEquals(4, items.getData().size());
		Assert.assertEquals(15, items.getRecordsTotal());
		Assert.assertTrue(items.getData().contains("item0"));
		Assert.assertTrue(items.getData().contains("item1"));
		Assert.assertTrue(items.getData().contains("item2"));
		Assert.assertTrue(items.getData().contains("item3"));
	}
}
