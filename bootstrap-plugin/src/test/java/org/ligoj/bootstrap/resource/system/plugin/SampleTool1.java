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
@Path("mock/sample1")
@Component
public class SampleTool1 implements FeaturePlugin {

	@Override
	public String getKey() {
		return "service:sample:tool1";
	}
	@GET
	public String test() {
		return "Hello";
	}

}
