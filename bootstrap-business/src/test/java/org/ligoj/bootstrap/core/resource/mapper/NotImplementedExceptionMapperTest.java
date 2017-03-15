package org.ligoj.bootstrap.core.resource.mapper;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

/**
 * Exception mapper test using {@link WebApplicationExceptionMapper}
 */
public class NotImplementedExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final NotImplementedException exception = new NotImplementedException("message-error");
		check(mock(new NotImplementedExceptionMapper()).toResponse(exception), 501,
				"{\"code\":\"not-implemented\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}
}
