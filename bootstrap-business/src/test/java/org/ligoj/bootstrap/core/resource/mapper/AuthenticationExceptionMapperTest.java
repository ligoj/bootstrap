/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

/**
 * Exception mapper test using {@link AuthenticationExceptionMapper}
 */
class AuthenticationExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new PreAuthenticatedCredentialsNotFoundException("message-error");
		check(mock(new AuthenticationExceptionMapper()).toResponse(exception), 401,
				"{\"code\":\"security\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}
}
