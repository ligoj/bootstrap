/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import java.util.function.Supplier;

import org.ligoj.bootstrap.core.plugin.FeaturePlugin;
import org.ligoj.bootstrap.core.plugin.PluginListener;
import org.ligoj.bootstrap.core.plugin.PluginVo;
import org.ligoj.bootstrap.model.system.SystemPlugin;

/**
 * Mock registerable {@link PluginListener} singleton.
 *
 */
public class MockPluginListener implements PluginListener {

	@Override
	public void configure(FeaturePlugin plugin, SystemPlugin entity) {
		// Nothing to do
	}

	@Override
	public Supplier<PluginVo> toVo() {
		return PluginVo::new;
	}

	@Override
	public void fillVo(SystemPlugin p, FeaturePlugin feature, PluginVo vo) {
		// Nothing to do
	}

}
