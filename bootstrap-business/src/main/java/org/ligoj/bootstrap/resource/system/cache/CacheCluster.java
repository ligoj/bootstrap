package org.ligoj.bootstrap.resource.system.cache;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CacheCluster {

	private String id;
	private String state;
	private List<CacheNode> members;
}
