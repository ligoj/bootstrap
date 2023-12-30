/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import jakarta.persistence.CascadeType;

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
class CsvForBeanTest {

	@BeforeAll
	static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");

		// Fix UTC time zone for this test, since date are compared
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Autowired
	private CsvForBean csvForBean;

	@Test
	void toCsvEmpty() throws IOException {
		final var result = new StringWriter();
		csvForBean.toCsv(new ArrayList<>(), DummyEntity.class, result);

		// Check there is only the header
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n", result.toString());

		// Only there for coverage
		Assertions.assertTrue(Wrapper.values().length > 0);
		Assertions.assertNotNull(Wrapper.valueOf(Wrapper.DOUBLE_QUOTE.name()));
	}

	@Test
	void toCsvEmptyObject() throws Exception {
		final var result = new StringWriter();
		csvForBean.toCsv(new ArrayList<>(), Object.class, result);
	}

	@Test
	void toCsvNullProperty() throws IOException {
		final List<DummyEntity> items = new ArrayList<>();
		final var result = new StringWriter();
		final var newWine = newWine();
		newWine.setWneCnty(null);
		items.add(newWine);
		csvForBean.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;;2;3;6;7;8\n",
				result.toString());
	}

	@Test
	void toCsvSpecialChars() throws IOException {
		final List<DummyEntity> items = new ArrayList<>();
		final var result = new StringWriter();
		final var newWine = newWine();
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
	void toCsv() throws IOException {
		final List<DummyEntity> items = new ArrayList<>();
		items.add(newWine());
		final var result = new StringWriter();
		csvForBean.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;1;2;3;6;7;8\n",
				result.toString());
	}

	@Test
	void toBean() throws IOException {
		final var items = csvForBean.toBean(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;5;6;7;'8'"));
		Assertions.assertEquals(1, items.size());
		assertEquals(newWine(), items.getFirst());
	}

	@Test
	void toBeanNull() throws IOException {
		final var item = csvForBean.toBean(new CsvBeanReader<>(new StringReader(""), DummyEntity.class));
		Assertions.assertNull(item);
	}

	@Test
	void toBeanPerformance() throws IOException {
		final var count = 100;
		for (var i = count; i-- > 0; ) {
			toBeanPerformanceP();
		}
	}

	void toBeanPerformanceP() throws IOException {
		final var buffer = new StringBuilder("name;name;name;name;name;name\n");
		final var count = 1000;
		for (var i = count; i-- > 0; ) {
			buffer.append("name;name;name;name;name;name\n");
		}
		final var items = csvForBean.toBean(DummyEntity.class, new StringReader(buffer.toString()));
		Assertions.assertEquals(count, items.size());
		Assertions.assertNotNull(items.getFirst().getName());
	}

	@Test
	void toBeanPerformance2() throws IOException {
		final var count = 100;
		for (var i = count; i-- > 0; ) {
			toBeanPerformanceP2();
		}
	}

	void toBeanPerformanceP2() throws IOException {
		final var buffer = new StringBuilder("name;name;name;name;name;name\n");
		final var count = 1000;
		for (var i = count; i-- > 0; ) {
			buffer.append("name;name;name;name;name;name\n");
		}
		final var items = csvForBean.toBean(DummyEntity.class, new StringReader(buffer.toString()),
				(item, p, v) -> item.setName(v));
		Assertions.assertEquals(count, items.size());
		Assertions.assertNotNull(items.getFirst().getName());
	}

	@Test
	void toBeanNullHeader() throws Exception {
		final var items = csvForBean.toBean(DummyEntity.class,
				new StringReader("name;;;   ;;;;wneYear\n4;1;2;3;5;6;7;'8'"));
		Assertions.assertEquals(1, items.size());
		var wine2 = items.getFirst();
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
	void toBeanTrimHeader() throws Exception {
		final var items = csvForBean.toBean(DummyEntity.class, new StringReader("name; wneYear \n4;1"));
		Assertions.assertEquals(1, items.size());
		var wine2 = items.getFirst();
		Assertions.assertEquals("4", wine2.getName());
		Assertions.assertEquals(1, wine2.getWneYear().intValue());
	}

	@Test
	void toBeanDouble() throws Exception {
		final var items = csvForBean.toBean(DummyEntity2.class, new StringReader("dialDouble\n1\n1.1\n1,2\n1 000.3"));
		Assertions.assertEquals(4, items.size());
		Assertions.assertEquals(1d, items.get(0).getDialDouble());
		Assertions.assertEquals(1.1d, items.get(1).getDialDouble());
		Assertions.assertEquals(1.2d, items.get(2).getDialDouble());
		Assertions.assertEquals(1000.3d, items.get(3).getDialDouble());
	}

	@Test
	void toBeanDate() throws Exception {
		final var items = csvForBean.toBean(DummyEntity2.class, new StringReader(
				"dialDate\n1556387665000\n2019-02-25T10:15:30\n2016/05/04\n2016/05/04 12:54:32\n2016/05/04 12:54\n04/05/2016\n04/05/2016 12:54\n04/05/2016 12:54:32"));
		Assertions.assertEquals(8, items.size());
		Assertions.assertEquals("Sat Apr 27 17:54:25 UTC 2019", items.get(0).getDialDate().toString());
		Assertions.assertEquals("Mon Feb 25 10:15:30 UTC 2019", items.get(1).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(2).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(3).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(4).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 00:00:00 UTC 2016", items.get(5).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:00 UTC 2016", items.get(6).getDialDate().toString());
		Assertions.assertEquals("Wed May 04 12:54:32 UTC 2016", items.get(7).getDialDate().toString());
	}

	@Test
	void toBeanLocalDate() throws Exception {
		final var items = csvForBean.toBean(DummyEntity2.class, new StringReader(
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
	void toBeanUnhandledDate() {
		final var reader = new StringReader("dialDate\nNOT_DATE");
		Assertions.assertThrows(TechnicalException.class, () -> csvForBean.toBean(DummyEntity2.class, reader));
	}

	@Test
	void toBeanDateTime() throws Exception {
		final var items = csvForBean.toBean(DummyAuditedBean.class, new StringReader(
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
	void toBeanInvalidForeignKey() {
		Assertions.assertThrows(TechnicalException.class,
				() -> csvForBean.toBean(DummyEntity2.class, "csv/demo/dummyentity2.csv"));
	}

	@Test
	void toJpaEmpty() throws Exception {
		final var items = csvForBean.toBean(DummyEntity.class, new StringReader(""));
		Assertions.assertTrue(items.isEmpty());
	}

	@Test
	void toBeanMap() throws Exception {
		final var items = csvForBean.toBean(DummyEntity3.class,
				new StringReader("login;map$key1;map$key2\nfdaugan;value1;value2"));
		Assertions.assertEquals(1, items.size());
		Assertions.assertEquals(2, items.getFirst().getMap().size());
		Assertions.assertEquals("value1", items.getFirst().getMap().get("key1"));
		Assertions.assertEquals("value2", items.getFirst().getMap().get("key2"));
	}

	@Test
	void toBeanList() throws Exception {
		final var items = csvForBean.toBean(DummyEntity3.class,
				new StringReader("login;list;setEnum\nfdaugan;value1,value2;PERSIST,MERGE"));
		Assertions.assertEquals(1, items.size());
		Assertions.assertEquals(2, items.getFirst().getList().size());
		Assertions.assertEquals("value1", items.getFirst().getList().get(0));
		Assertions.assertEquals("value2", items.getFirst().getList().get(1));
		Assertions.assertEquals(2, items.getFirst().getSetEnum().size());
		Assertions.assertTrue(items.getFirst().getSetEnum().contains(CascadeType.PERSIST));
		Assertions.assertTrue(items.getFirst().getSetEnum().contains(CascadeType.MERGE));
	}

	@Test
	void toBeanMapDuplicateKey() {
		final var reader = new StringReader("login;map$key1;map$key1\nfdaugan;value1;value2");
		final var e = Assertions.assertThrows(TechnicalException.class,
				() -> csvForBean.toBean(DummyEntity3.class, reader),
				"Unable to build an object of type : class org.ligoj.bootstrap.core.csv.DummyEntity3");
		Assertions.assertTrue(e.getCause().getMessage().contains(
				"Duplicate map entry key='key1' for map property map in class org.ligoj.bootstrap.core.csv.DummyEntity3"));
	}

	@Test
	void toBeanMapNotMapDuplicateKey() {
		final var reader = new StringReader("login;lastConnection$key1;lastConnection$key1\nfdaugan;value1;value2");
		final var e = Assertions.assertThrows(TechnicalException.class,
				() -> csvForBean.toBean(DummyEntity3.class, reader),
				"Unable to build an object of type : class org.ligoj.bootstrap.core.csv.DummyEntity3");
		Assertions.assertTrue(e.getCause().getMessage().contains(
				"Can not set java.util.Date field org.ligoj.bootstrap.core.csv.DummyEntity3.lastConnection"));
	}

	/**
	 * Create a new dummy entity.
	 */
	private DummyEntity newWine() {
		final var wine = new DummyEntity();
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
		Assertions.assertNull(wine2.getId());
		Assertions.assertEquals(newWine.getName(), wine2.getName());
		Assertions.assertEquals(newWine.getWnePict(), wine2.getWnePict());
		Assertions.assertEquals(newWine.getWneRegn(), wine2.getWneRegn());
		Assertions.assertEquals(newWine.getWneYear(), wine2.getWneYear());
	}
}
