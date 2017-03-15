package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

/**
 * Exception mapper test using {@link AuthenticationExceptionMapper}
 */
public class AuthenticationExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final PreAuthenticatedCredentialsNotFoundException exception = new PreAuthenticatedCredentialsNotFoundException("message-error");
		check(mock(new AuthenticationExceptionMapper()).toResponse(exception), 401,
				"{\"code\":\"security\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}
}
