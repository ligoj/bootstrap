package org.ligoj.bootstrap.http.proxy;

import java.awt.ComponentOrientation;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Filter able to mask the HTML extension from the URL, and forward to the master HTML file as necessary.
 */
public class HtmlProxyFilter implements Filter {

	/**
	 * System property name used to determine the runtime level.
	 */
	public static final String APP_ENV = "app-env";

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		final HttpServletResponse hresponse = (HttpServletResponse) response;

		// Force encoding and IE compatibility
		hresponse.setHeader("X-UA-Compatible", "IE=edge");

		// Disable cache for these main pages
		hresponse.setHeader("Cache-Control", "no-cache");
		hresponse.setHeader("Expires", "0");

		// Forward to the real resource : orientation and optimization according to the current environment
		final String baseName = getBaseName(request);
		request.getRequestDispatcher("/" + baseName + getOptimizedSuffix(request, baseName) + ".html").forward(request, response);
	}

	/**
	 * Return the base name of resource from the request.
	 */
	private String getBaseName(final ServletRequest request) {
		final String servletPath = ((HttpServletRequest) request).getServletPath();
		final String base = StringUtils.removeStart(servletPath, "/");
		return getBaseName(base.isEmpty() ? "index.html" : base);
	}

	/**
	 * Extract the base name from the Servlet path.
	 */
	private String getBaseName(final String servletPath) {
		return FilenameUtils.removeExtension(servletPath);
	}

	/**
	 * Return the optimized suffix corresponding to the given base name.
	 */
	private String getOptimizedSuffix(final ServletRequest request, final String baseName) {
		return "index".equals(baseName) ? getOrientationSuffix(request) + System.getProperty(APP_ENV, "") : "login".equals(baseName) ? System
				.getProperty(APP_ENV, "") : "";
	}

	/**
	 * Return the orientation suffix from the locale guess from the request.
	 */
	private String getOrientationSuffix(final ServletRequest request) {
		return ComponentOrientation.getOrientation(request.getLocale()).isLeftToRight() ? "-ltr" : "-rtl";
	}

	@Override
	public void init(final FilterConfig arg0) {
		// Nothing to do
	}

	@Override
	public void destroy() {
		// Nothing to do
	}

}
