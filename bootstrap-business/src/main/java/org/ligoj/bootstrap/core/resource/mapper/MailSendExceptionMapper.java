/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.springframework.mail.MailSendException;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles org.springframework.mail.MailSendException to a JSON string.
 */
@Provider
@Slf4j
public class MailSendExceptionMapper extends AbstractMapper implements ExceptionMapper<MailSendException> {

	@Override
	public Response toResponse(final MailSendException exception) {
		log.error("Mail exception", exception);
		return toResponse(Status.SERVICE_UNAVAILABLE, "mail-down", null);
	}

}
