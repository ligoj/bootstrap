/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.mail.MailSendException;

/**
 * Exception mapper test using {@link MailSendExceptionMapper}
 */
public class MailSendExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final MailSendException exception = new MailSendException("message-error");
		check(mock(new MailSendExceptionMapper()).toResponse(exception), 503,
				"{\"code\":\"mail-down\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

	@Test
	public void toResponseMessageDrop() {
		final MailSendException exception = new MailSendException("message", new IOException("Connection refused"));
		check(mock(new MailSendExceptionMapper()).toResponse(exception), 503,
				"{\"code\":\"mail-down\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

}
