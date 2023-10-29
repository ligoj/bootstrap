/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.UriInfo;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.hsqldb.lib.StringInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * HookProcessRunnable resource test, includes {@link HookProcessRunnable}
 */
class HookProcessRunnableTest {

	private final AtomicBoolean executed = new AtomicBoolean(false);

	@Test
	void run() throws IOException {
		final var processBuilder = Mockito.mock(ProcessBuilder.class);
		final var requestContext = Mockito.mock(ContainerRequestContext.class);
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		final var exchange = Mockito.mock(Exchange.class);
		final var principal = Mockito.mock(Principal.class);
		final var uriInfo = Mockito.mock(UriInfo.class);
		final var inMessage = Mockito.mock(Message.class);
		final var process = Mockito.mock(Process.class);
		final var inList = List.of("in1", "in2");
		final var local = new ThreadLocal<ProcessBuilder>();
		final var environment = new HashMap<String, String>();
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(uriInfo.getPath()).thenReturn("foo/bar");
		Mockito.when(requestContext.getMethod()).thenReturn("GET");
		Mockito.when(responseContext.getEntity()).thenReturn(Map.of("key1", "value1"));
		Mockito.when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("Resource#method");
		Mockito.when(exchange.getInMessage()).thenReturn(inMessage);
		Mockito.when(inMessage.getContent(List.class)).thenReturn(inList);
		Mockito.when(principal.getName()).thenReturn("junit");
		Mockito.when(processBuilder.environment()).thenReturn(environment);
		Mockito.when(processBuilder.start()).thenReturn(process);
		Mockito.when(process.getInputStream()).thenReturn(new StringInputStream("process_response"));

		final var hook1 = new SystemHook();
		hook1.setCommand("some args");
		hook1.setWorkingDirectory("working/directory");

		final var hookFail = new SystemHook();

		final var hooks = Map.of(Pattern.compile(".*"), List.of(hook1, hookFail));
		environment.get("PAYLOAD");

		final var runnable = new HookProcessRunnable("NOW", new ObjectMapper(), hooks, requestContext, responseContext,
				exchange, principal) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				local.set(super.newBuilder(hook));
				executed.set(true);
				return processBuilder;
			}
		};
		runnable.run();
		Assertions.assertTrue(executed.get());
		final var executedProcessBuilder = local.get();
		Assertions.assertNotNull(executedProcessBuilder);
		Assertions.assertEquals("working/directory", executedProcessBuilder.directory().toString());
		Assertions.assertEquals(List.of("some", "args"), executedProcessBuilder.command());

		final var payload64 = environment.get("PAYLOAD");
		Assertions.assertNotNull(payload64);
		final var jsonString = new String(HookProcessRunnable.BASE64_CODEC.decode(payload64), StandardCharsets.UTF_8);
		final var payload = new ObjectMapper().readTree(jsonString);
		Assertions.assertEquals("GET", payload.get("method").textValue());
		Assertions.assertEquals("junit", payload.get("user").textValue());
		Assertions.assertEquals("foo/bar", payload.get("path").textValue());
		Assertions.assertEquals("NOW", payload.get("now").textValue());
		Assertions.assertEquals("Resource#method", payload.get("api").textValue());

	}
}