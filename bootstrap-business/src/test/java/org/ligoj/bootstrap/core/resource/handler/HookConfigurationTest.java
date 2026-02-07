/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.cxf.message.Exchange;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.AbstractDataGeneratorTest;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.core.resource.filter.SystemHookParse;
import org.ligoj.bootstrap.dao.system.SystemHookRepository;
import org.ligoj.bootstrap.model.system.HookMatch;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.hook.HookProcessRunnable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Test class for {@link HookConfiguration}
 */
class HookConfigurationTest extends AbstractDataGeneratorTest {

	@InjectMocks
	private HookConfiguration hookConfiguration;

	@Mock
	private SystemHookRepository repository;

	@Mock
	private ObjectMapperTrim objectMapper;

	@Mock
	private HookConfiguration self;

	@BeforeEach
	void setup() {
		ReflectionTestUtils.setField(hookConfiguration, "self", self);
	}

	@Test
	void findAll() throws IOException {
		final var hook1 = new SystemHook();
		hook1.setName("hook1");
		hook1.setMatch("{\"path\":\"path1\"}");
		hook1.setCommand("command1");
		hook1.setWorkingDirectory("wd1");
		hook1.setTimeout(100);
		hook1.setInject(List.of("arg1"));
		hook1.setDelay(10);

		final var hook2 = new SystemHook();
		hook2.setName("hook2");
		hook2.setMatch("{\"path\":\"path2\"}");

		Mockito.when(repository.findAll()).thenReturn(List.of(hook1, hook2));

		final var match1 = new HookMatch();
		match1.setPath("path1");
		Mockito.when(objectMapper.readValue("{\"path\":\"path1\"}", HookMatch.class)).thenReturn(match1);

		final var match2 = new HookMatch();
		match2.setPath("path2");
		Mockito.when(objectMapper.readValue("{\"path\":\"path2\"}", HookMatch.class)).thenReturn(match2);

		final var result = hookConfiguration.findAll();

		Assertions.assertEquals(2, result.size());
		Assertions.assertTrue(result.keySet().stream().anyMatch(p -> p.pattern().equals("path1")));
		Assertions.assertTrue(result.keySet().stream().anyMatch(p -> p.pattern().equals("path2")));

		final var hooks1 = result.entrySet().stream().filter(e -> e.getKey().pattern().equals("path1")).findFirst().get().getValue();
		Assertions.assertEquals(1, hooks1.size());
		Assertions.assertEquals("hook1", hooks1.getFirst().getName());
		Assertions.assertEquals("command1", hooks1.getFirst().getCommand());
		Assertions.assertEquals("wd1", hooks1.getFirst().getWorkingDirectory());
		Assertions.assertEquals(100, hooks1.getFirst().getTimeout());
		Assertions.assertEquals(List.of("arg1"), hooks1.getFirst().getInject());
		Assertions.assertEquals(10, hooks1.getFirst().getDelay());

		final var hooks2 = result.entrySet().stream().filter(e -> e.getKey().pattern().equals("path2")).findFirst().get().getValue();
		Assertions.assertEquals(1, hooks2.size());
		Assertions.assertEquals("hook2", hooks2.getFirst().getName());
		// Defaults
		Assertions.assertEquals(10, hooks2.getFirst().getTimeout()); // Default 10
		Assertions.assertEquals(1, hooks2.getFirst().getDelay()); // Default 1
	}

