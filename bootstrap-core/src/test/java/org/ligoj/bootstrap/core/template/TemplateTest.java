package org.ligoj.bootstrap.core.template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Test class of {@link TemplateTest}
 */
public class TemplateTest {

	/**
	 * Rule manager for exception.
	 */
	@Rule
	// CHECKSTYLE:OFF -- expected by JUnit
	public ExpectedException thrown = ExpectedException.none();
	// CHECKSTYLE:ON

	private Writer writer;

	private ByteArrayOutputStream bos;

	@Before
	public void initialize() {
		bos = new ByteArrayOutputStream();
		writer = new PrintWriter(bos);
	}

	/**
	 * Simple write empty template.
	 */
	@Test
	public void testWriteEmpty() throws IOException {
		new Template<>("").write(writer, new HashMap<String, Processor<?>>(), null);
		Assert.assertEquals(0, bos.toByteArray().length);
	}

	/**
	 * Not mapped tag.
	 */
	@Test
	public void testWriteNotMappedTag() throws IOException {
		thrown.expectMessage("Not mapped template tag {{any}} found at position 0");
		new Template<>("{{any/}}").write(writer, new HashMap<String, Processor<?>>(), null);
	}

	/**
	 * Not invalid end tag.
	 */
	@Test
	public void testWriteInvalidEnd() throws IOException {
		thrown.expectMessage("Invalid opening tag syntax '{{' without '}}' at position 0");
		new Template<>("{{any/}").write(writer, new HashMap<String, Processor<?>>(), null);
	}

	/**
	 * Not invalid end tag.
	 */
	@Test
	public void testWriteInvalidEndOverlay() throws IOException {
		thrown.expectMessage("Invalid opening tag syntax '{{' without '}}' at position 7");
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("any", new Processor<>(new String[] { "value" }));
		new Template<>("{{any}}{{one... {{/any}}..}}").write(writer, tags, null);
	}

	/**
	 * Missing closing tag.
	 */
	@Test
	public void testWriteNoClosing() throws IOException {
		thrown.expectMessage("Closing tag {{/any}} not found");
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("any", new Processor<>());
		new Template<>("{{any}}..").write(writer, tags, null);
	}

	/**
	 * Null collection.
	 */
	@Test
	public void testWriteNullCollection() throws IOException {
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("any", new Processor<>());
		new Template<>("{{any}}.{{/any}}").write(writer, tags, null);
		Assert.assertEquals(0, bos.toByteArray().length);
	}

	/**
	 * Missing closing tag.
	 */
	@Test
	public void testWriteNoClosingNesting() throws IOException {
		thrown.expectMessage("Closing tag {{/one}} not found");
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("any", new Processor<>(new String[] { "value" }));
		tags.put("one", new Processor<>());
		new Template<>("{{any}}.{{one}}.{{/any}}.{{/one}}").write(writer, tags, null);
	}

	/**
	 * Empty tag name.
	 */
	@Test
	public void testWriteEmptyTag() throws IOException {
		thrown.expectMessage("Empty tag {{}} found at position 0");
		new Template<>("{{}}").write(writer, new HashMap<String, Processor<?>>(), null);
	}

	/**
	 * Too long tag name.
	 */
	@Test
	public void testWriteTooLongTag() throws IOException {
		thrown.expectMessage("Too long (max is 100 tag zzzzzzzzzzzzzzzzzzzzzzzzzzzz...}} found at position 0");
		new Template<>("{{" + StringUtils.repeat('z', 101) + "}}..").write(writer, new HashMap<String, Processor<?>>(), null);
	}

	/**
	 * Bean, without attribute access
	 */
	@Test
	public void testWriteBean() throws IOException {
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("tag", new Processor<>("value"));
		new Template<>("{{tag}}..{{/tag}}").write(writer, tags, null);
		Assert.assertEquals("..", new String(bos.toByteArray(), StandardCharsets.UTF_8));
	}

	/**
	 * Cascaded beans
	 */
	@Test
	public void testWriteCascadedBean() throws IOException {
		final Map<String, Processor<?>> tags = new HashMap<>();
		final SystemUser user = new SystemUser();
		user.setLogin("junit");
		final Set<SystemRoleAssignment> assignments = new HashSet<>();
		final SystemRoleAssignment assignment = new SystemRoleAssignment();
		final SystemRole role = new SystemRole();
		role.setName("myrole");
		assignment.setRole(role);
		assignments.add(assignment);
		user.setRoles(assignments);
		tags.put("user", new Processor<>(user));
		tags.put("username", new BeanProcessor<>(SystemUser.class, "login"));
		tags.put("assignments", new BeanProcessor<>(SystemUser.class, "roles"));
		tags.put("role", new BeanProcessor<>(SystemRoleAssignment.class, "role"));
		tags.put("name", new BeanProcessor<>(SystemRole.class, "name"));
		new Template<>("{{user}}1{{username/}}2{{assignments}}3{{role}}4{{name/}}5{{/role}}6{{/assignments}}7{{/user}}").write(writer, tags, null);
		Assert.assertEquals("1junit234myrole567", new String(bos.toByteArray(), StandardCharsets.UTF_8));
	}

	/**
	 * Nullable bean.
	 */
	@Test
	public void testWriteCascadedNullableBean() throws IOException {
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("user", new Processor<>("junit"));
		new Template<>("0{{user}}1{{user/}}2{{/user}}3").write(writer, tags, null);
		Assert.assertEquals("01junit23", new String(bos.toByteArray(), StandardCharsets.UTF_8));
	}

	/**
	 * Expecting collection or array and got empty {@link ArrayList}
	 */
	@Test
	public void testWriteEmptyCollection() throws IOException {
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("tag", new Processor<>(new ArrayList<>()));
		new Template<>("{{tag}}..{{/tag}}").write(writer, tags, null);
		Assert.assertEquals(0, bos.toByteArray().length);
	}

	/**
	 * Null data
	 */
	@Test
	public void testWriteNullData() throws IOException {
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("tag", new Processor<>());
		new Template<>("{{tag/}}").write(writer, tags, null);
		Assert.assertEquals(0, bos.toByteArray().length);
	}

	/**
	 * Expecting collection or array and got a simple array
	 */
	@Test
	public void testWriteArray() throws IOException {
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("tag", new Processor<>(new String[] { "value" }));
		new Template<>("{{tag}}..{{/tag}}").write(writer, tags, null);
		Assert.assertEquals("..", new String(bos.toByteArray(), StandardCharsets.UTF_8));
	}

	/**
	 * Compete features
	 */
	@Test
	public void testWriteFullFeature() throws IOException {
		final Map<String, Processor<?>> tags = new HashMap<>();
		tags.put("tag", new Processor<>(new String[] { "A", "B" }));
		tags.put("tag-value", new Processor<>());
		tags.put("sub-tag", new Processor<>(Arrays.asList(new String[] { "john", "doe" })));
		tags.put("sub-tag-value", new Processor<>());
		new Template<>("0{{tag}}1{{tag-value/}}2{{sub-tag}}{{sub-tag-value/}}{{/sub-tag}}{{/tag}}3").write(writer, tags, null);
		Assert.assertEquals("01A2johndoe1B2johndoe3", new String(bos.toByteArray(), StandardCharsets.UTF_8));
	}
}
