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

	/**
	 * Configure the given plug-in.
	 *
	 * @param plugin
	 *            The plug-in to configure.
	 * @param entity
	 *            The entity.
	 */
	void configure(FeaturePlugin plugin, SystemPlugin entity);

	/**
	 * VO transformer.
	 *
	 * @return VO transformer function.
	 */
	Supplier<PluginVo> toVo();

	/**
	 * Entity to VO function.
	 *
	 * @param plugin
	 *            Plug-in source
	 * @param feature
	 *            Related plug-in entity.
	 * @param vo
	 *            The target VO to fill.
	 */
	void fillVo(SystemPlugin plugin, FeaturePlugin feature, PluginVo vo);

}
