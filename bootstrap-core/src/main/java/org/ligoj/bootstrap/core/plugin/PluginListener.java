/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import java.util.function.Supplier;

import org.ligoj.bootstrap.model.system.SystemPlugin;

/**
 * Plug-in life cycle listener.
 *
 */
public interface PluginListener {

	/**
	 * Inform the plug-in is being installed.
	 *
	 * @return <code>false</code> only to veto this installation.
	 */
	default boolean install(FeaturePlugin feature) {
		return true;
	}

	void configure(FeaturePlugin plugin, SystemPlugin entity);

	Supplier<PluginVo> toVo();

	void fillVo(SystemPlugin p, FeaturePlugin feature, PluginVo vo);

}
