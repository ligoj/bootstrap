package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

/**
 * Exception mapper test using {@link WebApplicationExceptionMapper}
 */
public class WebApplicationExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final WebApplicationException exception = new WebApplicationException("message-error", 200);
		check(mock(new WebApplicationExceptionMapper()).toResponse(exception), 200, "{\"code\":\"internal\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}

	@Test
	public void toResponse404() {
		final WebApplicationException exception = new WebApplicationException("message-error", 404);
		check(mock(new WebApplicationExceptionMapper()).toResponse(exception), 404, "{\"code\":\"internal\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}

	@Test
	public void toResponse405() {
		final WebApplicationException exception = new WebApplicationException("message-error", 405);
		check(mock(new WebApplicationExceptionMapper()).toResponse(exception), 405, "{\"code\":\"internal\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}
}
