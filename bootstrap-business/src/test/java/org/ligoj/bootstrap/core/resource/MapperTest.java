/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * {@link AbstractMapper} test.
 */
class MapperTest extends AbstractMapper {

	/**
	 * Simulate a serialization issue of thrown exception.
	 */
	@Test
	void toResponseSerializationException() {
		final AbstractMapper mapper = new AbstractMapper() {

			@Override
			public Response toResponse(final Response.StatusType status, final Object object) {
				return super.toResponse(Status.FORBIDDEN, new NonSerializableObject());
			}
		};
		mapper.jacksonJsonProvider = new JacksonJsonProvider();
		Assertions.assertEquals("Unable to build a JSON string from a server error",
				Assertions.assertThrows(TechnicalException.class, () -> mapper.toResponse(null, null)).getMessage());
	}

	@Test
	void toResponse() {
		jacksonJsonProvider = new JacksonJsonProvider();
		super.toResponse(Status.FORBIDDEN, null, new NullPointerException());
	}

	@Test
	void toResponseNoException() {
		jacksonJsonProvider = new JacksonJsonProvider();
		Assertions.assertEquals("{\"code\":null,\"message\":null,\"parameters\":null,\"cause\":null}",
				toResponse(Status.FORBIDDEN, null, null).getEntity());
	}

	@Test
	void toResponseParameteredException() {
		jacksonJsonProvider = new JacksonJsonProvider();
		Assertions.assertEquals("{\"code\":null,\"message\":\"message\",\"parameters\":[\"p1\",\"p2\"],\"cause\":null}",
				toResponse(Status.FORBIDDEN, null, new BusinessException("message", "p1", "p2")).getEntity());
	}

	@Test
	void toResponseParameteredExceptionNoParameter() {
		jacksonJsonProvider = new JacksonJsonProvider();
		Assertions.assertEquals("{\"code\":null,\"message\":\"message\",\"parameters\":null,\"cause\":null}",
				toResponse(Status.FORBIDDEN, null, new BusinessException("message")).getEntity());
	}

	private static class NonSerializableObject {
		private String dummy;

		@SuppressWarnings("unused")
		String getDummy() throws IOException {
			throw new IOException(dummy);
		}

	}

}
