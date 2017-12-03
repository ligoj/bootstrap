package org.ligoj.bootstrap.core.csv;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.DateUtils;
import org.ligoj.bootstrap.core.resource.TechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Check all CSV to/from simple beans of {@link CsvForBean} utility.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
public class CsvForBeanTest {

	@BeforeClass
	public static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");

		// Fix UTC time zone for this test, since date are compared
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Rule manager for exception.
	 */
	@Rule
	// CHECKSTYLE:OFF -- expected by JUnit
	public ExpectedException thrown = ExpectedException.none();
	// CHECKSTYLE:ON

	@Autowired
	private CsvForBean csvForBean;

	@Test
	public void toCsvEmpty() throws Exception {
		final StringWriter result = new StringWriter();
		csvForBean.toCsv(new ArrayList<DummyEntity>(), DummyEntity.class, result);

		// Check there is only the header
		Assert.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n", result.toString());

		// Only there for coverage
		Wrapper.values();
		Wrapper.valueOf(Wrapper.DOUBLE_QUOTE.name());
	}

	@Test
	public void toCsvEmptyObject() throws Exception {
		final StringWriter result = new StringWriter();
		csvForBean.toCsv(new ArrayList<>(), Object.class, result);
	}

	@Test
	public void toCsvNullProperty() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		final DummyEntity newWine = newWine();
		newWine.setWneCnty(null);
		items.add(newWine);
		csvForBean.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assert.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;;2;3;6;7;8\n", result.toString());
	}

	@Test
	public void toCsvSpecialChars() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		final DummyEntity newWine = newWine();
		newWine.setWneCnty("World, hold on;");
		newWine.setName("Château d\"Yquem");
		items.add(newWine);
		csvForBean.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assert.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;\"Château d\"\"Yquem\";\"World, hold on;\";2;3;6;7;8\n",
				result.toString());
	}

	@Test
	public void toCsv() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		items.add(newWine());
		final StringWriter result = new StringWriter();
		csvForBean.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assert.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;1;2;3;6;7;8\n", result.toString());
	}

	@Test
	public void toBean() throws Exception {
		final List<DummyEntity> items = csvForBean.toBean(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;5;6;7;'8'"));
		Assert.assertEquals(1, items.size());
		assertEquals(newWine(), items.get(0));
	}

	@Test
	public void toBeanNullHeader() throws Exception {
		final List<DummyEntity> items = csvForBean.toBean(DummyEntity.class, new StringReader("name;;;;;;;wneYear\n4;1;2;3;5;6;7;'8'"));
		Assert.assertEquals(1, items.size());
		DummyEntity wine2 = items.get(0);
		Assert.assertEquals("4", wine2.getName());
		Assert.assertEquals(8, wine2.getWneYear().intValue());

		// Ignored headers
		Assert.assertNull(wine2.getWneCnty());
		Assert.assertNull(wine2.getWneDesc());
		Assert.assertNull(wine2.getWneGrpe());
		Assert.assertNull(wine2.getWnePict());
		Assert.assertNull(wine2.getWneRegn());

	}

	@Test
	public void toBeanDate() throws Exception {
		final List<DummyEntity2> items = csvForBean.toBean(DummyEntity2.class,
				new StringReader("dialDate\n2016/05/04\n2016/05/04 12:54:32\n2016/05/04 12:54\n04/05/2016\n04/05/2016 12:54\n04/05/2016 12:54:32"));
		Assert.assertEquals(6, items.size());
		Assert.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(0).getDialDate().toString());
		Assert.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(1).getDialDate().toString());
		Assert.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(2).getDialDate().toString());
		Assert.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(3).getDialDate().toString());
		Assert.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(4).getDialDate().toString());
		Assert.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(5).getDialDate().toString());
	}

	@Test
	public void toBeanLocalDate() throws Exception {
		final List<DummyEntity2> items = csvForBean.toBean(DummyEntity2.class,
				new StringReader("localDate\n2016/05/04\n2016/05/04 12:54:32\n2016/05/04 12:54\n04/05/2016\n04/05/2016 12:54\n04/05/2016 12:54:32"));
		Assert.assertEquals(6, items.size());
		Assert.assertEquals("2016-05-04", items.get(0).getLocalDate().toString());
		Assert.assertEquals("2016-05-04", items.get(1).getLocalDate().toString());
		Assert.assertEquals("2016-05-04", items.get(2).getLocalDate().toString());
		Assert.assertEquals("2016-05-04", items.get(3).getLocalDate().toString());
		Assert.assertEquals("2016-05-04", items.get(4).getLocalDate().toString());
		Assert.assertEquals("2016-05-04", items.get(5).getLocalDate().toString());
	}

	@Test
	public void toBeanDateTime() throws Exception {
		final List<DummyAuditedBean> items = csvForBean.toBean(DummyAuditedBean.class, new StringReader(
				"createdDate\n2016/05/04\n2016/05/04 12:54:32\n2016/05/04 12:54\n04/05/2016\n04/05/2016 12:54\n04/05/2016 12:54:32"));
		Assert.assertEquals(6, items.size());
		System.setProperty("user.timezone", "UTC");
		Assert.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(0).getCreatedDate().toString());
		Assert.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(1).getCreatedDate().toString());
		Assert.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(2).getCreatedDate().toString());
		Assert.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(3).getCreatedDate().toString());
		Assert.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(4).getCreatedDate().toString());
		Assert.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(5).getCreatedDate().toString());
	}

	@Test(expected = TechnicalException.class)
	public void toBeanInvalidForeignKey() throws IOException {
		csvForBean.toBean(DummyEntity2.class, "csv/demo/dummyentity2.csv");
	}

	@Test
	public void toJpaEmpty() throws Exception {
		final List<DummyEntity> items = csvForBean.toBean(DummyEntity.class, new StringReader(""));
		Assert.assertTrue(items.isEmpty());
	}

	@Test
	public void toBeanMap() throws Exception {
		final List<DummyEntity3> items = csvForBean.toBean(DummyEntity3.class, new StringReader("login;map$key1;map$key2\nfdaugan;value1;value2"));
		Assert.assertEquals(1, items.size());
		Assert.assertEquals(2, items.get(0).getMap().size());
		Assert.assertEquals("value1", items.get(0).getMap().get("key1"));
		Assert.assertEquals("value2", items.get(0).getMap().get("key2"));
	}

	@Test
	public void toBeanMapDuplicateKey() throws Exception {
		thrown.expect(TechnicalException.class);
		thrown.expectMessage("Unable to build an object of type : class org.ligoj.bootstrap.core.csv.DummyEntity3");
		thrown.expectCause(
				hasMessage(containsString("Duplicate map entry key='key1' for map property map in class org.ligoj.bootstrap.core.csv.DummyEntity3")));
		csvForBean.toBean(DummyEntity3.class, new StringReader("login;map$key1;map$key1\nfdaugan;value1;value2"));
	}

	@Test
	public void toBeanMapNotMapDuplicateKey() throws Exception {
		thrown.expect(TechnicalException.class);
		thrown.expectMessage("Unable to build an object of type : class org.ligoj.bootstrap.core.csv.DummyEntity3");
		thrown.expectCause(hasMessage(containsString(
				"Can not set java.util.Date field org.ligoj.bootstrap.core.csv.DummyEntity3.lastConnection to java.util.LinkedHashMap")));
		csvForBean.toBean(DummyEntity3.class, new StringReader("login;lastConnection$key1;lastConnection$key1\nfdaugan;value1;value2"));
	}

	/**
	 * Create a new dummy entity.
	 */
	private DummyEntity newWine() {
		final DummyEntity wine = new DummyEntity();
		wine.setWneCnty("1");
		wine.setWneDesc("2");
		wine.setWneGrpe("3");
		wine.setId(4);
		wine.setName("5");
		wine.setWnePict("6");
		wine.setWneRegn("7");
		wine.setWneYear(8);
		return wine;
	}

	/**
	 * Compare two wine items.
	 */
	private void assertEquals(final DummyEntity newWine, final DummyEntity wine2) {
		Assert.assertEquals(newWine.getWneCnty(), wine2.getWneCnty());
		Assert.assertEquals(newWine.getWneDesc(), wine2.getWneDesc());
		Assert.assertEquals(newWine.getWneGrpe(), wine2.getWneGrpe());
		Assert.assertEquals(null, wine2.getId());
		Assert.assertEquals(newWine.getName(), wine2.getName());
		Assert.assertEquals(newWine.getWnePict(), wine2.getWnePict());
		Assert.assertEquals(newWine.getWneRegn(), wine2.getWneRegn());
		Assert.assertEquals(newWine.getWneYear(), wine2.getWneYear());
	}
}
