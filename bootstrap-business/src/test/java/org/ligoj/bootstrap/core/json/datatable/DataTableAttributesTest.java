/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json.datatable;

import jakarta.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link DataTableAttributes}
 */
class DataTableAttributesTest {

	@Test
	void getSearchSelect2() {
		final var uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MetadataMap<>());
		uriInfo.getQueryParameters().add("q", "  S1 ");
		Assertions.assertEquals("S1", DataTableAttributes.getSearch(uriInfo));
	}

	@Test
	void getSearchDataTable() {
		final var uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MetadataMap<>());
		uriInfo.getQueryParameters().add(DataTableAttributes.SEARCH, "  S1 ");
		Assertions.assertEquals("S1", DataTableAttributes.getSearch(uriInfo));
	}

	@Test
	void getSearchDataTableToNull() {
		final var uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MetadataMap<>());
		uriInfo.getQueryParameters().add(DataTableAttributes.SEARCH, "   ");
		Assertions.assertEquals(0, DataTableAttributes.getSearch(uriInfo).length());
	}
}
