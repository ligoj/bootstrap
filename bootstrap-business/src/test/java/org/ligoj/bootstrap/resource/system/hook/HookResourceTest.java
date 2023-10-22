/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.hook;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link HookResource}
 */
@ExtendWith(SpringExtension.class)
class HookResourceTest extends AbstractBootTest {

	@Autowired
	private HookResource resource;

	@Test
	void findAll() {
		final var hook = new SystemHook();
		hook.setName("hook1");
		hook.setCommand("ls");
		hook.setMatch("{}");
		hook.setMatch("{}");
		resource.create(hook);
		final var  all = resource.findAll(newUriInfo());
		final var first = all.getData().getFirst();
		Assertions.assertEquals("hook1",first.getName());
		Assertions.assertEquals("ls",first.getCommand());
		Assertions.assertEquals("{}",first.getMatch());
		Assertions.assertNull(first.getMatchObject());
		hook.setName("hook2");
		resource.create(hook);
		Assertions.assertEquals("hook2",resource.findAll(newUriInfo()).getData().getFirst().getName());
		resource.delete(hook.getId());
		Assertions.assertTrue(resource.findAll(newUriInfo()).getData().isEmpty());
	}

}
