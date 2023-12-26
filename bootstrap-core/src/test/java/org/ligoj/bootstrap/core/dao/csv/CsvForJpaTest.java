/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao.csv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.csv.DummyEntity;
import org.ligoj.bootstrap.core.csv.DummyEntity2;
import org.ligoj.bootstrap.core.csv.DummyEntity3;
import org.ligoj.bootstrap.core.csv.Wrapper;
import org.ligoj.bootstrap.core.model.DummyChildClass;
import org.ligoj.bootstrap.core.model.DummyNamedEntity;
import org.ligoj.bootstrap.core.resource.TechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Check all CSV to/from JPA entities or simple beans of {@link CsvForJpa} utility.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
class CsvForJpaTest {

	@BeforeAll
	static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");
	}

	/**
	 * Entity manager.
	 */
	@PersistenceContext(type = PersistenceContextType.TRANSACTION)
	private EntityManager em;

	@Autowired
	private CsvForJpa csvForJpa;

	@Test
	void toBeanEmpty() throws IOException {
		final var jpa = csvForJpa.toBean(DummyEntity.class,
				new InputStreamReader(new ClassPathResource("csv/demo/dummyentity.csv").getInputStream()), null);
		Assertions.assertTrue(jpa.isEmpty());
	}

	@Test
	void toCsvList() throws IOException {
		final var jpa = csvForJpa.toBean(DummyEntity.class, "csv/demo/dummyentity.csv");
		Assertions.assertFalse(jpa.isEmpty());
	}

	@Test
	void toCsvListLocation() {
		Assertions.assertThrows(IOException.class, () -> csvForJpa.toJpa(DummyEntity.class, "csv/__.csv", true));
	}

	@Test
	void toCsvEmpty() throws Exception {
		final var result = new StringWriter();
		csvForJpa.toCsv(new ArrayList<>(), DummyEntity.class, result);

		// Check there is only the header
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n", result.toString());

		// Only there for coverage
		Assertions.assertEquals("DOUBLE_QUOTE", Wrapper.values()[Wrapper.valueOf(Wrapper.DOUBLE_QUOTE.name()).ordinal()].name());
	}

	@Test
	void toCsvEmptyObject() throws Exception {
		final var result = new StringWriter();
		csvForJpa.toCsv(new ArrayList<>(), Object.class, result);
	}

	@Test
	void toCsvEntityEmpty() throws Exception {
		final var result = new StringWriter();
		csvForJpa.toCsvEntity(new ArrayList<>(), DummyEntity.class, result);

		// Check there is no header
		Assertions.assertEquals("", result.toString());
	}

	@Test
	void toCsvNullProperty() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final var result = new StringWriter();
		final var newWine = newWine();
		newWine.setWneCnty(null);
		items.add(newWine);
		csvForJpa.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line with the empty property
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;;2;3;6;7;8\n",
				result.toString());
	}

	@Test
	void toCsvSpecialChars() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final var result = new StringWriter();
		final var newWine = newWine();
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
	void toCsvEntityEmptyError() {
		final List<DummyEntity> items = new ArrayList<>();
		final var result = new StringWriter();
		items.add(null);
		Assertions.assertThrows(TechnicalException.class,
				() -> csvForJpa.toCsvEntity(items, DummyEntity.class, result));
	}

	@Test
	void toCsv() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		items.add(newWine());
		final var result = new StringWriter();
		csvForJpa.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assertions.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;1;2;3;6;7;8\n",
				result.toString());
	}

	@Test
	void toJpaFiltered() throws Exception {
		Assertions.assertEquals(1,
				csvForJpa.toJpa(DummyEntity3.class, new StringReader("login\nA"), true, true, e -> true).size());
		em.flush();
		em.clear();
		csvForJpa.toJpa(DummyEntity3.class, new StringReader("login\nA"), true, true, e -> true);
		Assertions.assertThrows(PersistenceException.class, () -> em.flush());

		// Try again, but filter the entries
		Assertions.assertEquals(1,
				csvForJpa.toJpa(DummyEntity3.class, new StringReader("login\nA"), true, true, e -> false).size());
	}

	@Test
	void toJpaUnknownProperty() {
		final var str = new StringReader("blah\n4\n");
		Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(DummyEntity.class, str, true));
	}

	@Test
	void toJpaInvalidTrailing() {
		final var str = new StringReader("'8' \t7\n");
		Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(DummyEntity.class, str, false));
	}

	@Test
	void toJpaTooMuchValues() {
		final var str = new StringReader("id\n4;1\n");
		Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(DummyEntity.class, str, true));
	}

	private void toJpa(final String input, final boolean hasHeader) throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(input), hasHeader);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.getFirst());
	}

	@Test
	void toJpa() throws Exception {
		toJpa("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;1;2;3;6;7;8\n", true);
	}

	@Test
	void toJpaTrailing1() throws Exception {
		toJpa("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;5;6;7;'8'\n", true);
	}

	@Test
	void toJpaTrailing2() throws Exception {
		toJpa("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;5;6;7;'8'\r", true);
	}

	@Test
	void toJpaEntity() throws Exception {
		toJpa("9;5;3;1;7;8;6;2\n", false);
	}

	/**
	 * No header + trailing space.
	 */
	@Test
	void toJpaTrailing3() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class,
				new BufferedReader(new StringReader("9;5;3;1;7;8;6;'2' "), "5;3;1;7;8;6;2;'4' ".length()), false);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.getFirst());
	}

	@Test
	void toJpaNullValue() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;;6;7;8\n"), true);
		Assertions.assertEquals(1, jpa.size());
		final var newWine = newWine();
		newWine.setName(null);
		assertEquals(newWine, jpa.getFirst());
	}

	/**
	 * Check CSN reader handles well the protected and unprotected quote and the comma chars.
	 */
	@Test
	void toJpaSpecialChars() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
						"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"World, hold on\";2;'3\"\"'',';4;\"Château d\"\"Yquem\";6;7;8\n"),
				true);
		Assertions.assertEquals(1, jpa.size());
		final var newWine = newWine();
		newWine.setWneCnty("World, hold on");
		newWine.setName("Château d\"Yquem");
		newWine.setWneGrpe("3\"\"',");
		assertEquals(newWine, jpa.getFirst());
	}

	/**
	 * Check CSN reader handles well the protected and unprotected quote and the comma chars.
	 */
	@Test
	void toJpaSpecialCharsEnds() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
						"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"World, hold on\";2;'3\"\"'',';4;\"Château d\"\"Yquem\";6;7;\"8\n"),
				true);
		Assertions.assertEquals(1, jpa.size());
		final var newWine = newWine();
		newWine.setWneCnty("World, hold on");
		newWine.setName("Château d\"Yquem");
		newWine.setWneGrpe("3\"\"',");
		assertEquals(newWine, jpa.getFirst());
	}

	/**
	 * Check CSN reader handles well the protected and unprotected quote and the comma chars.
	 */
	@Test
	void toJpaIgnoreSpaces() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
						"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"1\"    ; 2 ; 3;4 ; \t  \"5\";   ' 6 '   ; 7 \"   ;  8   \n"),
				true);
		Assertions.assertEquals(1, jpa.size());
		final var newWine = newWine();
		newWine.setWnePict(" 6 ");
		newWine.setWneRegn("7 \"");
		assertEquals(newWine, jpa.getFirst());
	}

	/**
	 * Check the protected new line is well managed.
	 */
	@Test
	void toJpaEmbeddedNewLine() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
						"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"World\n, hold\non\";2;3;4;5;6;7;8\n"),
				true);
		Assertions.assertEquals(1, jpa.size());
		final var newWine = newWine();
		newWine.setWneCnty("World\n, hold\non");
		assertEquals(newWine, jpa.getFirst());
	}

	@Test
	void toJpaBufferEnd() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class,
				new BufferedReader(new StringReader("9;5;3;1;7;8;6;2\n"), "9;5;3;1;7;8;6;2\n".length()), false);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.getFirst());
	}

	@Test
	void toJpaNoValues() throws Exception {
		final var bufferedReader = new BufferedReader(new StringReader("\n"), 1);
		bufferedReader.read();
		final var jpa = csvForJpa.toJpa(DummyEntity.class, bufferedReader, false);
		Assertions.assertTrue(jpa.isEmpty());
	}

	@Test
	void toJpaNoValues2() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class, new BufferedReader(new StringReader("\r")), false);
		Assertions.assertTrue(jpa.isEmpty());
	}

	@Test
	void toJpaBufferEnd2() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class,
				new BufferedReader(new StringReader("9;5;3;1;7;8;6;2;\n\n\r")), false);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.getFirst());
	}

	@Test
	void toJpaBufferEnd3() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class, new BufferedReader(new StringReader("\n\r9;5;3;1;7;8;6;2")),
				false);
		Assertions.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.getFirst());
	}

	@Test
	void toJpaUnknownClass() {
		final var str = new StringReader("n\n1\n");
		Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(Integer.class, str, true));
	}

	@Test
	void toJpaEmpty() throws Exception {
		final var jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(""), true);
		Assertions.assertTrue(jpa.isEmpty());
	}

	@Test
	void toJpaPerformance() throws Exception {
		final var stringBuilder = new StringBuilder();
		stringBuilder.append("wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n");
		for (var i = 10000; i-- > 0; ) {
			stringBuilder.append("1;2;3;4;5;6;7;");
			stringBuilder.append(i);
			stringBuilder.append('\r');
		}
		final var jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(stringBuilder.toString()), true);
		Assertions.assertEquals(10000, jpa.size());
	}

	@Test
	void toJpatoJpaEntityPerformance() {
		final var stringBuilder = new StringBuilder();
		for (var i = 10000; i-- > 0; ) {
			stringBuilder.append("1;4;5;3;7;8;6;2\n");
		}
		Assertions.assertTimeout(Duration.ofSeconds(2), () -> Assertions.assertEquals(10000,
				csvForJpa.toJpa(DummyEntity.class, new StringReader(stringBuilder.toString()), false).size()));
	}

	@Test
	void toCsvPerformance() {
		final var items = newWines();
		final var result = new StringWriter();
		Assertions.assertTimeout(Duration.ofSeconds(2), () -> csvForJpa.toCsv(items, DummyEntity.class, result));

		// 160 per lines + 56 for header
		Assertions.assertEquals(160000 + 56, result.getBuffer().length());
	}

	@Test
	void toJpaMissingValues() {
		final var str = new StringReader("4;3.5;5;5;5;5;5;5;5;5;5;5\n");
		Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(DummyEntity.class, str, false));
	}

	@Test
	void toCsvEntity() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final var result = new StringWriter();
		items.add(newWine());
		csvForJpa.toCsvEntity(items, DummyEntity.class, result);

		// Check there is only data
		Assertions.assertEquals("4;5;3;1;7;8;6;2\n", result.toString());
	}

	@Test
	void toCsvEntityPerformance() {
		final var items = newWines();
		final var result = new StringWriter();
		Assertions.assertTimeout(Duration.ofSeconds(1), () -> csvForJpa.toCsvEntity(items, DummyEntity.class, result));
	}

	@Test
	void toJpaForeignKey() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		final var jpa = csvForJpa.toJpa(DummyEntity2.class, "csv/demo/dummyentity2.csv", true);
		Assertions.assertFalse(jpa.isEmpty());
	}

	@Test
	void toJpaForeignKeyPersist() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		final var jpa = csvForJpa.toJpa(DummyEntity2.class, "csv/demo/dummyentity2.csv", true, true);
		Assertions.assertFalse(jpa.isEmpty());
		Assertions.assertNotNull(jpa.getFirst().getId());
	}

	@Test
	void toJpaForeignKeyNoOptimisation() throws IOException {
		final var entity = new DummyEntity2();
		em.persist(entity);
		em.flush();
		final var jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("link.id!\n1"), true);
		Assertions.assertEquals(1, jpa.size());
		Assertions.assertEquals(entity.getId(), jpa.getFirst().getLink().getId());
	}

	@Test
	void toJpaForeignKeyRecursive() throws IOException {
		final var jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialChar;link.dialChar\nA;\nB;A\nC;B"),
				true, true);
		Assertions.assertEquals(3, jpa.size());
	}

	@Test
	void toJpaForeignKeyNotExist1() {
		final var str = new StringReader("link.id!\n8000");
		final var iau = Assertions.assertThrows(TechnicalException.class,
				() -> csvForJpa.toJpa(DummyEntity2.class, str, true));
		Assertions.assertEquals("Missing foreign key DummyEntity2#link.id = 8000", iau.getCause().getMessage());
	}

	@Test
	void toJpaEnum() throws IOException {
		final var jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialEnum\nALL"), true);
		Assertions.assertEquals(1, jpa.size());
	}

	@Test
	void toJpaEnumIgnoreCase() throws IOException {
		final var jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialEnum\naLl"), true);
		Assertions.assertEquals(1, jpa.size());
	}

	@Test
	void toJpaEnumInvalid() {
		final var str = new StringReader("dialEnum\n_ERROR_");
		Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(DummyEntity2.class, str, true));
	}

	@Test
	void toJpaForeignKeyNatural() throws IOException {
		final var systemUser = new DummyEntity3();
		systemUser.setLogin("test");
		em.persist(systemUser);
		em.flush();
		final var jpa = csvForJpa.toJpa(DummyEntity2.class,
				new StringReader("user.login\n" + systemUser.getLogin() + "\n" + systemUser.getLogin()), true);
		Assertions.assertEquals(2, jpa.size());
		Assertions.assertEquals("test", jpa.getFirst().getUser().getLogin());
	}

	@Test
	void toJpaForeignKeyNaturalNotExist() {
		final var str = new StringReader("user.login\nnobody");
		Assertions.assertEquals("Missing foreign key DummyEntity2#user.login = nobody",
				Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(DummyEntity2.class, str, true))
						.getCause().getMessage());
	}

	@Test
	void toJpaForeignKeyNaturalNotExist2() {
		final var str = new StringReader("user.login!\nnobody");
		Assertions.assertEquals("Missing foreign key DummyEntity2#user.login = nobody",
				Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(DummyEntity2.class, str, true))
						.getCause().getMessage());
	}

	@Test
	void toJpaForeignKeyNotExist2() {
		final var str = new StringReader("link.id\n8000");
		Assertions.assertEquals("Missing foreign key DummyEntity2#link.id = 8000",
				Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(DummyEntity2.class, str, true))
						.getCause().getMessage());
	}

	@Test
	void toJpaForeignKeyNotExist3() {
		final var str = new StringReader("link.id\n-8000");
		Assertions.assertEquals("Missing foreign key DummyEntity2#link.id = -8000",
				Assertions.assertThrows(TechnicalException.class, () -> csvForJpa.toJpa(DummyEntity2.class, str, true))
						.getCause().getMessage());
	}

	@Test
	void getJpaHeaders() {
		final var jpaHeaders = csvForJpa.getJpaHeaders(DummyEntity2.class);
		Assertions.assertArrayEquals(new String[]{"id", "dialChar", "dialBool", "dialShort", "dialLong", "dialDouble",
				"dialDate", "localDate", "dialEnum", "link", "user"}, jpaHeaders);
	}

	@Test
	void cleanup() {
		em.persist(new DummyEntity2());
		em.flush();
		Assertions.assertEquals(1, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
		csvForJpa.cleanup(DummyEntity2.class);
		Assertions.assertEquals(0, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
	}

	@Test
	void insert() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		csvForJpa.insert("csv/demo", DummyEntity.class);
		Assertions.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
	}

	@Test
	void insert2() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		csvForJpa.insert("csv/demo", DummyEntity.class, StandardCharsets.UTF_8.name());
		Assertions.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
	}

	@Test
	void insertConsummer() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		var flag = new AtomicInteger();
		csvForJpa.insert("csv/demo", DummyEntity.class, StandardCharsets.UTF_8.name(), d -> flag.getAndIncrement());
		Assertions.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
		Assertions.assertEquals(12, flag.get());
	}

	@Test
	void reset() throws IOException {
		em.persist(new DummyEntity());
		em.flush();
		Assertions.assertEquals(1, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
		csvForJpa.reset("csv/demo", DummyEntity.class);
		Assertions.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
	}

	@Test
	void resetSelfReferencing() throws IOException {
		var parent = new DummyEntity2();
		em.persist(parent);
		final var child = new DummyEntity2();
		child.setLink(parent);
		em.persist(child);
		final var self = new DummyEntity2();
		self.setLink(self);
		em.persist(self);
		final var futureChild = new DummyEntity2();
		em.persist(futureChild);
		final var futureParent = new DummyEntity2();
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
	void resetInvalidPath() {
		Assertions.assertThrows(FileNotFoundException.class, () -> csvForJpa.reset("csv/__", DummyEntity.class));
	}

	/**
	 * Create a new list of dummy entities.
	 */
	private List<DummyEntity> newWines() {
		final List<DummyEntity> items = new ArrayList<>();
		final var newWine = newWine();
		for (var i = 10000; i-- > 0; ) {
			items.add(newWine);
		}
		return items;
	}

	/**
	 * Create a new dummy entity.
	 */
	private DummyEntity newWine() {
		final var wine = new DummyEntity();
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
		Assertions.assertNull(wine2.getId());
		Assertions.assertEquals(newWine.getName(), wine2.getName());
		Assertions.assertEquals(newWine.getWnePict(), wine2.getWnePict());
		Assertions.assertEquals(newWine.getWneRegn(), wine2.getWneRegn());
		Assertions.assertEquals(newWine.getWneYear(), wine2.getWneYear());
	}

	@Test
	void toJpaForeignKeyInSet() throws IOException {
		final var jpa = csvForJpa.toJpa(DummyEntity2.class, "csv/demo/dummyentity2-collection.csv", true, true);
		Assertions.assertEquals("A", jpa.getFirst().getDialChar());
		Assertions.assertEquals("B", jpa.get(1).getDialChar());
		Assertions.assertEquals(jpa.get(1).getChildren().getFirst(), jpa.getFirst());
		Assertions.assertTrue(jpa.get(1).getLinkedChildren().contains(jpa.getFirst()));
		Assertions.assertEquals("C", jpa.get(2).getDialChar());
		Assertions.assertEquals(jpa.get(2).getChildren().getFirst(), jpa.getFirst());
		Assertions.assertEquals(jpa.get(2).getChildren().get(1), jpa.get(1));
		Assertions.assertTrue(jpa.get(2).getLinkedChildren().contains(jpa.getFirst()));
		Assertions.assertTrue(jpa.get(2).getLinkedChildren().contains(jpa.get(1)));
		Assertions.assertTrue(jpa.get(2).getLinkedChildrenCollection().contains(jpa.getFirst()));
		Assertions.assertTrue(jpa.get(2).getLinkedChildrenCollection().contains(jpa.get(1)));
	}

	@Test
	void toJpaNullForeignKey() throws Exception {
		// Add 2 records having foreign keys 'dialChar' : "A" and null
		csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialChar;children.dialChar\nA\n;A"), true, true);

		// Add a reference to previous records
		final var jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialChar;children.dialChar\nB;A\nC;"),
				true, true);
		Assertions.assertEquals(2, jpa.size());
		Assertions.assertEquals("B", jpa.getFirst().getDialChar());
		Assertions.assertEquals("C", jpa.get(1).getDialChar());
		Assertions.assertNull(jpa.get(1).getChildren());
	}

	@Test
	void toVariableType() throws IOException {
		csvForJpa.insert("csv/demo", DummyNamedEntity.class);
		final var jpa = csvForJpa.toBean(DummyChildClass.class, "csv/demo/dummychildclass.csv");
		Assertions.assertEquals(3, jpa.size());
	}
}
