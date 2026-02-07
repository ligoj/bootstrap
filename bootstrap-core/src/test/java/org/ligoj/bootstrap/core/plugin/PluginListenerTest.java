/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemPlugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class of {@link PluginListener}
 */
class PluginListenerTest {

	@Test
    void installIsTrueByDefault() {
		Assertions.assertTrue(new PluginListener() {

			@Override
			public Supplier<PluginVo> toVo() {
				return null;
			}

			@Override
			public void fillVo(SystemPlugin p, FeaturePlugin feature, PluginVo vo) {
				// Nothing to do
			}

			@Override
			public void configure(FeaturePlugin plugin, SystemPlugin entity) {
				// Nothing to do
			}
		}.install(null));
	}

	@Test
    void pluginVoSerialize() throws JsonProcessingException {
        var pluginVo = new PluginVo();
		pluginVo.setDeleted(false);
		pluginVo.setLocation("L");
		pluginVo.setLatestLocalVersion("1");
		pluginVo.setNewVersion("2");
		pluginVo.setName("N");
		pluginVo.setId("I");
        var plugin = new SystemPlugin();
		plugin.setKey("K");
		plugin.setArtifact("A");
		plugin.setType("T");
		plugin.setVersion("V");
		plugin.setBasePackage("P");
		pluginVo.setPlugin(plugin);
		pluginVo.setVendor("V");
		Assertions.assertEquals("{\"id\":\"I\",\"name\":\"N\",\"plugin\":{\"id\":null,"
				+ "\"createdBy\":null,\"createdDate\":null,\"lastModifiedBy\":null,\"lastModifiedDate\":null,"
				+ "\"creationContext\":null,\"metadata\":null,\"version\":\"V\",\"key\":\"K\",\"artifact\":\"A\",\"basePackage\":\"P\",\"type\":\"T\"},"
				+ "\"vendor\":\"V\",\"location\":\"L\",\"newVersion\":\"2\",\"latestLocalVersion\":\"1\",\"deleted\":false}",
				new ObjectMapper().writeValueAsString(pluginVo));
	}

}
