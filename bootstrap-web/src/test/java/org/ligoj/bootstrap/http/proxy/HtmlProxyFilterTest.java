package org.ligoj.bootstrap.http.proxy;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * HTML proxying test of HtmlProxyFilter class ..
 */
public class HtmlProxyFilterTest {

	/**
	 * Test forward without locale.
	 */
	@Test
	public void testUseCaseForwardNoLocale() throws IOException, ServletException {
		checkForwardTo("/index.html", "/index-ltr" + System.getProperty(HtmlProxyFilter.APP_ENV, "") + ".html", null);
	}

	/**
	 * Test forward from root URL.
	 */
	@Test
	public void testUseCaseForwardRoot() throws IOException, ServletException {
		checkForwardTo("/", "/index-ltr" + System.getProperty(HtmlProxyFilter.APP_ENV, "") + ".html", null);
	}

	/**
	 * Test forward from not index/login URL.
	 */
	@Test
	public void testUseCaseForwardNotRoot() throws IOException, ServletException {
		checkForwardTo("/any.html", "/any.html", null);
	}

	/**
	 * Test forward from login URL.
	 */
	@Test
	public void testUseCaseForwardLogin() throws IOException, ServletException {
		checkForwardTo("/login.html", "/login" + System.getProperty(HtmlProxyFilter.APP_ENV, "") + ".html", null);
	}

	/**
	 * Test forward from root, without context URL.
	 */
	@Test
	public void testUseCaseForwardRoot2() throws IOException, ServletException {
		checkForwardTo("", "/index-ltr" + System.getProperty(HtmlProxyFilter.APP_ENV, "") + ".html", null);
	}

	/**
	 * Test forward with defined LTR locale.
	 */
	@Test
	public void testUseCaseForwardLTR() throws IOException, ServletException {
		checkForwardTo("/index.html", "/index-ltr" + System.getProperty(HtmlProxyFilter.APP_ENV, "") + ".html", Locale.FRENCH);
	}

	/**
	 * Test forward with defined LTR locale.
	 */
	@Test
	public void testUseCaseForwardRTL() throws IOException, ServletException {
		checkForwardTo("/index.html", "/index-rtl" + System.getProperty(HtmlProxyFilter.APP_ENV, "") + ".html", new Locale("ar"));
	}

	/**
	 * Test use case forward.
	 */
	private void checkForwardTo(final String from, final String to, final Locale locale) throws IOException, ServletException {
		final HtmlProxyFilter htmlProxyFilter = new HtmlProxyFilter();
		setup(htmlProxyFilter);

		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(request.getServletPath()).thenReturn(from);
		final RequestDispatcher requestDispatcher = Mockito.mock(RequestDispatcher.class);
		Mockito.when(request.getRequestDispatcher(to)).thenReturn(requestDispatcher);
		Mockito.when(request.getLocale()).thenReturn(locale == null ? Locale.getDefault() : locale);
		htmlProxyFilter.doFilter(request, response, null);
		Mockito.verify(requestDispatcher, Mockito.atLeastOnce()).forward(request, response);
		Mockito.validateMockitoUsage();
	}

	/**
	 * Test no forward for home request.
	 */
	@Test
	public void testHomeForward1() throws IOException, ServletException {
		checkForwardTo("/", "/index-ltr" + System.getProperty(HtmlProxyFilter.APP_ENV, "") + ".html", null);
	}

	/**
	 * Test no forward for home request.
	 */
	@Test
	public void testHomeForward2() throws IOException, ServletException {
		checkForwardTo("", "/index-ltr" + System.getProperty(HtmlProxyFilter.APP_ENV, "") + ".html", null);
	}

	private void setup(final HtmlProxyFilter htmlProxyFilter) {
		// For coverage...
		htmlProxyFilter.init(null);

		// For coverage...
		htmlProxyFilter.destroy();

	}

}