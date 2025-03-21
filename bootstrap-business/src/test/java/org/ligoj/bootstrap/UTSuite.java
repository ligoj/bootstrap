/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.junit.platform.suite.api.*;

/**
 * Unit tests
 */
@Suite
@SelectPackages("org.ligoj.bootstrap")
@IncludeClassNamePatterns("^.*Test.*$")
@ExcludeClassNamePatterns("^(Abstract.*|.*IT)$")
public class UTSuite {
}