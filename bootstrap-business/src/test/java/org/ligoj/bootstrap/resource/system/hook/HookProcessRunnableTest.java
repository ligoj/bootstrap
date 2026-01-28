/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
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
		final var configuration = Mockito.mock(ConfigurationResource.class);
		Mockito.doReturn("^/path/other/.*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = Mockito.mock(Exchange.class);
		new HookProcessRunnable(exchange, "GET", "path", null,
				null,
				"NOW",
				new ObjectMapper(),
				null,
				configuration).process(null, hook, null);
		Mockito.verify(exchange, Mockito.never()).getInMessage();
	}

	@Test
	void processAllowed() {
		final var hook = new SystemHook();
		hook.setName("hook1");
		hook.setDelay(1);
		hook.setCommand("/path/to/foo");
		final var configuration = Mockito.mock(ConfigurationResource.class);
		Mockito.doReturn("^/path/other/.*,^/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = Mockito.mock(Exchange.class);
		Mockito.when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = Mockito.mock(Message.class);
		Mockito.when(exchange.getInMessage()).thenReturn(message);
		Mockito.when(message.getContent(List.class)).thenReturn(Collections.emptyList());

		new HookProcessRunnable(exchange, "GET", "path", null,
				null,
				"NOW",
				new ObjectMapper(),
				hook,
				configuration) {
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
		final var configuration = Mockito.mock(ConfigurationResource.class);
		Mockito.doReturn("^/path/other/.*,^/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = Mockito.mock(Exchange.class);
		Mockito.when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = Mockito.mock(Message.class);
		Mockito.when(exchange.getInMessage()).thenReturn(message);
		Mockito.when(message.getContent(List.class)).thenReturn(Collections.emptyList());

		final var outMessage = Mockito.mock(Message.class);
		Mockito.when(exchange.getOutMessage()).thenReturn(outMessage);
		final var headers = new MetadataMap<String, Object>();
		Mockito.when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);

		new HookProcessRunnable(exchange, "GET", "path", null,
				null,
				"NOW",
				new ObjectMapper(),
				hook,
				configuration) {
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
		final var configuration = Mockito.mock(ConfigurationResource.class);
		Mockito.doReturn("^/path/other/.*,^/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = Mockito.mock(Exchange.class);
		Mockito.when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = Mockito.mock(Message.class);
		Mockito.when(exchange.getInMessage()).thenReturn(message);
		Mockito.when(message.getContent(List.class)).thenReturn(Collections.emptyList());

		final var outMessage = Mockito.mock(Message.class);
		Mockito.when(exchange.getOutMessage()).thenReturn(outMessage);
		Mockito.when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(null);

		new HookProcessRunnable(exchange, "GET", "path", null,
				null,
				"NOW",
				new ObjectMapper(),
				hook,
				configuration) {
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

		final var configuration = Mockito.mock(ConfigurationResource.class);
		Mockito.doReturn(".*").when(configuration).get("ligoj.hook.path", "^$");
		final var exchange = Mockito.mock(Exchange.class);
		Mockito.when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = Mockito.mock(Message.class);
		Mockito.when(exchange.getInMessage()).thenReturn(message);
		Mockito.when(message.getContent(List.class)).thenReturn(Collections.emptyList());

		final var process = Mockito.mock(Process.class);
		final var inputStream = Mockito.mock(InputStream.class);
		Mockito.when(process.getInputStream()).thenReturn(inputStream);
		Mockito.when(inputStream.transferTo(Mockito.any())).thenReturn(0L);
		Mockito.when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(false); // Timeout

		final var capturedPb = new AtomicReference<ProcessBuilder>();

		new HookProcessRunnable(exchange, "GET", "path", null,
				null,
				"NOW",
				new ObjectMapper(),
				hook,
				configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				final var pb = Mockito.mock(ProcessBuilder.class);
				capturedPb.set(pb);
				Mockito.when(pb.environment()).thenReturn(new HashMap<>());
				try {
					Mockito.when(pb.start()).thenReturn(process);
				} catch (IOException e) {
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
	void processPayload() throws IOException {
		final var hook = new SystemHook();
		hook.setName("hook 1"); // Space in name
		hook.setDelay(0);
		hook.setCommand("cmd");

		final var configuration = Mockito.mock(ConfigurationResource.class);
		Mockito.doReturn(".*").when(configuration).get("ligoj.hook.path", "^$");

		final var exchange = Mockito.mock(Exchange.class);
		Mockito.when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("op");
		final var message = Mockito.mock(Message.class);
		Mockito.when(exchange.getInMessage()).thenReturn(message);
		// Use String to avoid serialization issues
		Mockito.when(message.getContent(List.class)).thenReturn(List.of("param1"));

		final var outMessage = Mockito.mock(Message.class);
		Mockito.when(exchange.getOutMessage()).thenReturn(outMessage);
		final var headers = new MetadataMap<String, Object>();
		Mockito.when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);

		// Use String for response
		final var response = "response1";

		final var capturedPayload = new AtomicReference<String>();

		new HookProcessRunnable(exchange, "GET", "path", null, // null principal
				response,
				"NOW",
				new ObjectMapper(),
				hook,
				configuration) {
			@Override
			ProcessBuilder newBuilder(final SystemHook hook) {
				final var pb = Mockito.mock(ProcessBuilder.class);
				final var env = new HashMap<String, String>();
				Mockito.when(pb.environment()).thenReturn(env);
				try {
					Mockito.when(pb.start()).thenAnswer(invocation -> {
						capturedPayload.set(env.get("PAYLOAD"));
						throw new RuntimeException("Stop here");
					});
				} catch (IOException e) {
					// Ignore
				}
				return pb;
			}
		}.process(null, hook, null);

		// Verify header name sanitization
		Assertions.assertEquals("FAILED", headers.getFirst("X-Ligoj-Hook-hook-1"));

		// Verify payload
		Assertions.assertNotNull(capturedPayload.get());
		final var jsonString = new String(HookProcessRunnable.BASE64_CODEC.decode(capturedPayload.get()), StandardCharsets.UTF_8);
		final var payload = new ObjectMapper().readTree(jsonString);

		Assertions.assertTrue(payload.get("user").isNull());
		Assertions.assertEquals("response1", payload.get("result").textValue());
		Assertions.assertEquals("param1", payload.get("params").get(0).textValue());
	}

	@Test
	void run() throws IOException {
		final var response = Map.of("key1", "value1");
		final var configuration = Mockito.mock(ConfigurationResource.class);
		Mockito.doReturn("/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		Mockito.when(configuration.get("LIGOJ_HOOK_TIMEOUT", HookProcessRunnable.DEFAULT_TIMEOUT)).thenReturn(30);

		final var exchange = Mockito.mock(Exchange.class);
		final var principal = Mockito.mock(Principal.class);
		final var uriInfo = Mockito.mock(UriInfo.class);
		final var inMessage = Mockito.mock(Message.class);
		final var inList = Arrays.asList("in1", "in2", uriInfo, Mockito.mock(SecurityContext.class), null);
		final var local = new ThreadLocal<Map<String, ProcessBuilder>>();
		local.set(new ConcurrentHashMap<>());
		final var environments = new ConcurrentHashMap<String, Map<String, String>>();
		Mockito.when(configuration.get("conf1", "")).thenReturn("value1");
		Mockito.when(configuration.get("conf2", "")).thenReturn("");
		Mockito.when(uriInfo.getPath()).thenReturn("foo/bar");
		Mockito.when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("Resource#method");
		Mockito.when(exchange.getInMessage()).thenReturn(inMessage);
		Mockito.when(inMessage.getContent(List.class)).thenReturn(inList);
		Mockito.when(principal.getName()).thenReturn("junit");

		final var outMessage = Mockito.mock(Message.class);
		Mockito.when(exchange.getOutMessage()).thenReturn(outMessage);
		final var headers = new MetadataMap<String, Object>();
		Mockito.when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);

		final var hook1 = new SystemHook();
		hook1.setName("hook1");
		hook1.setDelay(0);
		hook1.setCommand("/path/to/some args");
		hook1.setInject(List.of("conf1", "conf2"));
		hook1.setWorkingDirectory("working/directory");

		final var runnable = new HookProcessRunnable(exchange, "GET", "foo/bar", principal,
				response,
				"NOW",
				new ObjectMapper(),
				hook1,
				configuration) {
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
		Assertions.assertFalse(local.get().isEmpty());
		final var executedProcessBuilder = local.get().get("hook1");
		Assertions.assertNotNull(executedProcessBuilder);
		Assertions.assertEquals("working/directory", executedProcessBuilder.directory().toString());
		Assertions.assertEquals(List.of("/path/to/some", "args"), executedProcessBuilder.command());

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

		// Check captured output
		Assertions.assertEquals("process_response", headers.getFirst("X-Ligoj-Hook-hook1-message"));
	}

	@Test
	void runLargeOutput() throws IOException {
		final var response = Map.of("key1", "value1");
		final var configuration = Mockito.mock(ConfigurationResource.class);
		Mockito.doReturn("/path/to/.*").when(configuration).get("ligoj.hook.path", "^$");
		Mockito.when(configuration.get("LIGOJ_HOOK_TIMEOUT", HookProcessRunnable.DEFAULT_TIMEOUT)).thenReturn(30);

		final var exchange = Mockito.mock(Exchange.class);
		final var principal = Mockito.mock(Principal.class);
		final var uriInfo = Mockito.mock(UriInfo.class);
		final var inMessage = Mockito.mock(Message.class);
		final var inList = Arrays.asList("in1", "in2", uriInfo, Mockito.mock(SecurityContext.class), null);
		final var local = new ThreadLocal<Map<String, ProcessBuilder>>();
		local.set(new ConcurrentHashMap<>());
		final var environments = new ConcurrentHashMap<String, Map<String, String>>();
		Mockito.when(configuration.get("conf1", "")).thenReturn("value1");
		Mockito.when(configuration.get("conf2", "")).thenReturn("");
		Mockito.when(uriInfo.getPath()).thenReturn("foo/bar");
		Mockito.when(exchange.get("org.apache.cxf.resource.operation.name")).thenReturn("Resource#method");
		Mockito.when(exchange.getInMessage()).thenReturn(inMessage);
		Mockito.when(inMessage.getContent(List.class)).thenReturn(inList);
		Mockito.when(principal.getName()).thenReturn("junit");

		final var outMessage = Mockito.mock(Message.class);
		Mockito.when(exchange.getOutMessage()).thenReturn(outMessage);
		final var headers = new MetadataMap<String, Object>();
		Mockito.when(outMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);

		final var hook1 = new SystemHook();
		hook1.setName("hook1");
		hook1.setDelay(0);
		hook1.setCommand("/path/to/some args");
		hook1.setInject(List.of("conf1", "conf2"));
		hook1.setWorkingDirectory("working/directory");

		final var runnable = new HookProcessRunnable(exchange, "GET", "foo/bar", principal,
				response,
				"NOW",
				new ObjectMapper(),
				hook1,
				configuration) {
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
					// Generate a large output > 2048 bytes
					final var largeOutput = "a".repeat(3000);
					Mockito.when(process.getInputStream()).thenReturn(new ByteArrayInputStream(largeOutput.getBytes(StandardCharsets.UTF_8)));
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
		final var capturedMessage = (String) headers.getFirst("X-Ligoj-Hook-hook1-message");
		Assertions.assertNotNull(capturedMessage);
		Assertions.assertEquals(2048, capturedMessage.length());
		Assertions.assertTrue(capturedMessage.startsWith("aaaaa"));
	}

	@Test
	void limitCaptureOutputStream() throws IOException {
		final var out = new ByteArrayOutputStream();
		final var captured = new ByteArrayOutputStream();
		final var limitOut = new HookProcessRunnable.LimitCaptureOutputStream(out, captured, 5);
		limitOut.write('a');
		limitOut.write("bc".getBytes(), 0, 2);
		limitOut.write("def".getBytes(), 0, 3);
		limitOut.write('g');
		limitOut.write("hij".getBytes(), 0, 3);
		Assertions.assertEquals("abcdefghij", out.toString());
		Assertions.assertEquals("abcde", captured.toString());
	}
}