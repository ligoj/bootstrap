/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * Base test class of {@link AbstractMapper}
 */
public abstract class AbstractMapperTest {

	protected <T extends AbstractMapper> T mock(T mapper) {
		mapper.jacksonJsonProvider = Mockito.mock(JacksonJsonProvider.class);
		Mockito.when(mapper.jacksonJsonProvider.locateMapper(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new ObjectMapperTrim());
		return mapper;
	}

	protected void check(Response response, int status, String content) {
		Assertions.assertEquals(status, response.getStatus());
		Assertions.assertEquals(content, response.getEntity().toString());
	}
}
