package org.ligoj.bootstrap.core.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.json.datatable.DataTableAttributes;
import org.ligoj.bootstrap.core.json.jqgrid.BasicRule;
import org.ligoj.bootstrap.core.json.jqgrid.BasicRule.RuleOperator;
import org.ligoj.bootstrap.core.json.jqgrid.UiFilter;
import org.ligoj.bootstrap.core.json.jqgrid.UiFilter.FilterOperator;
import org.ligoj.bootstrap.core.json.jqgrid.UiPageRequest;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * JSon pagination test of {@link PaginationJson}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class PaginationJsonTest extends AbstractBootTest {

	@Autowired
	private PaginationJson paginationJson;

	/**
	 * No page display provided, mean all data
	 */
	@Test
	public void getPageRequestNoPageSize() {
		// create a mock URI info with pagination informations
		PageRequest pageRequest = paginationJson.getPageRequest(newUriInfo(), null);
		Assert.assertNotNull(pageRequest);
		Assert.assertFalse(pageRequest.getSort().isSorted());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertEquals(10, pageRequest.getPageSize());
	}

	/**
	 * Simple pagination with starting offset.
	 */
	@Test
	public void getPageRequestNoSortedColumn() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, null);
		Assert.assertNotNull(pageRequest);
		Assert.assertFalse(pageRequest.getSort().isSorted());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Simple pagination with starting page.
	 */
	@Test
	public void getPageRequestDisplayStart() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.START, "220");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, null);
		Assert.assertNotNull(pageRequest);
		Assert.assertFalse(pageRequest.getSort().isSorted());
		Assert.assertEquals(200, pageRequest.getOffset());
		Assert.assertEquals(2, pageRequest.getPageNumber());
	}

	/**
	 * Simple pagination with starting page.
	 */
	@Test
	public void getPageRequestPage() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add("page", "2");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, null);
		Assert.assertNotNull(pageRequest);
		Assert.assertFalse(pageRequest.getSort().isSorted());
		Assert.assertEquals(100, pageRequest.getOffset());
		Assert.assertEquals(1, pageRequest.getPageNumber());
	}

	/**
	 * UI filter built from complete valid input.
	 */
	@Test
	public void buildFilter() {
		final UiFilter buildFilter = paginationJson
				.buildFilter("{\"groupOp\":\"and\"," + "\"rules\":[{\"data\":\"data\",\"field\":\"field\",\"op\":\"eq\"}]}");
		Assert.assertEquals(FilterOperator.AND, buildFilter.getGroupOp());
		Assert.assertNotNull(buildFilter.getRules());
		Assert.assertEquals(1, buildFilter.getRules().size());
		final BasicRule rule = (BasicRule) buildFilter.getRules().get(0);
		Assert.assertEquals("data", rule.getData());
		Assert.assertEquals("field", rule.getField());
		Assert.assertEquals(RuleOperator.EQ, rule.getOp());

		// For coverage
		RuleOperator.valueOf(RuleOperator.values()[0].name());
		FilterOperator.valueOf(FilterOperator.values()[0].name());
	}

	/**
	 * UI filter built from an invalid input.
	 */
	@Test
	public void buildFilterInvalid() {
		final UiFilter buildFilter = paginationJson.buildFilter("{\"source\":\"source\",\"groupOp\":\"?\"}");
		Assert.assertEquals(null, buildFilter.getGroupOp());
		Assert.assertEquals(null, buildFilter.getRules());
	}

	/**
	 * UI filter built from null entry.
	 */
	@Test
	public void buildFilterNull() {
		final UiFilter buildFilter = paginationJson.buildFilter(null);
		Assert.assertEquals(null, buildFilter.getGroupOp());
		Assert.assertEquals(null, buildFilter.getRules());
	}

	/**
	 * No sorted direction.
	 */
	@Test
	public void getPageRequestSortedDirection() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, null);
		Assert.assertNotNull(pageRequest);
		Assert.assertFalse(pageRequest.getSort().isSorted());
		Assert.assertEquals(0, pageRequest.getOffset());
	}

	/**
	 * Sorted columns with direction.
	 */
	@Test
	public void getPageRequestSortedColumn() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORT_DIRECTION, "asc");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, null);
		Assert.assertNotNull(pageRequest);
		Assert.assertFalse(pageRequest.getSort().isSorted());
		Assert.assertEquals(0, pageRequest.getOffset());
	}

	/**
	 * Sorted direction with ordering but no mapping provided.
	 */
	@Test
	public void getPageRequestNoMappingOrder() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		uriInfo.getQueryParameters().add("columns[2][data]", "col1");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORT_DIRECTION, "asc");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, null);
		Assert.assertNotNull(pageRequest);
		Assert.assertFalse(pageRequest.getSort().isSorted());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Sorted direction with ordering but no corresponding ORM column.
	 */
	@Test
	public void getPageRequestNoMappedOrder() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		uriInfo.getQueryParameters().add("columns[2][data]", "col1");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORT_DIRECTION, "desc");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, new HashMap<String, String>());
		Assert.assertNotNull(pageRequest);
		Assert.assertFalse(pageRequest.getSort().isSorted());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Sorted direction with ordering and corresponding ORM column.
	 */
	@Test
	public void getPageRequestFullOrdering() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		uriInfo.getQueryParameters().add("columns[2][data]", "col1");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORT_DIRECTION, "asc");
		final Map<String, String> map = Collections.singletonMap("col1", "colOrm");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, map);
		Assert.assertNotNull(pageRequest);
		Assert.assertNotNull(pageRequest.getSort());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertNull(pageRequest.getSort().getOrderFor("colOrm?"));
		Assert.assertNotNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assert.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("colOrm").getDirection());
		Assert.assertTrue(pageRequest.getSort().getOrderFor("colOrm").isIgnoreCase());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Sorted direction with ordering and corresponding ORM column.
	 */
	@Test
	public void getPageRequestFullOrderingMixProvider() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add("sidx", "col1");
		uriInfo.getQueryParameters().add("sortd", "asc");
		final Map<String, String> map = Collections.singletonMap("col1", "colOrm");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, map);
		Assert.assertNotNull(pageRequest);
		Assert.assertNotNull(pageRequest.getSort());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertNull(pageRequest.getSort().getOrderFor("colOrm?"));
		Assert.assertNotNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assert.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("colOrm").getDirection());
		Assert.assertTrue(pageRequest.getSort().getOrderFor("colOrm").isIgnoreCase());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Sorted direction with ordering and corresponding ORM column.
	 */
	@Test
	public void getPageRequestIdentityMapping() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add("sidx", "col1");
		uriInfo.getQueryParameters().add("sortd", "asc");
		final Map<String, String> map = Collections.singletonMap("*", "*");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, map);
		Assert.assertNotNull(pageRequest);
		Assert.assertNotNull(pageRequest.getSort());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertNull(pageRequest.getSort().getOrderFor("col1?"));
		Assert.assertNotNull(pageRequest.getSort().getOrderFor("col1"));
		Assert.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("col1").getDirection());
		Assert.assertTrue(pageRequest.getSort().getOrderFor("col1").isIgnoreCase());
		Assert.assertEquals(10, pageRequest.getPageSize());
	}

	/**
	 * Sorted direction with ordering and corresponding ORM column.
	 */
	@Test
	public void getPageRequestFullOrderingAlias() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		uriInfo.getQueryParameters().add("columns[2][data]", "col1");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORT_DIRECTION, "asc");
		final Map<String, String> map = Collections.singletonMap("col1", "c.colOrm");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, map);
		Assert.assertNotNull(pageRequest);
		Assert.assertNotNull(pageRequest.getSort());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertNull(pageRequest.getSort().getOrderFor("c.colOrm?"));
		Assert.assertNotNull(pageRequest.getSort().getOrderFor("UPPER(c.colOrm)"));
		Assert.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("UPPER(c.colOrm)").getDirection());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Sorted columns with direction.
	 */
	@Test
	public void getPageRequestSortedColumnWithFunction() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		uriInfo.getQueryParameters().add("columns[2][data]", "col1");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORT_DIRECTION, "asc");
		final Map<String, String> map = Collections.singletonMap("col1", "COUNT(colOrm)");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, map);
		Assert.assertNotNull(pageRequest);
		Assert.assertNotNull(pageRequest.getSort());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assert.assertNotNull(pageRequest.getSort().getOrderFor("COUNT(colOrm)"));
		Assert.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("COUNT(colOrm)").getDirection());
		Assert.assertFalse(pageRequest.getSort().getOrderFor("COUNT(colOrm)").isIgnoreCase());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Sorted columns with direction.
	 */
	@Test
	public void getPageRequestCaseSensitiveOder() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		uriInfo.getQueryParameters().add("columns[2][data]", "col1");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORT_DIRECTION, "asc");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, Collections.singletonMap("col1", "colOrm"),
				Collections.singleton("col1"));
		Assert.assertNotNull(pageRequest);
		Assert.assertNotNull(pageRequest.getSort());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertNotNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assert.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("colOrm").getDirection());
		Assert.assertFalse(pageRequest.getSort().getOrderFor("colOrm").isIgnoreCase());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Undefined {@link UriInfo}
	 */
	@Test
	public void getPageRequestNotUriInfo() {
		final PageRequest pageRequest = paginationJson.getPageRequest(null, null);
		Assert.assertNotNull(pageRequest);
		Assert.assertFalse(pageRequest.getSort().isSorted());
		Assert.assertEquals(10, pageRequest.getPageSize());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertEquals(0, pageRequest.getOffset());
	}

	/**
	 * Sorted columns with direction.
	 */
	@Test
	public void getPageRequestCaseInsensitiveOder() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		uriInfo.getQueryParameters().add("columns[2][data]", "col1");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORT_DIRECTION, "asc");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, Collections.singletonMap("col1", "colOrm"),
				Collections.singleton("any"));
		Assert.assertNotNull(pageRequest);
		Assert.assertNotNull(pageRequest.getSort());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertNotNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assert.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("colOrm").getDirection());
		Assert.assertTrue(pageRequest.getSort().getOrderFor("colOrm").isIgnoreCase());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Sorted columns with direction.
	 */
	@Test
	public void getPageRequestCaseInsensitiveOderAlias() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add(DataTableAttributes.PAGE_LENGTH, "100");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORTED_COLUMN, "2");
		uriInfo.getQueryParameters().add("columns[2][data]", "col1");
		uriInfo.getQueryParameters().add(DataTableAttributes.SORT_DIRECTION, "asc");
		final PageRequest pageRequest = paginationJson.getPageRequest(uriInfo, Collections.singletonMap("col1", "c.colOrm"),
				Collections.singleton("any"));
		Assert.assertNotNull(pageRequest);
		Assert.assertNotNull(pageRequest.getSort());
		Assert.assertEquals(0, pageRequest.getOffset());
		Assert.assertEquals(0, pageRequest.getPageNumber());
		Assert.assertNotNull(pageRequest.getSort().getOrderFor("UPPER(c.colOrm)"));
		Assert.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("UPPER(c.colOrm)").getDirection());
		Assert.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Simple page request with default values.
	 */
	@Test
	public void getUiPageRequest() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		final UiPageRequest pageRequest = paginationJson.getUiPageRequest(uriInfo);
		Assert.assertNotNull(pageRequest);
		Assert.assertEquals(1, pageRequest.getPage());
		Assert.assertEquals(10, pageRequest.getPageSize());
		Assert.assertNotNull(pageRequest.getUiFilter());
		Assert.assertNull(pageRequest.getUiFilter().getGroupOp());
		Assert.assertNull(pageRequest.getUiFilter().getRules());
		Assert.assertNull(pageRequest.getUiSort());
	}

	/**
	 * Simple page request with default values but sorted column.
	 */
	@Test
	public void getUiPageRequestSimpleSortDesc() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add("page", "1");
		uriInfo.getQueryParameters().add("rows", String.valueOf(PaginationJson.DEFAULT_PAGE_SIZE));
		uriInfo.getQueryParameters().add("sord", "desc");
		uriInfo.getQueryParameters().add("sidx", "colX");
		final UiPageRequest pageRequest = paginationJson.getUiPageRequest(uriInfo);
		Assert.assertNotNull(pageRequest);
		Assert.assertEquals(1, pageRequest.getPage());
		Assert.assertEquals(10, pageRequest.getPageSize());
		Assert.assertNotNull(pageRequest.getUiFilter());
		Assert.assertNull(pageRequest.getUiFilter().getGroupOp());
		Assert.assertNull(pageRequest.getUiFilter().getRules());
		Assert.assertNotNull(pageRequest.getUiSort());
		Assert.assertEquals("colX", pageRequest.getUiSort().getColumn());
		Assert.assertEquals(Direction.DESC, pageRequest.getUiSort().getDirection());
	}

	/**
	 * Simple page request with default values but sorted column.
	 */
	@Test
	public void getUiPageRequestSimpleSort() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().add("sidx", "colX");
		final UiPageRequest pageRequest = paginationJson.getUiPageRequest(uriInfo);
		Assert.assertNotNull(pageRequest);
		Assert.assertEquals(1, pageRequest.getPage());
		Assert.assertEquals(10, pageRequest.getPageSize());
		Assert.assertNotNull(pageRequest.getUiFilter());
		Assert.assertNull(pageRequest.getUiFilter().getGroupOp());
		Assert.assertNull(pageRequest.getUiFilter().getRules());
		Assert.assertNotNull(pageRequest.getUiSort());
		Assert.assertEquals("colX", pageRequest.getUiSort().getColumn());
		Assert.assertEquals(Direction.ASC, pageRequest.getUiSort().getDirection());
	}

	/**
	 * Pagination test without lazy mode.
	 */
	@Test
	public void applyPaginationNotLazy() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		final Page<SystemUser> page = Mockito.mock(Page.class);
		final List<SystemUser> list = new ArrayList<>();
		uriInfo.getQueryParameters().putSingle(DataTableAttributes.ECHO, "echo");
		list.add(new SystemUser());
		Mockito.when(page.getContent()).thenReturn(list);
		Mockito.when(page.getTotalElements()).thenReturn(1L);
		final TableItem<SystemUser> pageRequest = paginationJson.applyPagination(uriInfo, page, Function.identity());

		Assert.assertNotNull(pageRequest);
		Assert.assertEquals(1, pageRequest.getData().size());
		Assert.assertEquals(1, pageRequest.getRecordsTotal());
		Assert.assertEquals(1, pageRequest.getRecordsFiltered());
		Assert.assertEquals("echo", pageRequest.getDraw());
		Assert.assertTrue(pageRequest.getData() instanceof ArrayList<?>);
	}

	/**
	 * Pagination test without lazy mode.
	 */
	@Test
	public void applyPaginationNullUriInfo() {
		// create a mock URI info with pagination informations
		final Page<SystemUser> page = Mockito.mock(Page.class);
		final List<SystemUser> list = new ArrayList<>();
		list.add(new SystemUser());
		Mockito.when(page.getContent()).thenReturn(list);
		final TableItem<SystemUser> pageRequest = paginationJson.applyPagination(null, page, Function.identity());

		Assert.assertNotNull(pageRequest);
		Assert.assertEquals(1, pageRequest.getData().size());
		Assert.assertNull(pageRequest.getDraw());
		Assert.assertTrue(pageRequest.getData() instanceof ArrayList<?>);
	}
}
