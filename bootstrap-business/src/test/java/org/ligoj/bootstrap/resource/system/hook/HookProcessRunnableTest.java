/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * HookProcessRunnable resource test, includes {@link HookProcessRunnable}
 */
@Slf4j
class HookProcessRunnableTest {

	@Test
	void run() throws IOException {
		final var requestContext = Mockito.mock(ContainerRequestContext.class);
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		final var configuration = Mockito.mock(ConfigurationResource.class);
		final var exchange = Mockito.mock(Exchange.class);
		final var principal = Mockito.mock(Principal.class);
		final var uriInfo = Mockito.mock(UriInfo.class);
		final var inMessage = Mockito.mock(Message.class);
		final var inList = List.of("in1", "in2", uriInfo, Mockito.mock(SecurityContext.class));
		final var local = new ThreadLocal<Map<String, ProcessBuilder>>();
		local.set(new ConcurrentHashMap<>());
		final var environments = new ConcurrentHashMap<String, Map<String, String>>();
		Mockito.when(configuration.get("conf1", "")).thenReturn("value1");
		Mockito.when(configuration.get("conf2", "")).thenReturn("");
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(uriInfo.getPath()).thenReturn("foo/bar");
		Mockito.when(requestContext.getMethod()).thenReturn("GET");
		Mockito.when(responseContext.getEntity()).thenReturn(Map.of("key1", "value1"));
		Mockito.when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("Resource#method");
		Mockito.when(exchange.getInMessage()).thenReturn(inMessage);
		Mockito.when(inMessage.getContent(List.class)).thenReturn(inList);
		Mockito.when(principal.getName()).thenReturn("junit");

		final var hook1 = new SystemHook();
		hook1.setName("hook1");
		hook1.setCommand("some args");
		hook1.setInject(List.of("conf1", "conf2"));
		hook1.setWorkingDirectory("working/directory");


		final var hookNPE = new SystemHook();
		hookNPE.setName("hookNPE");

		final var hookTimeout = new SystemHook();
		hookTimeout.setName("hookTimeout");
		hookTimeout.setCommand("some args");
		hookTimeout.setWorkingDirectory("working/directory");
		hookTimeout.setTimeout(1);

		final var hooks = Map.of(Pattern.compile(".*"), List.of(hook1, hookNPE, hookTimeout));

		final var runnable = new HookProcessRunnable("NOW", new ObjectMapper(), hooks, requestContext, responseContext,
				exchange, principal, configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				final var builder = super.newBuilder(hook);
				local.get().put(hook.getName(), builder);
				final var processBuilder = Mockito.mock(ProcessBuilder.class);
				final var environment = new HashMap<String, String>();
				environments.put(hook.getName(), environment);
				Mockito.when(processBuilder.environment()).thenReturn(environment);
				final var process = Mockito.mock(Process.class);
				try {
					Mockito.when(processBuilder.start()).thenReturn(process);
					Mockito.when(process.getInputStream()).thenReturn(new ByteArrayInputStream("process_response".getBytes(StandardCharsets.UTF_8)));
					final var timeout = ObjectUtils.defaultIfNull(hook.getTimeout(), 30);
					Mockito.doReturn(timeout != 1).when(process).waitFor(timeout, TimeUnit.SECONDS);
					Mockito.doReturn(1).when(process).exitValue();
				} catch (final Exception e) {
					log.error("Unable to mock process", e);
				}
				return processBuilder;
			}
		};
		runnable.run();
		Assertions.assertFalse(local.get().isEmpty());
		final var executedProcessBuilder = local.get().get("hook1");
		Assertions.assertNotNull(executedProcessBuilder);
		Assertions.assertEquals("working/directory", executedProcessBuilder.directory().toString());
		Assertions.assertEquals(List.of("some", "args"), executedProcessBuilder.command());

		final var payload64 = environments.get("hook1").get("PAYLOAD");
		Assertions.assertNotNull(payload64);
		final var jsonString = new String(HookProcessRunnable.BASE64_CODEC.decode(payload64), StandardCharsets.UTF_8);
		final var payload = new ObjectMapper().readTree(jsonString);
		Assertions.assertEquals("GET", payload.get("method").textValue());
		Assertions.assertEquals("hook1", payload.get("name").textValue());
		Assertions.assertEquals("junit", payload.get("user").textValue());
		Assertions.assertEquals("foo/bar", payload.get("path").textValue());
		Assertions.assertEquals("NOW", payload.get("now").textValue());
		Assertions.assertEquals("Resource#method", payload.get("api").textValue());
		Assertions.assertEquals("{\"conf2\":\"\",\"conf1\":\"value1\"}", payload.get("inject").toString());
	}
}