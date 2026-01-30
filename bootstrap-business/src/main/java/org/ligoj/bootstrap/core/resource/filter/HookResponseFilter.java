/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.jaxrs.impl.AbstractPropertiesImpl;
import org.ligoj.bootstrap.core.resource.handler.HookConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Synchronous hook execution.
 */
@Provider
@Slf4j
public class HookResponseFilter implements ContainerResponseFilter {

	@Autowired
	protected HookConfiguration hookConfiguration;

	@Override
	public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
		final var status = responseContext.getStatus();
		if (status >= 200 && status < 300) {
			final var exchange = ((AbstractPropertiesImpl) requestContext).getMessage().getExchange();
			final var principal = requestContext.getSecurityContext().getUserPrincipal();
			final var path = requestContext.getUriInfo().getPath();
			final var responseList = exchange.getOutMessage().getContent(List.class);
			final var response = responseList.isEmpty() ? null : responseList.getFirst();
			hookConfiguration.process(exchange, requestContext.getMethod(), path, principal, response,
					hook -> hook.getDelay() == 0,
					(hook, runnable) -> runnable.run());
		}
	}
}
