/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Unit tests
 */
@Suite
@SelectPackages("org.ligoj.bootstrap")
@IncludeClassNamePatterns("^.*Test.*$")
@ExcludeClassNamePatterns("^(Abstract.*|.*IT)$")
public class UTSuite {
}