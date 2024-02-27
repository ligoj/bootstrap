/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.javadoc;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.doc.DocumentationProvider;

import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extract JavaDoc from provider jar URLs.
 */
public class JavadocDocumentationProvider implements DocumentationProvider {
	private static final String MARKUP_OPERATION = "<section class=\"detail\" id=\"";
	private static final String MARKUP_PARAMETER = "<dt>Parameters:</dt>";
	private static final String MARKUP_BLOCK = "<div class=\"block\">";
	private static final String MARKUP_BLOCK_END = "</div>";

	private final ConcurrentHashMap<String, ClassDocs> docs = new ConcurrentHashMap<>();

	private final URLClassLoader javaDocLoader;

	JavadocDocumentationProvider(URLClassLoader javaDocLoader) {
		this.javaDocLoader = javaDocLoader;
	}

	@Override
	public String getClassDoc(ClassResourceInfo cri) {
		try {
			var doc = getClassDocInternal(cri.getServiceClass());
			if (doc == null) {
				return null;
			}
			return doc.getClassInfo();
		} catch (Exception ex) {
			// ignore
		}
		return null;
	}

	@Override
	public String getMethodDoc(OperationResourceInfo ori) {
		try {
			var doc = getOperationDocInternal(ori);
			if (doc == null) {
				return null;
			}
			return doc.getMethodInfo();
		} catch (Exception ex) {
			// ignore
		}
		return null;
	}

	@Override
	public String getMethodResponseDoc(OperationResourceInfo ori) {
		try {
			var doc = getOperationDocInternal(ori);
			if (doc == null) {
				return null;
			}
			return doc.getReturnInfo();
		} catch (Exception ex) {
			// ignore
		}
		return null;
	}

	@Override
	public String getMethodParameterDoc(OperationResourceInfo ori, int paramIndex) {
		try {
			var doc = getOperationDocInternal(ori);
			if (doc == null) {
				return null;
			}
			var params = doc.getParamInfo();
			if (paramIndex < params.size()) {
				return params.get(paramIndex);
			}
			return null;
		} catch (Exception ex) {
			// ignore
		}
		return null;
	}

	private Class<?> getPathAnnotatedClass(Class<?> cls) {
		if (cls.getAnnotation(jakarta.ws.rs.Path.class) != null) {
			return cls;
		}
		if (cls.getSuperclass() != null && cls.getSuperclass().getAnnotation(jakarta.ws.rs.Path.class) != null) {
			return cls.getSuperclass();
		}
		for (var i : cls.getInterfaces()) {
			if (i.getAnnotation(jakarta.ws.rs.Path.class) != null) {
				return i;
			}
		}
		return cls;
	}

	private ClassDocs getClassDocInternal(Class<?> cls) throws Exception {
		var annotatedClass = getPathAnnotatedClass(cls);
		var resource = annotatedClass.getName().replace(".", "/") + ".html";
		var classDocs = docs.get(resource);
		if (classDocs == null) {
			var loader = javaDocLoader != null ? javaDocLoader : annotatedClass.getClassLoader();
			var resourceStream = loader.getResourceAsStream(resource);
			if (resourceStream != null) {
				var doc = IOUtils.readStringFromStream(resourceStream);
				var qualifier = annotatedClass.isInterface() ? "Interface" : "Class";
				var classMarker = qualifier + " " + annotatedClass.getSimpleName();
				int index = doc.indexOf(classMarker);
				if (index != -1) {
					var classInfo = getJavaDocText(doc, MARKUP_BLOCK, "Method Summary", index + classMarker.length(), MARKUP_BLOCK_END);
					classDocs = new ClassDocs(doc, classInfo);
					docs.putIfAbsent(resource, classDocs);
				}
			}
		}
		return classDocs;
	}

	private MethodDocs addParamDoc(String operDoc) {
		var operInfo = getJavaDocText(operDoc, MARKUP_BLOCK, MARKUP_OPERATION, 0, MARKUP_BLOCK_END);
		String responseInfo = null;
		var paramDocs = new LinkedList<String>();
		var returnsIndex = operDoc.indexOf("<dt>Returns:</dt>");
		if (returnsIndex != -1) {
			responseInfo = getJavaDocText(operDoc, "<dd>", "<__>", returnsIndex + 8, "</dd>");
		}
		var paramIndex = operDoc.indexOf(MARKUP_PARAMETER);
		if (paramIndex != -1) {
			var paramString = operDoc.substring(paramIndex + MARKUP_PARAMETER.length(), Math.max(returnsIndex, operDoc.length()));
			var codeIndex = 0;
			var parameterInfo = getJavaDocText(paramString, "<dd>", "<dt>", codeIndex, "</dd>");
			while (parameterInfo != null) {
				paramDocs.add(parameterInfo.split("- ")[1].trim());
				codeIndex += parameterInfo.length();
				parameterInfo = getJavaDocText(paramString, "<dd>", "<dt>", codeIndex, "</dd>");
			}

		}
		return new MethodDocs(operInfo, paramDocs, responseInfo);
	}

	private MethodDocs getOperationDocInternal(OperationResourceInfo ori) throws Exception {
		var method = ori.getAnnotatedMethod() == null ? ori.getMethodToInvoke() : ori.getAnnotatedMethod();
		var classDoc = getClassDocInternal(method.getDeclaringClass());
		if (classDoc == null) {
			return null;
		}
		var signatureNoClass = org.apache.commons.lang3.StringUtils.substringAfter(method.toString(), ori.getClassResourceInfo().getResourceClass().getName()).substring(1);
		var mDocs = classDoc.getMethodDocs(method);
		if (mDocs == null) {
			var operDoc = getJavaDocText(classDoc.getClassDoc(), MARKUP_OPERATION + signatureNoClass, "<__>", 0, "</section>");
			mDocs = addParamDoc(operDoc);
			classDoc.addMethodDocs(method, mDocs);
		}

		return mDocs;
	}

	protected static String normalize(String doc) {
		return StringUtils.capitalize(StringUtils.trim(removeUselessChars(StringUtils.trim(doc))));
	}

	/**
	 * Remove useless chars from documentation lines.
	 */
	protected static String removeUselessChars(String doc) {
		return StringUtils.removeEnd(doc, ".");
	}

	private String getJavaDocText(String doc, String tag, String notAfterTag, int index, String subNext) {
		var tagIndex = doc.indexOf(tag, index);
		if (tagIndex != -1) {
			var notAfterIndex = doc.indexOf(notAfterTag, index);
			if (notAfterIndex == -1 || notAfterIndex > tagIndex) {
				var nextIndex = doc.indexOf(subNext, tagIndex + tag.length());
				if (nextIndex != -1) {
					return normalize(doc.substring(tagIndex + tag.length(), nextIndex));
				}
			}
		}
		return null;
	}
}