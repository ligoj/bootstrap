/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.ligoj.bootstrap.core.plugin.FeaturePlugin;
import org.springframework.stereotype.Component;

/**
 * Sample tool for test.
 */
@Path("mock/sample2")
@Component
public class SampleTool2 implements FeaturePlugin {

	@Override
	public String getKey() {
		return "service:sample:tool2";
	}

	@GET
	public String test() {
		return "Hello";
	}

}
