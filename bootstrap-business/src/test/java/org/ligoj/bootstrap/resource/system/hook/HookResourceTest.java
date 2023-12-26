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
		em.persist(newHook());
		final var all = resource.findAll(newUriInfo());
		final var first = all.getData().getFirst();
		Assertions.assertEquals("hook1", first.getName());
		Assertions.assertEquals("ls", first.getCommand());
		Assertions.assertEquals("{\"path\":\"foo/bar\"}", first.getMatch());
		Assertions.assertNull(first.getMatchObject());
	}

	private SystemHook newHook() {
		final var hook = new SystemHook();
		hook.setName("hook1");
		hook.setCommand("ls");
		hook.setWorkingDirectory(".");
		hook.setMatch("{\"path\":\"foo/bar\"}");
		return hook;
	}


	@Test
	void create() {
		final var hook = newHook();
		resource.create(hook);
		Assertions.assertEquals("hook1", resource.findAll(newUriInfo()).getData().getFirst().getName());
	}
	@Test
	void delete() {
		final var hook = newHook();
		em.persist(hook);
		resource.delete(hook.getId());
		Assertions.assertTrue(resource.findAll(newUriInfo()).getData().isEmpty());
	}
}
