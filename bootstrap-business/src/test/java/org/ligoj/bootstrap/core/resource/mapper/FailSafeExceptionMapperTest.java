/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.naming.CommunicationException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.ligoj.bootstrap.core.resource.TechnicalException;

/**
 * Exception mapper test using {@link FailSafeExceptionMapper}
 */
class FailSafeExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final Exception exception = new NullPointerException();
		check(mock(new FailSafeExceptionMapper()).toResponse(exception), 500,
				"{\"code\":\"internal\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

	@Test
	void toResponseCommunicationException() {
		final Exception exception = new TechnicalException("some", new CommunicationException());
		check(mock(new FailSafeExceptionMapper()).toResponse(exception), 503,
				"{\"code\":\"ldap-down\",\"message\":null,\"parameters\":null,\"cause\":null}");

	}
	
}
