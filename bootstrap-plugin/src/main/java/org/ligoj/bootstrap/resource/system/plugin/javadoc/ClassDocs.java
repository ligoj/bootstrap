/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.javadoc;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Javadoc's class structure.
 */
@AllArgsConstructor
class ClassDocs {

	@Getter
	private final String classDoc;

	@Getter
	private final String classInfo;

	private final Map<Method, MethodDocs> methodDocs = new ConcurrentHashMap<>();

	public MethodDocs getMethodDocs(final Method method) {
		return methodDocs.get(method);
	}

	public void addMethodDocs(final Method method, final MethodDocs doc) {
		methodDocs.putIfAbsent(method, doc);
	}
}