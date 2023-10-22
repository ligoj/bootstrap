/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Integration tests
 */
@Suite
@SelectPackages({"org.ligoj.bootstrap"})
@IncludeClassNamePatterns({"^.*IT$"})
@ExcludeClassNamePatterns({"^.*(Abstract.*|.*Test)$"})
public class ITSuite {
}