package org.ligoj.bootstrap.core.json.datatable;

import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link DataTableAttributes}
 */
public class DataTableAttributesTest {

	@Test
	public void getSearchSelect2() {
		final UriInfo uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MetadataMap<String, String>());
		uriInfo.getQueryParameters().add("q", "  S1 ");
		Assertions.assertEquals("S1", DataTableAttributes.getSearch(uriInfo));
	}

	@Test
	public void getSearchDataTable() {
		final UriInfo uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MetadataMap<String, String>());
		uriInfo.getQueryParameters().add(DataTableAttributes.SEARCH, "  S1 ");
		Assertions.assertEquals("S1", DataTableAttributes.getSearch(uriInfo));
	}

	@Test
	public void getSearchDataTableToNull() {
		final UriInfo uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MetadataMap<String, String>());
		uriInfo.getQueryParameters().add(DataTableAttributes.SEARCH, "   ");
		Assertions.assertTrue(DataTableAttributes.getSearch(uriInfo).length() == 0);
	}
}
