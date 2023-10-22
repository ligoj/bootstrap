package org.eclipse.jetty.util.resource;

import java.util.List;

public class VisibleCombinedResource {
	public static Resource combine(Resource... resources) {
		return CombinedResource.combine(List.of(resources));
	}

}
