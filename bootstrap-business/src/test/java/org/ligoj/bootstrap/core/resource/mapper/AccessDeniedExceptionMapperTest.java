/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.security.access.AccessDeniedException;

/**
 * Exception mapper test using {@link AccessDeniedExceptionMapper}
 */
public class AccessDeniedExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final AccessDeniedException exception = new AccessDeniedException("message-error");
		check(mock(new AccessDeniedExceptionMapper()).toResponse(exception), 403,
				"{\"code\":\"security\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

}