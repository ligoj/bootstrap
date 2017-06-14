package org.ligoj.bootstrap.core.dao;

import javax.transaction.Transactional;

import org.ligoj.bootstrap.AbstractJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public abstract class AbstractBootTest extends AbstractJpaTest {

	// Annotation holder only
}