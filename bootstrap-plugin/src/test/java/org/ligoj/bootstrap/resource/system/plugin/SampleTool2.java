/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import jakarta.ws.rs.GET;
import org.springframework.stereotype.Component;

/**
 * Sample tool for test.
 */
@Component
public class SampleTool2 extends SampleTool1 {

	@Override
	public String getKey() {
		return "service:sample:tool2";
	}

	@GET
	public String test() {
		return "Hello";
	}

}
