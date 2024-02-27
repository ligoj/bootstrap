/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.javadoc;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.MethodDispatcher;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.plugin.SampleTool1;
import org.ligoj.bootstrap.resource.system.plugin.SampleTool2;
import org.ligoj.bootstrap.resource.system.plugin.SampleTool4;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Test class of {@link JavadocDocumentationProvider}.
 */
class JavadocDocumentationProviderTest extends AbstractJavaDocTest {

	private JavadocDocumentationProvider provider;

	@BeforeEach
	void configureProvider() throws IOException {
		final var javadocUrls = newJavadocUrls();
		provider = new JavadocDocumentationProvider(new URLClassLoader(javadocUrls.toArray(new URL[0])));
	}

	@Test
	void getClassDoc() {
		final var cri1 = new ClassResourceInfo(SampleTool1.class);
		cri1.setMethodDispatcher(new MethodDispatcher());
		Assertions.assertEquals("Sample tool for test", provider.getClassDoc(cri1));
	}

	@Test
	void getClassDocSuper() {
		final var cri1 = new ClassResourceInfo(SampleTool2.class);
		cri1.setMethodDispatcher(new MethodDispatcher());
		Assertions.assertEquals("Sample tool for test", provider.getClassDoc(cri1));
	}

	@Test
	void getClassDocInterfaces() {
		final var cri1 = new ClassResourceInfo(SampleTool4.class);
		cri1.setMethodDispatcher(new MethodDispatcher());
		Assertions.assertNull(provider.getClassDoc(cri1));
	}

	@Test
	void getClassDocNoDoc() {
		final var cri1 = new ClassResourceInfo(String.class);
		Assertions.assertNull(provider.getClassDoc(cri1));
	}

	@Test
	void getClassDocError() {
		Assertions.assertNull(provider.getClassDoc(null));
	}

	@Test
	void getMethodDoc() {
		final var cri1 = new ClassResourceInfo(SampleTool1.class);
		final var ori1 = new OperationResourceInfo(MethodUtils.getMatchingMethod(SampleTool1.class, "test1", String.class, SystemUser.class), cri1);
		Assertions.assertEquals("Method doc. Details", provider.getMethodDoc(ori1));
	}


	@Test
	void getMethodDocFromString() {
		var mDoc =  provider.getMethodDoc("""
				<section class="method-details" id="method-detail">
				<section class="detail" id="test1(java.lang.String,org.ligoj.bootstrap.model.system.SystemUser)">
				<div class="block">Method doc. Details.</div>
				<dl class="notes">
				<dt>Parameters:</dt>
				<dd><code>param1</code> - Param1 doc.</dd>
				<dd><code>user</code> - User doc. Details.</dd>
				<dt>Returns:</dt>
				<dd>Return doc</dd>
				</dl>
				</section>
				""");
		Assertions.assertEquals("Method doc. Details",mDoc.getMethodInfo());
		Assertions.assertEquals("Param1 doc",mDoc.getParamInfo().getFirst());
		Assertions.assertEquals("Return doc",mDoc.getReturnInfo());
	}


	@Test
	void getMethodDocFromStringNoReturn() {
		var mDoc =  provider.getMethodDoc("""
				<section class="method-details" id="method-detail">
				<section class="detail" id="test1(java.lang.String,org.ligoj.bootstrap.model.system.SystemUser)">
				<div class="block">Method doc. Details.</div>
				<dl class="notes">
				<dt>Parameters:</dt>
				<dd><code>param1</code> - Param1 doc.</dd>
				<dd><code>user</code> - User doc. Details.</dd>
				</dl>
				</section>
				""");
		Assertions.assertEquals("Method doc. Details",mDoc.getMethodInfo());
		Assertions.assertEquals("Param1 doc",mDoc.getParamInfo().getFirst());
		Assertions.assertNull(mDoc.getReturnInfo());
	}


