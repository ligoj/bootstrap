/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

/**
 * Test class of {@link CurlCacheToken}
 */
class CurlCacheTokenTest {

	private CurlCacheToken cacheToken;

	@BeforeEach
	@AfterEach
	void clearCache() {
		cacheToken = new CurlCacheToken();
		cacheToken.applicationContext = Mockito.mock(ApplicationContext.class);
		Mockito.when(cacheToken.applicationContext.getBean(CurlCacheToken.class)).thenReturn(cacheToken);
	}

	@Test
	void getTokenCacheFailed() {
		final var sync = new Object();
		var counter = new AtomicInteger();
		final UnaryOperator<String> function = k -> {
			counter.incrementAndGet();
			return null;
		};
		Assertions.assertThrows(ValidationJsonException.class,
				() -> cacheToken.getTokenCache(sync, "key", function, 2, ValidationJsonException::new));
		Assertions.assertEquals(2, counter.get());
		Assertions.assertEquals("ok", cacheToken.getTokenCache(sync, "key", k -> {
			if (counter.incrementAndGet() == 4) {
				return "ok";
			}
			return null;
		}, 2, ValidationJsonException::new));
		Assertions.assertEquals(4, counter.get());
	}

	@Test
	void getTokenCache() {
		final var sync = new Object();
		var counter = new AtomicInteger();
		Assertions.assertEquals("ok", cacheToken.getTokenCache(sync, "key", k -> {
			if (counter.incrementAndGet() == 2) {
				return "ok";
			}
			return null;
		}, 2, ValidationJsonException::new));
		Assertions.assertEquals(2, counter.get());
	}
}
