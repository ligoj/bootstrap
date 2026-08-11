/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.impl.ContainerRequestContextImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.handler.HookConfiguration;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.hook.HookProcessRunnable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ContainerResponseFilter resource test, includes {@link HookResponseFilter}
 */
class HookResponseFilterTest {

	@Test
	void filterIgnoreStatusLow() {
		final var filter = new HookResponseFilter();
		final var requestContext = mock(ContainerRequestContextImpl.class);
		final var responseContext = mock(ContainerResponseContext.class);
		when(responseContext.getStatus()).thenReturn(100);
		filter.filter(requestContext, responseContext);
		Mockito.verifyNoInteractions(requestContext);
	}

	@Test
	void filterIgnoreStatusHigh() {
		final var filter = new HookResponseFilter();
		final var requestContext = mock(ContainerRequestContextImpl.class);
		final var responseContext = mock(ContainerResponseContext.class);
		when(responseContext.getStatus()).thenReturn(300);
		filter.filter(requestContext, responseContext);
		Mockito.verifyNoInteractions(requestContext);
	}

	@SuppressWarnings("unchecked")
	@Test
	void filter() {
		final var filter = new HookResponseFilter();
		final var hookConfiguration = mock(HookConfiguration.class);
		filter.hookConfiguration = hookConfiguration;

		final var requestContext = mock(ContainerRequestContextImpl.class);
		final var responseContext = mock(ContainerResponseContext.class);
		final var message = mock(Message.class);
		final var exchange = mock(Exchange.class);
		final var securityContext = mock(SecurityContext.class);
		final var principal = mock(Principal.class);
		final var uriInfo = mock(UriInfo.class);
		final var outMessage = mock(Message.class);

		when(responseContext.getStatus()).thenReturn(200);
		when(requestContext.getMessage()).thenReturn(message);
		when(message.getExchange()).thenReturn(exchange);
		when(requestContext.getSecurityContext()).thenReturn(securityContext);
		when(securityContext.getUserPrincipal()).thenReturn(principal);
		when(requestContext.getUriInfo()).thenReturn(uriInfo);
		when(uriInfo.getPath()).thenReturn("path");
		when(requestContext.getMethod()).thenReturn("GET");
		when(exchange.getOutMessage()).thenReturn(outMessage);
		when(outMessage.getContent(List.class)).thenReturn(List.of("response"));

		filter.filter(requestContext, responseContext);

		final var captor = ArgumentCaptor.forClass(BiConsumer.class);
		Mockito.verify(hookConfiguration).process(Mockito.eq(exchange), Mockito.eq("GET"), Mockito.eq("path"),
				Mockito.eq(principal), Mockito.eq("response"), Mockito.any(Predicate.class), captor.capture());

		// Verify processor execution
		final var hook = new SystemHook();
		final var runnable = mock(HookProcessRunnable.class);
		captor.getValue().accept(hook, runnable);
		Mockito.verify(runnable).run();
		
		// Verify predicate
		final var predicateCaptor = ArgumentCaptor.forClass(Predicate.class);
		Mockito.verify(hookConfiguration).process(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), predicateCaptor.capture(), Mockito.any());
		
		final var hookSync = new SystemHook();
		hookSync.setDelay(0);
		Assertions.assertTrue(((Predicate<SystemHook>)predicateCaptor.getValue()).test(hookSync));
		
		final var hookAsync = new SystemHook();
		hookAsync.setDelay(1);
		Assertions.assertFalse(((Predicate<SystemHook>)predicateCaptor.getValue()).test(hookAsync));
	}

	@Test
	void filterEmptyResponse() {
		final var filter = new HookResponseFilter();
		final var hookConfiguration = mock(HookConfiguration.class);
		filter.hookConfiguration = hookConfiguration;

		final var requestContext = mock(ContainerRequestContextImpl.class);
		final var responseContext = mock(ContainerResponseContext.class);
		final var message = mock(Message.class);
		final var exchange = mock(Exchange.class);
		final var securityContext = mock(SecurityContext.class);
		final var uriInfo = mock(UriInfo.class);
		final var outMessage = mock(Message.class);

		when(responseContext.getStatus()).thenReturn(200);
		when(requestContext.getMessage()).thenReturn(message);
		when(message.getExchange()).thenReturn(exchange);
		when(requestContext.getSecurityContext()).thenReturn(securityContext);
		when(requestContext.getUriInfo()).thenReturn(uriInfo);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		when(outMessage.getContent(List.class)).thenReturn(Collections.emptyList());

		filter.filter(requestContext, responseContext);

		Mockito.verify(hookConfiguration).process(Mockito.eq(exchange), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.isNull(), Mockito.any(), Mockito.any());
	}
}
