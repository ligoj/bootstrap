package org.ligoj.bootstrap.core.resource;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * {@link AbstractMapper} test.
 */
public class MapperTest {

	/**
	 * Simulate a serialization issue of thrown exception.
	 */
	@Test(expected = TechnicalException.class)
	public void testSerializationException() {
		final AbstractMapper mapper = new AbstractMapper() {

			@Override
			public Response toResponse(final Response.StatusType status, final Object object) {
				return super.toResponse(Status.FORBIDDEN, new NonSerializableObject());
			}

		};
		mapper.jacksonJsonProvider = new JacksonJsonProvider();
		mapper.toResponse(null, null);
	}

	private static class NonSerializableObject {
		private String dummy;

		@SuppressWarnings("unused")
		public String getDummy() throws IOException {
			throw new IOException(dummy);
		}

	}

}
