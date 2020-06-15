/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.springframework.stereotype.Component;

/**
 * Test of {@link AfterJpaBeforeSpringDataListener}
 */
@Component
class SomeAfterJpaBeforeSpringDataListener implements AfterJpaBeforeSpringDataListener{

	@Override
	public void callback() {
		// nothing to do for this test
	}

}
