/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.hook;

import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.mockito.Mockito;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * HookProcessRunnable resource test, includes {@link HookProcessRunnable}
 */
@Slf4j
class HookProcessRunnableTest {

	@Test
	void processNotAllowed() {
		final var hook = new SystemHook();
		hook.setDelay(1);
		hook.setCommand("/path/to/any");
		final var configuration = mock(ConfigurationResource.class);
		Mockito.doReturn("^/path/other/.*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = mock(Exchange.class);
		new HookProcessRunnable(exchange, "GET", "path", null, null, "NOW", new ObjectMapper(), null, configuration).process(null, hook, null);
		Mockito.verify(exchange, Mockito.never()).getInMessage();
	}

	@Test
	void processAllowed() {
		final var hook = new SystemHook();
		hook.setName("hook1");
		hook.setDelay(1);
		hook.setCommand("/path/to/foo");
		final var configuration = mock(ConfigurationResource.class);
		Mockito.doReturn("^/path/other/.*,^/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = mock(Exchange.class);
		when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = mock(Message.class);
		when(exchange.getInMessage()).thenReturn(message);
		when(message.getContent(List.class)).thenReturn(Collections.emptyList());

		new HookProcessRunnable(exchange, "GET", "path", null, null, "NOW", new ObjectMapper(), hook, configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				throw new RuntimeException("Simulated failure");
			}
		}.process(null, hook, null);
		Mockito.verify(exchange, Mockito.atLeastOnce()).getInMessage();
	}

	@Test
	void processSync() {
		final var hook = new SystemHook();
		hook.setDelay(0); // Sync
		hook.setName("hook1");
		hook.setCommand("/path/to/foo");
		final var configuration = mock(ConfigurationResource.class);
		Mockito.doReturn("^/path/other/.*,^/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = mock(Exchange.class);
		when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = mock(Message.class);
		when(exchange.getInMessage()).thenReturn(message);
		when(message.getContent(List.class)).thenReturn(Collections.emptyList());

		final var outMessage = mock(Message.class);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		final var headers = new MetadataMap<String, Object>();
		when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);

		new HookProcessRunnable(exchange, "GET", "path", null, null, "NOW", new ObjectMapper(), hook, configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				throw new RuntimeException("Simulated failure");
			}
		}.process(null, hook, null);

		// Verify headers updated
		Assertions.assertEquals("FAILED", headers.getFirst("X-Ligoj-Hook-hook1"));
	}

	@Test
	void processSyncNoHeaders() {
		final var hook = new SystemHook();
		hook.setDelay(0); // Sync
		hook.setName("hook1");
		hook.setCommand("/path/to/foo");
		final var configuration = mock(ConfigurationResource.class);
		Mockito.doReturn("^/path/other/.*,^/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = mock(Exchange.class);
		when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = mock(Message.class);
		when(exchange.getInMessage()).thenReturn(message);
		when(message.getContent(List.class)).thenReturn(Collections.emptyList());

		final var outMessage = mock(Message.class);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(null);

		new HookProcessRunnable(exchange, "GET", "path", null, null, "NOW", new ObjectMapper(), hook, configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				throw new RuntimeException("Simulated failure");
			}
		}.process(null, hook, null);

		// Verify headers updated
		Mockito.verify(outMessage).put(Mockito.eq(Message.PROTOCOL_HEADERS), Mockito.any());
	}

	@Test
	void processTimeout() throws Exception {
		final var hook = new SystemHook();
		hook.setName("hookTimeout");
		hook.setDelay(1);
		hook.setCommand("/path/to/foo");
		hook.setTimeout(1);

		final var configuration = mock(ConfigurationResource.class);
		Mockito.doReturn(".*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = mock(Exchange.class);
		when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = mock(Message.class);
		when(exchange.getInMessage()).thenReturn(message);
		when(message.getContent(List.class)).thenReturn(Collections.emptyList());

		final var process = mock(Process.class);
		final var inputStream = mock(InputStream.class);
		when(process.getInputStream()).thenReturn(inputStream);
		when(inputStream.transferTo(Mockito.any())).thenReturn(0L);
		when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(false); // Timeout

		final var capturedPb = new AtomicReference<ProcessBuilder>();

		new HookProcessRunnable(exchange, "GET", "path", null, null, "NOW", new ObjectMapper(), hook, configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				final var pb = mock(ProcessBuilder.class);
				capturedPb.set(pb);
				when(pb.environment()).thenReturn(new HashMap<>());
				try {
					when(pb.start()).thenReturn(process);
				} catch (IOException _) {
					// Ignore
				}
				return pb;
			}
		}.process(null, hook, new ByteArrayOutputStream());

		Assertions.assertNotNull(capturedPb.get(), "newBuilder was not called");
		Mockito.verify(process).waitFor(1, TimeUnit.SECONDS);
	}

