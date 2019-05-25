/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration for some boot components.
 */
@TestConfiguration
public class AppTestConfiguration {

	/**
	 * Tested spring boot bean with a restart endpoint access.
	 * 
	 * @return A spring boot bean with a restart endpoint access.
	 */
	@Bean
	public RestartEndpoint mockRestartEndpoint() {
		return Mockito.mock(RestartEndpoint.class);
	}
}
