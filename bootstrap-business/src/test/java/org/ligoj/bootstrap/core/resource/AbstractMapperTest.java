package org.ligoj.bootstrap.core.resource;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.ligoj.bootstrap.core.json.ObjectMapper;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * Base test class of {@link AbstractMapper}
 */
public abstract class AbstractMapperTest {

	protected <T extends AbstractMapper> T mock(T mapper) {
		mapper.jacksonJsonProvider = Mockito.mock(JacksonJsonProvider.class);
		Mockito.when(mapper.jacksonJsonProvider.locateMapper(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new ObjectMapper());
		return mapper;
	}

	protected void check(Response response, int status, String content) {
		Assert.assertEquals(status, response.getStatus());
		Assert.assertEquals(content, response.getEntity().toString());
	}
}
