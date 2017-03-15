package org.ligoj.bootstrap.core.resource.mapper;

import javax.naming.CommunicationException;

import org.junit.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.ligoj.bootstrap.core.resource.TechnicalException;

/**
 * Exception mapper test using {@link FailSafeExceptionMapper}
 */
public class FailSafeExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final Exception exception = new NullPointerException();
		check(mock(new FailSafeExceptionMapper()).toResponse(exception), 500,
				"{\"code\":\"internal\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

	@Test
	public void toResponseCommunicationException() {
		final Exception exception = new TechnicalException("some", new CommunicationException());
		check(mock(new FailSafeExceptionMapper()).toResponse(exception), 503,
				"{\"code\":\"ldap-down\",\"message\":null,\"parameters\":null,\"cause\":null}");

	}
	
}
