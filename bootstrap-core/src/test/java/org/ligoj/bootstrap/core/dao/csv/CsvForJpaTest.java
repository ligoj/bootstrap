/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.csv.DummyEntity;
import org.ligoj.bootstrap.core.csv.DummyEntity2;
import org.ligoj.bootstrap.core.csv.DummyEntity3;
import org.ligoj.bootstrap.core.csv.Wrapper;
import org.ligoj.bootstrap.core.resource.TechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Check all CSV to/from JPA entities or simple beans of {@link CsvForJpa} utility.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public class CsvForJpaTest {

	@BeforeAll
	public static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");
	}

	/**
	 * Entity manager.
	 */
	@PersistenceContext(type = PersistenceContextType.TRANSACTION)
	protected EntityManager em;

	@Autowired
	private CsvForJpa csvForJpa;

	@Test
	public void toCsvList() throws IOException {
		final List<DummyEntity> jpa = csvForJpa.toBean(DummyEntity.class, "csv/demo/dummyentity.csv");
		Assertions.assertFalse(jpa.isEmpty());
	}

	@Test
	public void toCsvListLocation() {
		Assertions.assertThrows(IOException.class, () -> {
			csvForJpa.toJpa(DummyEntity.class, "csv/__.csv", true);
		});
	}

	@Test
	public void toCsvEmpty() throws Exception {
		final StringWriter result = new StringWriter();
		csvForJpa.toCsv(new ArrayList<DummyEntity>(), DummyEntity.class, result);

		// Check there is only the header
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n", result.toString());

		// Only there for coverage
		Wrapper.values();
		Wrapper.valueOf(Wrapper.DOUBLE_QUOTE.name());
	}

	@Test
	public void toCsvEmptyObject() throws Exception {
		final StringWriter result = new StringWriter();
		csvForJpa.toCsv(new ArrayList<>(), Object.class, result);
	}

	@Test
	public void toCsvEntityEmpty() throws Exception {
		final StringWriter result = new StringWriter();
		csvForJpa.toCsvEntity(new ArrayList<DummyEntity>(), DummyEntity.class, result);

		// Check there is no header
		Assertions.assertEquals("", result.toString());
	}

	@Test
	public void toCsvNullProperty() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		final DummyEntity newWine = newWine();
		newWine.setWneCnty(null);
		items.add(newWine);
		csvForJpa.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line with the empty property
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;;2;3;6;7;8\n",
				result.toString());
	}

	@Test
	public void toCsvSpecialChars() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		final DummyEntity newWine = newWine();
		newWine.setWneCnty("World, hold on;");
		newWine.setName("Château d\"Yquem");
		items.add(newWine);
		csvForJpa.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assertions.assertEquals(
				"id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;\"Château d\"\"Yquem\";\"World, hold on;\";2;3;6;7;8\n",
				result.toString());
	}

	@Test
	public void toCsvEntityEmptyError() {
		final List<DummyEntity> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		items.add(null);
		Assertions.assertThrows(TechnicalException.class, () -> {
			csvForJpa.toCsvEntity(items, DummyEntity.class, result);
		});
	}

	@Test
	public void toCsv() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		items.add(newWine());
		final StringWriter result = new StringWriter();
		csvForJpa.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;1;2;3;6;7;8\n",
				result.toString());
	}

	@Test
	public void toJpa() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;1;2;3;6;7;8\n"), true);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaUnknownProperty() {
		Assertions.assertThrows(TechnicalException.class, () -> {
			csvForJpa.toJpa(DummyEntity.class, new StringReader("blah\n4\n"), true);
		});
	}

	@Test
	public void toJpaInvalidTrailing() {
		Assertions.assertThrows(TechnicalException.class, () -> {
			csvForJpa.toJpa(DummyEntity.class, new StringReader("'8' \t7\n"), false);
		});
	}

	@Test
	public void toJpaTooMuchValues() {
		Assertions.assertThrows(TechnicalException.class, () -> {
			csvForJpa.toJpa(DummyEntity.class, new StringReader("id\n4;1\n"), true);
		});
	}

	@Test
	public void toJpaTrailing1() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;5;6;7;'8'\n"), true);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaTrailing2() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;5;6;7;'8'\r"), true);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	/**
	 * No header + trailing space.
	 */
	@Test
	public void toJpaTrailing3() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new BufferedReader(new StringReader("9;5;3;1;7;8;6;'2' "), "5;3;1;7;8;6;2;'4' ".length()), false);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaNullValue() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;;6;7;8\n"), true);
		Assertions.assertEquals(1, jpa.size());
		final DummyEntity newWine = newWine();
		newWine.setName(null);
		assertEquals(newWine, jpa.get(0));
	}

	/**
	 * Check CSN reader handles well the protected and unprotected quote and the comma chars.
	 */
	@Test
	public void toJpaSpecialChars() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
				"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"World, hold on\";2;'3\"\"\'\',';4;\"Château d\"\"Yquem\";6;7;8\n"),
				true);
		Assertions.assertEquals(1, jpa.size());
		final DummyEntity newWine = newWine();
		newWine.setWneCnty("World, hold on");
		newWine.setName("Château d\"Yquem");
		newWine.setWneGrpe("3\"\"',");
		assertEquals(newWine, jpa.get(0));
	}

	/**
	 * Check CSN reader handles well the protected and unprotected quote and the comma chars.
	 */
	@Test
	public void toJpaSpecialCharsEnds() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
				"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"World, hold on\";2;'3\"\"\'\',';4;\"Château d\"\"Yquem\";6;7;\"8\n"),
				true);
		Assertions.assertEquals(1, jpa.size());
		final DummyEntity newWine = newWine();
		newWine.setWneCnty("World, hold on");
		newWine.setName("Château d\"Yquem");
		newWine.setWneGrpe("3\"\"',");
		assertEquals(newWine, jpa.get(0));
	}

	/**
	 * Check CSN reader handles well the protected and unprotected quote and the comma chars.
	 */
	@Test
	public void toJpaIgnoreSpaces() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
				"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"1\"    ; 2 ; 3;4 ; \t  \"5\";   ' 6 '   ; 7 \"   ;  8   \n"),
				true);
		Assertions.assertEquals(1, jpa.size());
		final DummyEntity newWine = newWine();
		newWine.setWnePict(" 6 ");
		newWine.setWneRegn("7 \"");
		assertEquals(newWine, jpa.get(0));
	}

	/**
	 * Check the protected new line is well managed.
	 */
	@Test
	public void toJpaEmbeddedNewLine() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
				"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"World\n, hold\non\";2;3;4;5;6;7;8\n"),
				true);
		Assertions.assertEquals(1, jpa.size());
		final DummyEntity newWine = newWine();
		newWine.setWneCnty("World\n, hold\non");
		assertEquals(newWine, jpa.get(0));
	}

	@Test
	public void toJpaEntity() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader("9;5;3;1;7;8;6;2\n"), false);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaBufferEnd() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new BufferedReader(new StringReader("9;5;3;1;7;8;6;2\n"), "9;5;3;1;7;8;6;2\n".length()), false);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaNoValues() throws Exception {
		final BufferedReader bufferedReader = new BufferedReader(new StringReader("\n"), 1);
		bufferedReader.read();
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, bufferedReader, false);
		Assertions.assertTrue(jpa.isEmpty());
	}

	@Test
	public void toJpaNoValues2() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new BufferedReader(new StringReader("\r")),
				false);
		Assertions.assertTrue(jpa.isEmpty());
	}

	@Test
	public void toJpaBufferEnd2() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new BufferedReader(new StringReader("9;5;3;1;7;8;6;2;\n\n\r")), false);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaBufferEnd3() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new BufferedReader(new StringReader("\n\r9;5;3;1;7;8;6;2")), false);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaUnknownClass() {
		Assertions.assertThrows(TechnicalException.class, () -> {
			csvForJpa.toJpa(Integer.class, new StringReader("n\n1\n"), true);
		});
	}

	@Test
	public void toJpaEmpty() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(""), true);
		Assertions.assertTrue(jpa.isEmpty());
	}

	@Test
	public void toJpaPerformance() throws Exception {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n");
		for (int i = 10000; i-- > 0;) {
			stringBuilder.append("1;2;3;4;5;6;7;");
			stringBuilder.append(i);
			stringBuilder.append('\r');
		}
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(stringBuilder.toString()),
				true);
		Assertions.assertEquals(10000, jpa.size());
	}

	@Test
	public void toJpatoJpaEntityPerformance() {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 10000; i-- > 0;) {
			stringBuilder.append("1;4;5;3;7;8;6;2\n");
		}
		Assertions.assertTimeout(Duration.ofSeconds(2), () -> {
			final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(stringBuilder.toString()),
					false);
			Assertions.assertEquals(10000, jpa.size());
		});
	}

	@Test
	public void toCsvPerformance() {
		final List<DummyEntity> items = newWines();
		final StringWriter result = new StringWriter();
		Assertions.assertTimeout(Duration.ofSeconds(2), () -> {
			csvForJpa.toCsv(items, DummyEntity.class, result);
		});

		// 160 per lines + 56 for header
		Assertions.assertEquals(160000 + 56, result.getBuffer().length());
	}

	@Test
	public void toJpaMissingValues() {
		Assertions.assertThrows(TechnicalException.class, () -> {
			csvForJpa.toJpa(DummyEntity.class, new StringReader("4;3.5;5;5;5;5;5;5;5;5;5;5\n"), false);
		});
	}

	@Test
	public void toCsvEntity() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		items.add(newWine());
		csvForJpa.toCsvEntity(items, DummyEntity.class, result);

		// Check there is only data
		Assertions.assertEquals("4;5;3;1;7;8;6;2\n", result.toString());
	}

	@Test
	public void toCsvEntityPerformance() {
		final List<DummyEntity> items = newWines();
		final StringWriter result = new StringWriter();
		Assertions.assertTimeout(Duration.ofSeconds(1), () -> {
			csvForJpa.toCsvEntity(items, DummyEntity.class, result);
		});
	}

	@Test
	public void toJpaForeignKey() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, "csv/demo/dummyentity2.csv", true);
		Assertions.assertFalse(jpa.isEmpty());
	}

	@Test
	public void toJpaForeignKeyPersist() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, "csv/demo/dummyentity2.csv", true, true);
		Assertions.assertFalse(jpa.isEmpty());
		Assertions.assertNotNull(jpa.get(0).getId());
	}

	@Test
	public void toJpaForeignKeyNoOptimisation() throws IOException {
		final DummyEntity2 entity = new DummyEntity2();
		em.persist(entity);
		em.flush();
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("link.id!\n1"), true);
		Assertions.assertEquals(1, jpa.size());
		Assertions.assertEquals(entity.getId(), jpa.get(0).getLink().getId());
	}

	@Test
	public void toJpaForeignKeyRecursive() throws IOException {
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class,
				new StringReader("dialChar;link.dialChar\nA;\nB;A\nC;B"), true, true);
		Assertions.assertEquals(3, jpa.size());
	}

	@Test
	public void toJpaForeignKeyNotExist1() {
		final TechnicalException iau = Assertions.assertThrows(TechnicalException.class, () -> {
			csvForJpa.toJpa(DummyEntity2.class, new StringReader("link.id!\n8000"), true);
		});
		Assertions.assertEquals("Missing foreign key DummyEntity2#link.id = 8000", iau.getCause().getMessage());
	}

	@Test
	public void toJpaEnum() throws IOException {
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialEnum\nALL"), true);
		Assertions.assertEquals(1, jpa.size());
	}

	@Test
	public void toJpaEnumIgnoreCase() throws IOException {
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialEnum\naLl"), true);
		Assertions.assertEquals(1, jpa.size());
	}

	@Test
	public void toJpaEnumInvalid() {
		Assertions.assertThrows(TechnicalException.class, () -> {
			csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialEnum\n_ERROR_"), true);
		});
	}

	@Test
	public void toJpaForeignKeyNatural() throws IOException {
		final DummyEntity3 systemUser = new DummyEntity3();
		systemUser.setLogin("test");
		em.persist(systemUser);
		em.flush();
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class,
				new StringReader("user.login\n" + systemUser.getLogin() + "\n" + systemUser.getLogin()), true);
		Assertions.assertEquals(2, jpa.size());
		Assertions.assertEquals("test", jpa.get(0).getUser().getLogin());
	}

	@Test
	public void toJpaForeignKeyNaturalNotExist() {
		Assertions.assertEquals("Missing foreign key DummyEntity2#user.login = nobody",
				Assertions.assertThrows(TechnicalException.class, () -> {
					csvForJpa.toJpa(DummyEntity2.class, new StringReader("user.login\nnobody"), true);
				}).getCause().getMessage());
	}

	@Test
	public void toJpaForeignKeyNaturalNotExist2() {
		Assertions.assertEquals("Missing foreign key DummyEntity2#user.login = nobody",
				Assertions.assertThrows(TechnicalException.class, () -> {
					csvForJpa.toJpa(DummyEntity2.class, new StringReader("user.login!\nnobody"), true);
				}).getCause().getMessage());
	}

	@Test
	public void toJpaForeignKeyNotExist2() {
		Assertions.assertEquals("Missing foreign key DummyEntity2#link.id = 8000",
				Assertions.assertThrows(TechnicalException.class, () -> {
					csvForJpa.toJpa(DummyEntity2.class, new StringReader("link.id\n8000"), true);
				}).getCause().getMessage());
	}

	@Test
	public void toJpaForeignKeyNotExist3() {
		Assertions.assertEquals("Missing foreign key DummyEntity2#link.id = -8000",
				Assertions.assertThrows(TechnicalException.class, () -> {
					csvForJpa.toJpa(DummyEntity2.class, new StringReader("link.id\n-8000"), true);
				}).getCause().getMessage());
	}

	@Test
	public void getJpaHeaders() {
		final String[] jpaHeaders = csvForJpa.getJpaHeaders(DummyEntity2.class);
		Assertions.assertArrayEquals(new String[] { "id", "dialChar", "dialBool", "dialShort", "dialLong", "dialDouble",
				"dialDate", "localDate", "dialEnum", "link", "user" }, jpaHeaders);
	}

	@Test
	public void cleanup() {
		em.persist(new DummyEntity2());
		em.flush();
		Assertions.assertEquals(1, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
		csvForJpa.cleanup(DummyEntity2.class);
		Assertions.assertEquals(0, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
	}

	@Test
	public void insert() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		csvForJpa.insert("csv/demo", DummyEntity.class);
		Assertions.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
	}

	@Test
	public void insert2() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		csvForJpa.insert("csv/demo", DummyEntity.class, StandardCharsets.UTF_8.name());
		Assertions.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
	}

	@Test
	public void insertConsummer() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		AtomicInteger flag = new AtomicInteger();
		csvForJpa.insert("csv/demo", DummyEntity.class, StandardCharsets.UTF_8.name(), d -> flag.getAndIncrement());
		Assertions.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
		Assertions.assertEquals(12, flag.get());
	}

	@Test
	public void reset() throws IOException {
		em.persist(new DummyEntity());
		em.flush();
		Assertions.assertEquals(1, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
		csvForJpa.reset("csv/demo", DummyEntity.class);
		Assertions.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
	}

	@Test
	public void resetSelfReferencing() throws IOException {
		DummyEntity2 parent = new DummyEntity2();
		em.persist(parent);
		final DummyEntity2 child = new DummyEntity2();
		child.setLink(parent);
		em.persist(child);
		final DummyEntity2 self = new DummyEntity2();
		self.setLink(self);
		em.persist(self);
		final DummyEntity2 futureChild = new DummyEntity2();
		em.persist(futureChild);
		final DummyEntity2 futureParent = new DummyEntity2();
		em.persist(futureParent);
		futureChild.setLink(futureParent);
		em.merge(futureChild);
		em.flush();
		em.clear();
		Assertions.assertEquals(5, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
		csvForJpa.reset("csv/demo/", DummyEntity2.class);
		Assertions.assertEquals(5, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
	}

	@Test
	public void resetInvalidPath() {
		Assertions.assertThrows(FileNotFoundException.class, () -> {
			csvForJpa.reset("csv/__", DummyEntity.class);
		});
	}

	/**
	 * Create a new list of dummy entities.
	 */
	private List<DummyEntity> newWines() {
		final List<DummyEntity> items = new ArrayList<>();
		final DummyEntity newWine = newWine();
		for (int i = 10000; i-- > 0;) {
			items.add(newWine);
		}
		return items;
	}

	/**
	 * Create a new dummy entity.
	 */
	private DummyEntity newWine() {
		final DummyEntity wine = new DummyEntity();
		wine.setId(4);
		wine.setName("5");
		wine.setWneCnty("1");
		wine.setWneDesc("2");
		wine.setWneGrpe("3");
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

	@Test
	public void toJpaForeignKeyInSet() throws IOException {
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, "csv/demo/dummyentity2-collection.csv", true,
				true);
		Assertions.assertEquals("A", jpa.get(0).getDialChar());
		Assertions.assertEquals("B", jpa.get(1).getDialChar());
		Assertions.assertTrue(jpa.get(1).getChildren().get(0).equals(jpa.get(0)));
		Assertions.assertTrue(jpa.get(1).getLinkedChildren().contains(jpa.get(0)));
		Assertions.assertEquals("C", jpa.get(2).getDialChar());
		Assertions.assertTrue(jpa.get(2).getChildren().get(0).equals(jpa.get(0)));
		Assertions.assertTrue(jpa.get(2).getChildren().get(1).equals(jpa.get(1)));
		Assertions.assertTrue(jpa.get(2).getLinkedChildren().contains(jpa.get(0)));
		Assertions.assertTrue(jpa.get(2).getLinkedChildren().contains(jpa.get(1)));
		Assertions.assertTrue(jpa.get(2).getLinkedChildrenCollection().contains(jpa.get(0)));
		Assertions.assertTrue(jpa.get(2).getLinkedChildrenCollection().contains(jpa.get(1)));
	}

	@Test
	public void toJpaNullForeignKey() throws Exception {
		// Add 2 records having foreign keys 'dialChar' : "A" and null
		csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialChar;children.dialChar\nA\n;A"), true, true);

		// Add a reference to previous records
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class,
				new StringReader("dialChar;children.dialChar\nB;A\nC;"), true, true);
		Assertions.assertEquals(2, jpa.size());
	}
}
