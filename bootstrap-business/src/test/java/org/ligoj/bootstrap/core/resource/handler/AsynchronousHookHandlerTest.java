/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.hook.HookProcessRunnable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link AsynchronousHookInterceptor}
 */
class AsynchronousHookInterceptorTest {

	@Test
	void handleMessageIgnoreStatusLow() {
		final var handler = new AsynchronousHookInterceptor();
		final var hookConfiguration = mock(HookConfiguration.class);
		handler.hookConfiguration = hookConfiguration;
		
		final var message = mock(Message.class);
		final var exchange = mock(Exchange.class);
		final var outMessage = mock(Message.class);
		final var request = mock(SecurityContextHolderAwareRequestWrapper.class);

		when(message.get("HTTP.REQUEST")).thenReturn(request);
		when(message.getExchange()).thenReturn(exchange);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		when(outMessage.get("org.apache.cxf.message.Message.RESPONSE_CODE")).thenReturn(100);
		when(request.getPathInfo()).thenReturn("/path");

		handler.handleMessage(message);
		Mockito.verifyNoInteractions(hookConfiguration);
	}

	@Test
	void handleMessageIgnoreStatusHigh() {
		final var handler = new AsynchronousHookInterceptor();
		final var hookConfiguration = mock(HookConfiguration.class);
		handler.hookConfiguration = hookConfiguration;

		final var message = mock(Message.class);
		final var exchange = mock(Exchange.class);
		final var outMessage = mock(Message.class);
		final var request = mock(SecurityContextHolderAwareRequestWrapper.class);

		when(message.get("HTTP.REQUEST")).thenReturn(request);
		when(message.getExchange()).thenReturn(exchange);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		when(outMessage.get("org.apache.cxf.message.Message.RESPONSE_CODE")).thenReturn(300);
		when(request.getPathInfo()).thenReturn("/path");

		handler.handleMessage(message);
		Mockito.verifyNoInteractions(hookConfiguration);
	}

	@SuppressWarnings("unchecked")
	@Test
	void handleMessage() {
		final var handler = new AsynchronousHookInterceptor();
		final var hookConfiguration = mock(HookConfiguration.class);
		handler.hookConfiguration = hookConfiguration;

		final var message = mock(Message.class);
		final var exchange = mock(Exchange.class);
		final var outMessage = mock(Message.class);
		final var request = mock(SecurityContextHolderAwareRequestWrapper.class);
		final var principal = mock(Principal.class);

		when(message.get("HTTP.REQUEST")).thenReturn(request);
		when(message.getExchange()).thenReturn(exchange);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		when(outMessage.get("org.apache.cxf.message.Message.RESPONSE_CODE")).thenReturn(200);
		when(request.getUserPrincipal()).thenReturn(principal);
		when(request.getPathInfo()).thenReturn("/path");
		when(request.getMethod()).thenReturn("GET");
		when(outMessage.getContent(List.class)).thenReturn(List.of("response"));

		handler.handleMessage(message);

		final var captor = ArgumentCaptor.forClass(BiConsumer.class);
		Mockito.verify(hookConfiguration).process(Mockito.eq(exchange), Mockito.eq("GET"), Mockito.eq("path"),
				Mockito.eq(principal), Mockito.eq("response"), Mockito.any(Predicate.class), captor.capture());

		// Verify processor execution
		final var hook = new SystemHook();
		hook.setDelay(1);
		final var runnable = mock(HookProcessRunnable.class);
		
		// We cannot easily verify CompletableFuture execution without waiting or mocking static methods.
		// But we can verify that accept is called without exception.
		captor.getValue().accept(hook, runnable);
		
		// Verify predicate
		final var predicateCaptor = ArgumentCaptor.forClass(Predicate.class);
		Mockito.verify(hookConfiguration).process(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), predicateCaptor.capture(), Mockito.any());

		// Not executed, since synchronous hook
		final var hookSync = new SystemHook();
		hookSync.setDelay(0);
		Assertions.assertFalse(((Predicate<SystemHook>)predicateCaptor.getValue()).test(hookSync));
		
		final var hookAsync = new SystemHook();
		hookAsync.setDelay(1);
		Assertions.assertTrue(((Predicate<SystemHook>)predicateCaptor.getValue()).test(hookAsync));
	}

	@Test
	void handleMessageEmptyResponse() {
		final var handler = new AsynchronousHookInterceptor();
		final var hookConfiguration = mock(HookConfiguration.class);
		handler.hookConfiguration = hookConfiguration;

		final var message = mock(Message.class);
		final var exchange = mock(Exchange.class);
		final var outMessage = mock(Message.class);
		final var request = mock(SecurityContextHolderAwareRequestWrapper.class);

		when(message.get("HTTP.REQUEST")).thenReturn(request);
		when(message.getExchange()).thenReturn(exchange);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		when(outMessage.get("org.apache.cxf.message.Message.RESPONSE_CODE")).thenReturn(200);
		when(request.getPathInfo()).thenReturn("path"); // No leading slash
		when(outMessage.getContent(List.class)).thenReturn(Collections.emptyList());

		handler.handleMessage(message);

		Mockito.verify(hookConfiguration).process(Mockito.eq(exchange), Mockito.any(), Mockito.eq("path"),
				Mockito.any(), Mockito.isNull(), Mockito.any(), Mockito.any());
	}
}
