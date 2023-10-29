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
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.dao.system.SystemHookRepository;
import org.ligoj.bootstrap.model.system.HookMatch;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.hook.HookProcessRunnable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Principal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * ContainerResponseFilter resource test, includes {@link HookResponseFilter}
 */
@ExtendWith(SpringExtension.class)
class HookResponseFilterTest extends AbstractBootTest {

	@Autowired
	private HookResponseFilter filter;
	@Autowired
	private SystemHookRepository repository;

	private final AtomicBoolean executed = new AtomicBoolean(false);
	private final List<SystemHook> hooks = new ArrayList<>();
	private final HookResponseFilter filterMock = new HookResponseFilter() {
		@Override
		void execute(final HookProcessRunnable runnable) {
			executed.set(true);
		}

		@Override
		public Map<Pattern, List<SystemHook>> findAll() {
			return Map.of(Pattern.compile("foo/bar.+"), hooks);
		}
	};

	@BeforeEach
	void setup() {
		executed.set(false);
		hooks.clear();
	}

	@Test
	void findAll() {
		final var hook = new SystemHook();
		hook.setName("hook1");
		hook.setMatch("{\"path\":\"foo/bar.+\"}");
		hook.setWorkingDirectory("./home");
		hook.setCommand("any");
		repository.saveAndFlush(hook);

		final var hook2 = new SystemHook();
		hook2.setName("hook2");
		hook2.setMatch("{\"path\":\"foo/bar.+\"}");
		hook2.setWorkingDirectory("./home2");
		hook2.setCommand("any2");
		repository.saveAndFlush(hook2);

		// This hook will not be considered
		final var hookBadJson = new SystemHook();
		hookBadJson.setName("hook_bad_json");
		hookBadJson.setMatch("{_invalid_json_}");
		hookBadJson.setWorkingDirectory("./home3");
		hookBadJson.setCommand("any3");
		repository.saveAndFlush(hookBadJson);

		final var all = filter.findAll();
		Assertions.assertEquals(1, all.size());
		final var entry = all.entrySet().stream().findFirst().get();
		Assertions.assertEquals("foo/bar.+", entry.getKey().pattern());
		Assertions.assertEquals(2, entry.getValue().size());
		Assertions.assertEquals("hook1", entry.getValue().getFirst().getName());
		Assertions.assertEquals("hook2", entry.getValue().getLast().getName());
		Assertions.assertEquals("any", entry.getValue().getFirst().getCommand());
		Assertions.assertEquals("{\"path\":\"foo/bar.+\"}", entry.getValue().getFirst().getMatch());
		Assertions.assertEquals("./home", entry.getValue().getFirst().getWorkingDirectory());

		// Coverage
		final var match = new HookMatch();
		match.setPath("foo");
		Assertions.assertEquals("foo", match.getPath());

	}

	@Test
	void execute() {
		new HookResponseFilter().execute(new HookProcessRunnable(null, null, null, null, null, null, null) {
			@Override
			public void run() {
				executed.set(true);
			}
		});
		Awaitility.waitAtMost(Duration.ofSeconds(2)).until(executed::get);
	}

	@Test
	void filterNoMatchStatus100() {
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(100);
		filterMock.filter(null, responseContext);
		Assertions.assertFalse(executed.get());
	}

	@Test
	void filterNoMatchStatus400() {
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(400);
		filterMock.filter(null, responseContext);
		Assertions.assertFalse(executed.get());
	}

	@Test
	void filterNoMatch() {
		final var requestContext = Mockito.mock(ContainerRequestContextImpl.class);
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		final var uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(responseContext.getStatus()).thenReturn(200);
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(uriInfo.getPath()).thenReturn("any");
		filterMock.filter(requestContext, responseContext);
		Assertions.assertFalse(executed.get());
	}

	@Test
	void filterMatchUser() {
		filterMatch(null);
	}

	@Test
	void filterMatchNoUser() {
		final var principal = Mockito.mock(Principal.class);
		Mockito.when(principal.getName()).thenReturn("junit");
		filterMatch(principal);
	}

	private void filterMatch(Principal principal) {
		final var requestContext = Mockito.mock(ContainerRequestContextImpl.class);
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		final var uriInfo = Mockito.mock(UriInfo.class);
		final var message = Mockito.mock(Message.class);
		final var exchange = Mockito.mock(Exchange.class);
		final var securityContext = Mockito.mock(SecurityContext.class);

		Mockito.when(responseContext.getStatus()).thenReturn(200);
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(requestContext.getMessage()).thenReturn(message);
		Mockito.when(requestContext.getSecurityContext()).thenReturn(securityContext);
		Mockito.when(securityContext.getUserPrincipal()).thenReturn(principal);
		Mockito.when(message.getExchange()).thenReturn(exchange);
		Mockito.when(uriInfo.getPath()).thenReturn("foo/bar1");
		filterMock.filter(requestContext, responseContext);
		Assertions.assertTrue(executed.get());
	}

}