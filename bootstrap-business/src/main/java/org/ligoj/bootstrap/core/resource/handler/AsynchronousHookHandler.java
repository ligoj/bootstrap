/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.apache.cxf.annotations.Provider;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous hook execution handler.
 * #see <a href="https://cxf.apache.org/docs/interceptors.html">CXF Interceptors</a>
 */
@Slf4j
@org.apache.cxf.annotations.Provider(value = Provider.Type.InInterceptor, scope = Provider.Scope.Server)
public class AsynchronousHookHandler extends AbstractPhaseInterceptor<Message> {

	@Autowired
	protected HookConfiguration hookConfiguration;

	/**
	 * Default constructor.
	 */
	public AsynchronousHookHandler() {
		super(Phase.POST_INVOKE);
	}

	@Override
	public void handleMessage(final Message message) {
		final var request = (SecurityContextHolderAwareRequestWrapper) message.get("HTTP.REQUEST");
		final var exchange = message.getExchange();
		final var principal = request.getUserPrincipal();
		final var path = Strings.CS.removeStart(request.getPathInfo(), "/");
		final var status = (Integer) exchange.getOutMessage().get("org.apache.cxf.message.Message.RESPONSE_CODE");
		if (status >= 200 && status < 300) {
			final var responseList = exchange.getOutMessage().getContent(List.class);
			final var response = responseList.isEmpty() ? null : responseList.getFirst();
			hookConfiguration.process(exchange, request.getMethod(), path, principal, response,
					hook -> hook.getDelay() == 0,
					(hook, runnable) -> CompletableFuture.delayedExecutor(hook.getDelay(), TimeUnit.SECONDS).execute(runnable));
		}
	}

}
