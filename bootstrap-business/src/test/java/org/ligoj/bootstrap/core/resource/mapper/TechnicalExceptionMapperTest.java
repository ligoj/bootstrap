package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.ligoj.bootstrap.core.resource.TechnicalException;

/**
 * Exception mapper test using {@link TechnicalExceptionMapper}
 */
public class TechnicalExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final TechnicalException exception = new TechnicalException("message-error", "p", "v");
		check(mock(new TechnicalExceptionMapper()).toResponse(exception), 500,
				"{\"code\":\"technical\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}
}