	@Test
	void getMethodDocFromStringNoParams() {
		var mDoc =  provider.getMethodDoc("""
				<section class="method-details" id="method-detail">
				<section class="detail" id="test1(java.lang.String,org.ligoj.bootstrap.model.system.SystemUser)">
				<div class="block">Method doc. Details.</div>
				<dl class="notes">
				</dl>
				</section>
				""");
		Assertions.assertEquals("Method doc. Details",mDoc.getMethodInfo());
		Assertions.assertTrue(mDoc.getParamInfo().isEmpty());
		Assertions.assertNull(mDoc.getReturnInfo());
	}
	@Test
	void getMethodNoDoc() {
		final var cri1 = new ClassResourceInfo(String.class);
		final var ori1 = new OperationResourceInfo(MethodUtils.getMatchingMethod(String.class, "toString"), cri1);
		Assertions.assertNull(provider.getMethodDoc(ori1));
	}

	@Test
	void getMethodDocError() {
		Assertions.assertNull(provider.getMethodDoc((OperationResourceInfo) null));
	}


	@Test
	void getMethodResponseDoc() {
		final var cri1 = new ClassResourceInfo(SampleTool1.class);
		final var ori1 = new OperationResourceInfo(MethodUtils.getMatchingMethod(SampleTool1.class, "test1", String.class, SystemUser.class), cri1);
		Assertions.assertEquals("Return doc", provider.getMethodResponseDoc(ori1));
	}


	@Test
	void getMethodResponseNoDoc() {
		final var cri1 = new ClassResourceInfo(String.class);
		final var ori1 = new OperationResourceInfo(MethodUtils.getMatchingMethod(String.class, "toString"), cri1);
		Assertions.assertNull(provider.getMethodResponseDoc(ori1));
	}

	@Test
	void getMethodResponseDocError() {
		Assertions.assertNull(provider.getMethodResponseDoc(null));
	}


	@Test
	void getMethodParameterDoc() {
		final var cri1 = new ClassResourceInfo(SampleTool1.class);
		final var ori1 = new OperationResourceInfo(MethodUtils.getMatchingMethod(SampleTool1.class, "test1", String.class, SystemUser.class), cri1);
		Assertions.assertEquals("Param1 doc", provider.getMethodParameterDoc(ori1, 0));
		Assertions.assertEquals("User doc. Details", provider.getMethodParameterDoc(ori1, 1));
		Assertions.assertNull(provider.getMethodParameterDoc(ori1, 2));
	}


	@Test
	void getMethodParameterNoDoc() {
		final var cri1 = new ClassResourceInfo(String.class);
		final var ori1 = new OperationResourceInfo(MethodUtils.getMatchingMethod(String.class, "getBytes"), cri1);
		Assertions.assertNull(provider.getMethodParameterDoc(ori1, 0));
	}

	@Test
	void getMethodParameterDocError() {
		Assertions.assertNull(provider.getMethodParameterDoc(null, 0));
	}

	@Test
	void normalize() {
		Assertions.assertEquals("Test", JavadocDocumentationProvider.normalize("Test"));
		Assertions.assertEquals("Test sample", JavadocDocumentationProvider.normalize("   test sample . "));
		Assertions.assertEquals("", JavadocDocumentationProvider.normalize(" "));
		Assertions.assertNull(JavadocDocumentationProvider.normalize(null));
		Assertions.assertEquals("", JavadocDocumentationProvider.normalize("."));
	}

	@Test
	void removeUselessChars() {
		Assertions.assertEquals("Test", JavadocDocumentationProvider.removeUselessChars("Test"));
		Assertions.assertEquals("test", JavadocDocumentationProvider.removeUselessChars("   test . "));
		Assertions.assertEquals("", JavadocDocumentationProvider.removeUselessChars(" "));
		Assertions.assertNull(JavadocDocumentationProvider.removeUselessChars(null));
		Assertions.assertEquals("", JavadocDocumentationProvider.removeUselessChars("."));
	}

}
