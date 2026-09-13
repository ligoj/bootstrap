package org.ligoj.bootstrap.core.plugin;

/**
 * Class shadowed by a plug-in jar in {@code PluginsClassLoaderTest#childFirstClassLoading}: the parent (test
 * class-path) flavor.
 */
public class ShadowedSample {
	/** Origin marker read by reflection. */
	public static final String ORIGIN = "parent";
}