	@Test
	void newBuilder() {
		final var hook = new SystemHook();
		hook.setCommand("cmd arg1");
		hook.setWorkingDirectory("wd");
		final var runnable = new HookProcessRunnable(null, null, null, null, null, null, null, null, null);
		final var builder = runnable.newBuilder(hook);
		Assertions.assertEquals("wd", builder.directory().getName());
		Assertions.assertEquals(List.of("cmd", "arg1"), builder.command());
	}

	@Test
	void processSynchronous() {
		final var hook = new SystemHook();
		hook.setName("hook_1"); // Space in name
		hook.setDelay(0);
		hook.setCommand("cmd");

		final var configuration = mock(ConfigurationResource.class);
		Mockito.doReturn(".*").when(configuration).get("ligoj.hook.path", "^$");

		final var exchange = mock(Exchange.class);
		when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = mock(Message.class);
		when(exchange.getInMessage()).thenReturn(message);
		// Use String to avoid serialization issues
		when(message.getContent(List.class)).thenReturn(List.of("param1"));

		final var outMessage = mock(Message.class);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		final var headers = new MetadataMap<String, Object>();
		when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);

		// Use String for response
		final var response = "response1";

		final var capturedPayload = new AtomicReference<String>();

		new HookProcessRunnable(exchange, "GET", "/path", null, response, "NOW", new ObjectMapper(), hook, configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				final var pb = mock(ProcessBuilder.class);
				final var env = new HashMap<String, String>();
				when(pb.environment()).thenReturn(env);
				try {
					when(pb.start()).thenAnswer(invocation -> {
						capturedPayload.set(env.get("PAYLOAD"));
						throw new RuntimeException("Stop here");
					});
				} catch (IOException _) {
					// Ignore
				}
				return pb;
			}
		}.process("/path", hook, null);

		// Verify header name sanitization
		Assertions.assertEquals("FAILED", headers.getFirst("X-Ligoj-Hook-hook_1"));

		// Verify payload
		Assertions.assertNotNull(capturedPayload.get());
		final var jsonString = new String(HookProcessRunnable.BASE64_CODEC.decode(capturedPayload.get()), StandardCharsets.UTF_8);
		final var payload = new ObjectMapper().readTree(jsonString);

		Assertions.assertTrue(payload.get("user").isNull());
		Assertions.assertEquals("response1", payload.get("result").stringValue());
		Assertions.assertEquals("param1", payload.get("params").get(0).stringValue());
	}

	@Test
	void run() {
		final var response = Map.of("key1", "value1");
		final var configuration = mock(ConfigurationResource.class);
		Mockito.doReturn("/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		when(configuration.get("ligoj.hook.timeout", HookProcessRunnable.DEFAULT_TIMEOUT)).thenReturn(30);

		final var exchange = mock(Exchange.class);
		final var principal = mock(Principal.class);
		final var uriInfo = mock(UriInfo.class);
		final var inMessage = mock(Message.class);
		final var inList = Arrays.asList("in1", "in2", uriInfo, mock(SecurityContext.class), null);
		final var local = new ThreadLocal<Map<String, ProcessBuilder>>();
		local.set(new ConcurrentHashMap<>());
		final var environments = new ConcurrentHashMap<String, Map<String, String>>();
		when(configuration.get("conf1", "")).thenReturn("value1");
		when(configuration.get("conf2", "")).thenReturn("");
		when(uriInfo.getPath()).thenReturn("foo/bar");
		when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("Resource#method");
		when(exchange.getInMessage()).thenReturn(inMessage);
		when(inMessage.getContent(List.class)).thenReturn(inList);
		when(principal.getName()).thenReturn("junit");

		final var outMessage = mock(Message.class);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		final var headers = new MetadataMap<String, Object>();
		when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);

		final var hook1 = new SystemHook();
		hook1.setName("hook1");
		hook1.setDelay(0);
		hook1.setCommand("/path/to/some args");
		hook1.setInject(List.of("conf1", "conf2"));
		hook1.setWorkingDirectory("working/directory");

		final var runnable = new HookProcessRunnable(exchange, "GET", "foo/bar", principal, response, "NOW", new ObjectMapper(), hook1, configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				final var builder = super.newBuilder(hook);
				local.get().put(hook.getName(), builder);
				final var processBuilder = mock(ProcessBuilder.class);
				final var environment = new HashMap<String, String>();
				environments.put(hook.getName(), environment);
				when(processBuilder.environment()).thenReturn(environment);
				final var process = mock(Process.class);
				try {
					when(processBuilder.start()).thenReturn(process);
					when(process.getInputStream()).thenReturn(new ByteArrayInputStream("process_response".getBytes(StandardCharsets.UTF_8)));
					final var timeout = ObjectUtils.getIfNull(hook.getTimeout(), 30);
					Mockito.doReturn(timeout != 1).when(process).waitFor(timeout, TimeUnit.SECONDS);
					Mockito.doReturn(0).when(process).exitValue();
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
		Assertions.assertEquals(List.of("/path/to/some", "args"), executedProcessBuilder.command());

		final var payload64 = environments.get("hook1").get("PAYLOAD");
		Assertions.assertNotNull(payload64);
		final var jsonString = new String(HookProcessRunnable.BASE64_CODEC.decode(payload64), StandardCharsets.UTF_8);
		final var payload = new ObjectMapper().readTree(jsonString);
		Assertions.assertEquals("GET", payload.get("method").stringValue());
		Assertions.assertEquals("hook1", payload.get("name").stringValue());
		Assertions.assertEquals("junit", payload.get("user").stringValue());
		Assertions.assertEquals("foo/bar", payload.get("path").stringValue());
		Assertions.assertEquals("NOW", payload.get("now").stringValue());
		Assertions.assertEquals("Resource#method", payload.get("api").stringValue());
		Assertions.assertEquals("{\"conf2\":\"\",\"conf1\":\"value1\"}", payload.get("inject").toString());

		// Check captured output
		Assertions.assertEquals("process_response", headers.getFirst("X-Ligoj-Hook-hook1-Message"));
	}

	@Test
	void runLargeOutput() {
		final var response = Map.of("key1", "value1");
		final var configuration = mock(ConfigurationResource.class);
		Mockito.doReturn("/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		when(configuration.get("ligoj.hook.timeout", HookProcessRunnable.DEFAULT_TIMEOUT)).thenReturn(30);

		final var exchange = mock(Exchange.class);
		final var principal = mock(Principal.class);
		final var uriInfo = mock(UriInfo.class);
		final var inMessage = mock(Message.class);
		final var inList = Arrays.asList("in1", "in2", uriInfo, mock(SecurityContext.class), null);
		final var local = new ThreadLocal<Map<String, ProcessBuilder>>();
		local.set(new ConcurrentHashMap<>());
		final var environments = new ConcurrentHashMap<String, Map<String, String>>();
		when(configuration.get("conf1", "")).thenReturn("value1");
		when(configuration.get("conf2", "")).thenReturn("");
		when(uriInfo.getPath()).thenReturn("foo/bar");
		when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("Resource#method");
		when(exchange.getInMessage()).thenReturn(inMessage);
		when(inMessage.getContent(List.class)).thenReturn(inList);
		when(principal.getName()).thenReturn("junit");

		final var outMessage = mock(Message.class);
		when(exchange.getOutMessage()).thenReturn(outMessage);
		final var headers = new MetadataMap<String, Object>();
		when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);

		final var hook1 = new SystemHook();
		hook1.setName("hook1");
		hook1.setDelay(0);
		hook1.setCommand("/path/to/some args");
		hook1.setInject(List.of("conf1", "conf2"));
		hook1.setWorkingDirectory("working/directory");

		final var runnable = new HookProcessRunnable(exchange, "GET", "foo/bar", principal, response, "NOW", new ObjectMapper(), hook1, configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				final var builder = super.newBuilder(hook);
				local.get().put(hook.getName(), builder);
				final var processBuilder = mock(ProcessBuilder.class);
				final var environment = new HashMap<String, String>();
				environments.put(hook.getName(), environment);
				when(processBuilder.environment()).thenReturn(environment);
				final var process = mock(Process.class);
				try {
					when(processBuilder.start()).thenReturn(process);
					// Generate a large output > 2048 bytes
					final var largeOutput = "0".repeat(3000);
					when(process.getInputStream()).thenReturn(new ByteArrayInputStream(largeOutput.getBytes(StandardCharsets.UTF_8)));
					final var timeout = ObjectUtils.getIfNull(hook.getTimeout(), 30);
					Mockito.doReturn(timeout != 1).when(process).waitFor(timeout, TimeUnit.SECONDS);
					Mockito.doReturn(1).when(process).exitValue();
				} catch (final Exception e) {
					log.error("Unable to mock process", e);
				}
				return processBuilder;
			}
		};
		runnable.run();

		// Check captured output is truncated to 2048
		final var capturedMessage = (String) headers.getFirst("X-Ligoj-Hook-hook1-Message");
		Assertions.assertNotNull(capturedMessage);
		Assertions.assertEquals(2048, capturedMessage.length());
		Assertions.assertTrue(capturedMessage.startsWith("00000"));
	}

	@Test
	void limitCaptureOutputStream() throws IOException {
		final var out = new ByteArrayOutputStream();
		final var captured = new ByteArrayOutputStream();
		try (var limitOut = new HookProcessRunnable.LimitCaptureOutputStream(out, captured, 5)) {
			limitOut.write('0');
			limitOut.write("12".getBytes(), 0, 2);
			limitOut.write("345".getBytes(), 0, 3);
			limitOut.write('6');
			limitOut.write("789".getBytes(), 0, 3);
		}
		Assertions.assertEquals("0123456789", out.toString());
		Assertions.assertEquals("01234", captured.toString());
	}
}