/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.eclipse.jetty.util.resource;

import java.util.List;

/**
 * Dummy implementation required by a poor design of jetty sever configuration.
 */
public class VisibleCombinedResource {
	/**
	 * Simple override of CombinedResource#combine
	 * @param resources The combined resources.
	 * @return The combined resources.
	 */
	public static Resource combine(Resource... resources) {
		return CombinedResource.combine(List.of(resources));
	}

}
