/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.ForbiddenException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

/**
 * Exception mapper test using {@link ForbiddenExceptionMapper}
 */
public class ForbiddenExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final ForbiddenException exception = new ForbiddenException("message-error");
		check(mock(new ForbiddenExceptionMapper()).toResponse(exception), 403,
				"{\"code\":\"security\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

}