	@Test
	void findAllInvalidJson() throws IOException {
		final var hook3 = new SystemHook(); // Invalid JSON
		hook3.setName("hook3");
		hook3.setMatch("{invalid}");

		Mockito.when(repository.findAll()).thenReturn(List.of(hook3));
		final var exc = Mockito.mock(JsonProcessingException.class);
		Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.eq(HookMatch.class))).thenThrow(exc);

		final var result = hookConfiguration.findAll();
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	void findAllInvalidPattern() throws IOException {
		final var hook4 = new SystemHook(); // Invalid Pattern
		hook4.setName("hook4");
		hook4.setMatch("{\"path\":\"(\"}");

		Mockito.when(repository.findAll()).thenReturn(List.of(hook4));

		final var match4 = new HookMatch();
		match4.setPath("(");
		Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.eq(HookMatch.class))).thenReturn(match4);

		final var result = hookConfiguration.findAll();
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	void process() {
		final var exchange = Mockito.mock(Exchange.class);
		final var principal = Mockito.mock(Principal.class);
		final var response = new Object();

		final var hp1 = getSystemHookParse();

		final var hp2 = new org.ligoj.bootstrap.core.resource.filter.SystemHookParse();
		hp2.setName("hook2");
		final var match2 = new HookMatch();
		match2.setPath("path/.*");
		match2.setMethod("POST"); // Wrong method
		hp2.setMatchObject(match2);

		final var hp3 = new org.ligoj.bootstrap.core.resource.filter.SystemHookParse();
		hp3.setName("hook3");
		final var match3 = new HookMatch();
		match3.setPath("other/.*"); // Wrong path
		hp3.setMatchObject(match3);

		final var hp4 = new org.ligoj.bootstrap.core.resource.filter.SystemHookParse();
		hp4.setName("hook4");
		final var match4 = new HookMatch();
		match4.setPath("path/.*");
		match4.setMethod(null); // Any method
		hp4.setMatchObject(match4);

		final var patterns = Map.of(
				Pattern.compile("path/.*"), List.of(hp1, hp2, hp4),
				Pattern.compile("other/.*"), List.of(hp3)
		);

		Mockito.when(self.findAll()).thenReturn(patterns);

		final var counter = new AtomicInteger();
		final BiConsumer<SystemHook, HookProcessRunnable> processor = (h, r) -> {
			counter.incrementAndGet();
			Assertions.assertNotNull(r);
		};

		hookConfiguration.process(exchange, "GET", "path/foo", principal, response, h -> true, processor);

		Assertions.assertEquals(2, counter.get()); // hook1 and hook4 should match
	}

	private static @NonNull SystemHookParse getSystemHookParse() {
		final var hook1 = new SystemHook();
		hook1.setName("hook1");
		hook1.setMatch("{\"path\":\"path/.*\", \"method\":\"GET\"}");
		final var match1 = new HookMatch();
		match1.setPath("path/.*");
		match1.setMethod("GET");

		// We need to mock the parsed hook
		// Since parseHook is private, we can't easily mock it if we call findAll on the real object.
		// But process calls self.findAll().
		// So we can mock self.findAll() to return prepared parsed hooks.

		// We need SystemHookParse objects
		final var hp1 = new SystemHookParse();
		hp1.setName("hook1");
		hp1.setMatchObject(match1);
		return hp1;
	}

	@Test
	void processException() {
		Mockito.when(self.findAll()).thenThrow(new RuntimeException("Simulated error"));
		final BiConsumer<SystemHook, HookProcessRunnable> processor = Mockito.mock(BiConsumer.class);
		hookConfiguration.process(null, null, null, null, null, null, processor);
		Mockito.verify(processor, Mockito.never()).accept(Mockito.any(), Mockito.any());
	}

	@Test
	void filterUnSafe() {
		// This is tested via process(), but we can add a specific test if needed.
		// process() calls filterUnSafe() inside a try-catch.
		// If we want to test filterUnSafe logic specifically without try-catch, we can call it directly (it's package-private).
		
		final var exchange = Mockito.mock(Exchange.class);
		final var principal = Mockito.mock(Principal.class);
		final var response = new Object();

		final var hp1 = new org.ligoj.bootstrap.core.resource.filter.SystemHookParse();
		hp1.setName("hook1");
		final var match1 = new HookMatch();
		match1.setPath("path");
		hp1.setMatchObject(match1);

		Mockito.when(self.findAll()).thenReturn(Map.of(Pattern.compile("path"), List.of(hp1)));

		final var counter = new AtomicInteger();
		hookConfiguration.filterUnSafe(exchange, "GET", "path", principal, response, h -> false, (h, r) -> counter.incrementAndGet());
		
		Assertions.assertEquals(0, counter.get()); // Filter returned false
	}
}
