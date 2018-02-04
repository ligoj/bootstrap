package org.ligoj.bootstrap.core.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * JSon pagination test of {@link PaginationJson}.
 */
@ExtendWith(SpringExtension.class)
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertFalse(pageRequest.getSort().isSorted());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertEquals(10, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertFalse(pageRequest.getSort().isSorted());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertEquals(100, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertFalse(pageRequest.getSort().isSorted());
		Assertions.assertEquals(200, pageRequest.getOffset());
		Assertions.assertEquals(2, pageRequest.getPageNumber());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertFalse(pageRequest.getSort().isSorted());
		Assertions.assertEquals(100, pageRequest.getOffset());
		Assertions.assertEquals(1, pageRequest.getPageNumber());
	}

	/**
	 * UI filter built from complete valid input.
	 */
	@Test
	public void buildFilter() {
		final UiFilter buildFilter = paginationJson
				.buildFilter("{\"groupOp\":\"and\"," + "\"rules\":[{\"data\":\"data\",\"field\":\"field\",\"op\":\"eq\"}]}");
		Assertions.assertEquals(FilterOperator.AND, buildFilter.getGroupOp());
		Assertions.assertNotNull(buildFilter.getRules());
		Assertions.assertEquals(1, buildFilter.getRules().size());
		final BasicRule rule = (BasicRule) buildFilter.getRules().get(0);
		Assertions.assertEquals("data", rule.getData());
		Assertions.assertEquals("field", rule.getField());
		Assertions.assertEquals(RuleOperator.EQ, rule.getOp());

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
		Assertions.assertEquals(null, buildFilter.getGroupOp());
		Assertions.assertEquals(null, buildFilter.getRules());
	}

	/**
	 * UI filter built from null entry.
	 */
	@Test
	public void buildFilterNull() {
		final UiFilter buildFilter = paginationJson.buildFilter(null);
		Assertions.assertEquals(null, buildFilter.getGroupOp());
		Assertions.assertEquals(null, buildFilter.getRules());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertFalse(pageRequest.getSort().isSorted());
		Assertions.assertEquals(0, pageRequest.getOffset());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertFalse(pageRequest.getSort().isSorted());
		Assertions.assertEquals(0, pageRequest.getOffset());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertFalse(pageRequest.getSort().isSorted());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertEquals(100, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertFalse(pageRequest.getSort().isSorted());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertEquals(100, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertNotNull(pageRequest.getSort());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertNull(pageRequest.getSort().getOrderFor("colOrm?"));
		Assertions.assertNotNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assertions.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("colOrm").getDirection());
		Assertions.assertTrue(pageRequest.getSort().getOrderFor("colOrm").isIgnoreCase());
		Assertions.assertEquals(100, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertNotNull(pageRequest.getSort());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertNull(pageRequest.getSort().getOrderFor("colOrm?"));
		Assertions.assertNotNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assertions.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("colOrm").getDirection());
		Assertions.assertTrue(pageRequest.getSort().getOrderFor("colOrm").isIgnoreCase());
		Assertions.assertEquals(100, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertNotNull(pageRequest.getSort());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertNull(pageRequest.getSort().getOrderFor("col1?"));
		Assertions.assertNotNull(pageRequest.getSort().getOrderFor("col1"));
		Assertions.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("col1").getDirection());
		Assertions.assertTrue(pageRequest.getSort().getOrderFor("col1").isIgnoreCase());
		Assertions.assertEquals(10, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertNotNull(pageRequest.getSort());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertNull(pageRequest.getSort().getOrderFor("c.colOrm?"));
		Assertions.assertNotNull(pageRequest.getSort().getOrderFor("UPPER(c.colOrm)"));
		Assertions.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("UPPER(c.colOrm)").getDirection());
		Assertions.assertEquals(100, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertNotNull(pageRequest.getSort());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assertions.assertNotNull(pageRequest.getSort().getOrderFor("COUNT(colOrm)"));
		Assertions.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("COUNT(colOrm)").getDirection());
		Assertions.assertFalse(pageRequest.getSort().getOrderFor("COUNT(colOrm)").isIgnoreCase());
		Assertions.assertEquals(100, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertNotNull(pageRequest.getSort());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertNotNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assertions.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("colOrm").getDirection());
		Assertions.assertFalse(pageRequest.getSort().getOrderFor("colOrm").isIgnoreCase());
		Assertions.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Undefined {@link UriInfo}
	 */
	@Test
	public void getPageRequestNotUriInfo() {
		final PageRequest pageRequest = paginationJson.getPageRequest(null, null);
		Assertions.assertNotNull(pageRequest);
		Assertions.assertFalse(pageRequest.getSort().isSorted());
		Assertions.assertEquals(10, pageRequest.getPageSize());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertEquals(0, pageRequest.getOffset());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertNotNull(pageRequest.getSort());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertNotNull(pageRequest.getSort().getOrderFor("colOrm"));
		Assertions.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("colOrm").getDirection());
		Assertions.assertTrue(pageRequest.getSort().getOrderFor("colOrm").isIgnoreCase());
		Assertions.assertEquals(100, pageRequest.getPageSize());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertNotNull(pageRequest.getSort());
		Assertions.assertEquals(0, pageRequest.getOffset());
		Assertions.assertEquals(0, pageRequest.getPageNumber());
		Assertions.assertNotNull(pageRequest.getSort().getOrderFor("UPPER(c.colOrm)"));
		Assertions.assertEquals(Direction.ASC, pageRequest.getSort().getOrderFor("UPPER(c.colOrm)").getDirection());
		Assertions.assertEquals(100, pageRequest.getPageSize());
	}

	/**
	 * Simple page request with default values.
	 */
	@Test
	public void getUiPageRequest() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		final UiPageRequest pageRequest = paginationJson.getUiPageRequest(uriInfo);
		Assertions.assertNotNull(pageRequest);
		Assertions.assertEquals(1, pageRequest.getPage());
		Assertions.assertEquals(10, pageRequest.getPageSize());
		Assertions.assertNotNull(pageRequest.getUiFilter());
		Assertions.assertNull(pageRequest.getUiFilter().getGroupOp());
		Assertions.assertNull(pageRequest.getUiFilter().getRules());
		Assertions.assertNull(pageRequest.getUiSort());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertEquals(1, pageRequest.getPage());
		Assertions.assertEquals(10, pageRequest.getPageSize());
		Assertions.assertNotNull(pageRequest.getUiFilter());
		Assertions.assertNull(pageRequest.getUiFilter().getGroupOp());
		Assertions.assertNull(pageRequest.getUiFilter().getRules());
		Assertions.assertNotNull(pageRequest.getUiSort());
		Assertions.assertEquals("colX", pageRequest.getUiSort().getColumn());
		Assertions.assertEquals(Direction.DESC, pageRequest.getUiSort().getDirection());
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
		Assertions.assertNotNull(pageRequest);
		Assertions.assertEquals(1, pageRequest.getPage());
		Assertions.assertEquals(10, pageRequest.getPageSize());
		Assertions.assertNotNull(pageRequest.getUiFilter());
		Assertions.assertNull(pageRequest.getUiFilter().getGroupOp());
		Assertions.assertNull(pageRequest.getUiFilter().getRules());
		Assertions.assertNotNull(pageRequest.getUiSort());
		Assertions.assertEquals("colX", pageRequest.getUiSort().getColumn());
		Assertions.assertEquals(Direction.ASC, pageRequest.getUiSort().getDirection());
	}

	/**
	 * Pagination test without lazy mode.
	 */
	@Test
	public void applyPaginationNotLazy() {
		// create a mock URI info with pagination informations
		final UriInfo uriInfo = newUriInfo();
		@SuppressWarnings("unchecked")
		final Page<SystemUser> page = Mockito.mock(Page.class);
		final List<SystemUser> list = new ArrayList<>();
		uriInfo.getQueryParameters().putSingle(DataTableAttributes.ECHO, "echo");
		list.add(new SystemUser());
		Mockito.when(page.getContent()).thenReturn(list);
		Mockito.when(page.getTotalElements()).thenReturn(1L);
		final TableItem<SystemUser> pageRequest = paginationJson.applyPagination(uriInfo, page, Function.identity());

		Assertions.assertNotNull(pageRequest);
		Assertions.assertEquals(1, pageRequest.getData().size());
		Assertions.assertEquals(1, pageRequest.getRecordsTotal());
		Assertions.assertEquals(1, pageRequest.getRecordsFiltered());
		Assertions.assertEquals("echo", pageRequest.getDraw());
		Assertions.assertTrue(pageRequest.getData() instanceof ArrayList<?>);
	}

	/**
	 * Pagination test without lazy mode.
	 */
	@Test
	public void applyPaginationNullUriInfo() {
		// create a mock URI info with pagination informations
		@SuppressWarnings("unchecked")
		final Page<SystemUser> page = Mockito.mock(Page.class);
		final List<SystemUser> list = new ArrayList<>();
		list.add(new SystemUser());
		Mockito.when(page.getContent()).thenReturn(list);
		final TableItem<SystemUser> pageRequest = paginationJson.applyPagination(null, page, Function.identity());

		Assertions.assertNotNull(pageRequest);
		Assertions.assertEquals(1, pageRequest.getData().size());
		Assertions.assertNull(pageRequest.getDraw());
		Assertions.assertTrue(pageRequest.getData() instanceof ArrayList<?>);
	}
}
