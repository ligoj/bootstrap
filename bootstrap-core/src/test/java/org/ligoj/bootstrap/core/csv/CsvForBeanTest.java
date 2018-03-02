package org.ligoj.bootstrap.core.csv;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.DateUtils;
import org.ligoj.bootstrap.core.resource.TechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Check all CSV to/from simple beans of {@link CsvForBean} utility.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
public class CsvForBeanTest {

	@BeforeAll
	public static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");

		// Fix UTC time zone for this test, since date are compared
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Autowired
	private CsvForBean csvForBean;

	@Test
	public void toCsvEmpty() throws Exception {
		final StringWriter result = new StringWriter();
		csvForBean.toCsv(new ArrayList<DummyEntity>(), DummyEntity.class, result);

		// Check there is only the header
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe; wnePict ;wneRegn;wneYear\n", result.toString());

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
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;;2;3;6;7;8\n", result.toString());
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
		Assertions.assertEquals(
				"id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;\"Château d\"\"Yquem\";\"World, hold on;\";2;3;6;7;8\n",
				result.toString());
	}

	@Test
	public void toCsv() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		items.add(newWine());
		final StringWriter result = new StringWriter();
		csvForBean.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;1;2;3;6;7;8\n", result.toString());
	}

	@Test
	public void toBean() throws Exception {
		final List<DummyEntity> items = csvForBean.toBean(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;5;6;7;'8'"));
		Assertions.assertEquals(1, items.size());
		assertEquals(newWine(), items.get(0));
	}

	@Test
	public void toBeanNullHeader() throws Exception {
		final List<DummyEntity> items = csvForBean.toBean(DummyEntity.class, new StringReader("name;;;;;;;wneYear\n4;1;2;3;5;6;7;'8'"));
		Assertions.assertEquals(1, items.size());
		DummyEntity wine2 = items.get(0);
		Assertions.assertEquals("4", wine2.getName());
		Assertions.assertEquals(8, wine2.getWneYear().intValue());

		// Ignored headers
		Assertions.assertNull(wine2.getWneCnty());
		Assertions.assertNull(wine2.getWneDesc());
		Assertions.assertNull(wine2.getWneGrpe());
		Assertions.assertNull(wine2.getWnePict());
		Assertions.assertNull(wine2.getWneRegn());

	}

	@Test
	public void toBeanDate() throws Exception {
		final List<DummyEntity2> items = csvForBean.toBean(DummyEntity2.class, new StringReader(
				"dialDate\n2016/05/04\n2016/05/04 12:54:32\n2016/05/04 12:54\n04/05/2016\n04/05/2016 12:54\n04/05/2016 12:54:32"));
		Assertions.assertEquals(6, items.size());
		Assertions.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(0).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(1).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(2).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(3).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(4).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(5).getDialDate().toString());
	}

	@Test
	public void toBeanLocalDate() throws Exception {
		final List<DummyEntity2> items = csvForBean.toBean(DummyEntity2.class, new StringReader(
				"localDate\n2016/05/04\n2016/05/04 12:54:32\n2016/05/04 12:54\n04/05/2016\n04/05/2016 12:54\n04/05/2016 12:54:32"));
		Assertions.assertEquals(6, items.size());
		Assertions.assertEquals("2016-05-04", items.get(0).getLocalDate().toString());
		Assertions.assertEquals("2016-05-04", items.get(1).getLocalDate().toString());
		Assertions.assertEquals("2016-05-04", items.get(2).getLocalDate().toString());
		Assertions.assertEquals("2016-05-04", items.get(3).getLocalDate().toString());
		Assertions.assertEquals("2016-05-04", items.get(4).getLocalDate().toString());
		Assertions.assertEquals("2016-05-04", items.get(5).getLocalDate().toString());
	}

	@Test
	public void toBeanDateTime() throws Exception {
		final List<DummyAuditedBean> items = csvForBean.toBean(DummyAuditedBean.class, new StringReader(
				"createdDate\n2016/05/04\n2016/05/04 12:54:32\n2016/05/04 12:54\n04/05/2016\n04/05/2016 12:54\n04/05/2016 12:54:32"));
		Assertions.assertEquals(6, items.size());
		System.setProperty("user.timezone", "UTC");
		Assertions.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(0).getCreatedDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(1).getCreatedDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(2).getCreatedDate().toString());
		Assertions.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(3).getCreatedDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(4).getCreatedDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(5).getCreatedDate().toString());
	}

	@Test
	public void toBeanInvalidForeignKey() {
		Assertions.assertThrows(TechnicalException.class, () -> {
			csvForBean.toBean(DummyEntity2.class, "csv/demo/dummyentity2.csv");
		});
	}

	@Test
	public void toJpaEmpty() throws Exception {
		final List<DummyEntity> items = csvForBean.toBean(DummyEntity.class, new StringReader(""));
		Assertions.assertTrue(items.isEmpty());
	}

	@Test
	public void toBeanMap() throws Exception {
		final List<DummyEntity3> items = csvForBean.toBean(DummyEntity3.class,
				new StringReader("login;map$key1;map$key2\nfdaugan;value1;value2"));
		Assertions.assertEquals(1, items.size());
		Assertions.assertEquals(2, items.get(0).getMap().size());
		Assertions.assertEquals("value1", items.get(0).getMap().get("key1"));
		Assertions.assertEquals("value2", items.get(0).getMap().get("key2"));
	}

	@Test
	public void toBeanMapDuplicateKey() {
		final TechnicalException e = Assertions.assertThrows(TechnicalException.class, () -> {
			csvForBean.toBean(DummyEntity3.class, new StringReader("login;map$key1;map$key1\nfdaugan;value1;value2"));
		}, "Unable to build an object of type : class org.ligoj.bootstrap.core.csv.DummyEntity3");
		Assertions.assertTrue(e.getCause().getMessage()
				.contains("Duplicate map entry key='key1' for map property map in class org.ligoj.bootstrap.core.csv.DummyEntity3"));
	}

	@Test
	public void toBeanMapNotMapDuplicateKey() {
		final TechnicalException e = Assertions.assertThrows(TechnicalException.class, () -> {
			csvForBean.toBean(DummyEntity3.class, new StringReader("login;lastConnection$key1;lastConnection$key1\nfdaugan;value1;value2"));
		}, "Unable to build an object of type : class org.ligoj.bootstrap.core.csv.DummyEntity3");
		Assertions.assertTrue(e.getCause().getMessage().contains(
				"Can not set java.util.Date field org.ligoj.bootstrap.core.csv.DummyEntity3.lastConnection to java.util.LinkedHashMap"));
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
		Assertions.assertEquals(newWine.getWneCnty(), wine2.getWneCnty());
		Assertions.assertEquals(newWine.getWneDesc(), wine2.getWneDesc());
		Assertions.assertEquals(newWine.getWneGrpe(), wine2.getWneGrpe());
		Assertions.assertEquals(null, wine2.getId());
		Assertions.assertEquals(newWine.getName(), wine2.getName());
		Assertions.assertEquals(newWine.getWnePict(), wine2.getWnePict());
		Assertions.assertEquals(newWine.getWneRegn(), wine2.getWneRegn());
		Assertions.assertEquals(newWine.getWneYear(), wine2.getWneYear());
	}
}
