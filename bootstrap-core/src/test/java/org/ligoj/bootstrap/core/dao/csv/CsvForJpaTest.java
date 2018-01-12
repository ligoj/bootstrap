package org.ligoj.bootstrap.core.dao.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.csv.DummyEntity;
import org.ligoj.bootstrap.core.csv.DummyEntity2;
import org.ligoj.bootstrap.core.csv.DummyEntity3;
import org.ligoj.bootstrap.core.csv.DummyEntity4;
import org.ligoj.bootstrap.core.csv.Wrapper;
import org.ligoj.bootstrap.core.resource.TechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Check all CSV to/from JPA entities or simple beans of {@link CsvForJpa}
 * utility.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public class CsvForJpaTest {

	@BeforeClass
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
		Assert.assertFalse(jpa.isEmpty());
	}

	@Test(expected = IOException.class)
	public void toCsvListLocation() throws IOException {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, "csv/__.csv", true);
		Assert.assertFalse(jpa.isEmpty());
	}

	@Test
	public void toCsvEmpty() throws Exception {
		final StringWriter result = new StringWriter();
		csvForJpa.toCsv(new ArrayList<DummyEntity>(), DummyEntity.class, result);

		// Check there is only the header
		Assert.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n", result.toString());

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
		Assert.assertEquals("", result.toString());
	}

	@Test
	public void toCsvNullProperty() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		final DummyEntity newWine = newWine();
		newWine.setWneCnty(null);
		items.add(newWine);
		csvForJpa.toCsv(items, DummyEntity.class, result);

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
		csvForJpa.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assert.assertEquals(
				"id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;\"Château d\"\"Yquem\";\"World, hold on;\";2;3;6;7;8\n",
				result.toString());
	}

	@Test(expected = TechnicalException.class)
	public void toCsvEntityEmptyError() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		items.add(null);
		csvForJpa.toCsvEntity(items, DummyEntity.class, result);
	}

	@Test
	public void toCsv() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		items.add(newWine());
		final StringWriter result = new StringWriter();
		csvForJpa.toCsv(items, DummyEntity.class, result);

		// Check there is the header and one data line
		Assert.assertEquals("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;1;2;3;6;7;8\n", result.toString());
	}

	@Test
	public void toJpa() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("id;name;wneCnty;wneDesc;wneGrpe;wnePict;wneRegn;wneYear\n4;5;1;2;3;6;7;8\n"), true);
		Assert.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test(expected = TechnicalException.class)
	public void toJpaUnknownProperty() throws Exception {
		csvForJpa.toJpa(DummyEntity.class, new StringReader("blah\n4\n"), true);
	}

	@Test(expected = TechnicalException.class)
	public void toJpaInvalidTrailing() throws Exception {
		csvForJpa.toJpa(DummyEntity.class, new StringReader("'8' \t7\n"), false);
	}

	@Test(expected = TechnicalException.class)
	public void toJpaTooMuchValues() throws Exception {
		csvForJpa.toJpa(DummyEntity.class, new StringReader("id\n4;1\n"), true);
	}

	@Test
	public void toJpaTrailing1() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;5;6;7;'8'\n"), true);
		Assert.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaTrailing2() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;5;6;7;'8'\r"), true);
		Assert.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	/**
	 * No header + trailing space.
	 */
	@Test
	public void toJpaTrailing3() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new BufferedReader(new StringReader("9;5;3;1;7;8;6;'2' "), "5;3;1;7;8;6;2;'4' ".length()), false);
		Assert.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaNullValue() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("id;wneCnty;wneDesc;wneGrpe;name;wnePict;wneRegn;wneYear\n4;1;2;3;;6;7;8\n"), true);
		Assert.assertEquals(1, jpa.size());
		final DummyEntity newWine = newWine();
		newWine.setName(null);
		assertEquals(newWine, jpa.get(0));
	}

	/**
	 * Check CSN reader handles well the protected and unprotected quote and the
	 * comma chars.
	 */
	@Test
	public void toJpaSpecialChars() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
				"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"World, hold on\";2;'3\"\"\'\',';4;\"Château d\"\"Yquem\";6;7;8\n"),
				true);
		Assert.assertEquals(1, jpa.size());
		final DummyEntity newWine = newWine();
		newWine.setWneCnty("World, hold on");
		newWine.setName("Château d\"Yquem");
		newWine.setWneGrpe("3\"\"',");
		assertEquals(newWine, jpa.get(0));
	}

	/**
	 * Check CSN reader handles well the protected and unprotected quote and the
	 * comma chars.
	 */
	@Test
	public void toJpaSpecialCharsEnds() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
				"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"World, hold on\";2;'3\"\"\'\',';4;\"Château d\"\"Yquem\";6;7;\"8\n"),
				true);
		Assert.assertEquals(1, jpa.size());
		final DummyEntity newWine = newWine();
		newWine.setWneCnty("World, hold on");
		newWine.setName("Château d\"Yquem");
		newWine.setWneGrpe("3\"\"',");
		assertEquals(newWine, jpa.get(0));
	}

	/**
	 * Check CSN reader handles well the protected and unprotected quote and the
	 * comma chars.
	 */
	@Test
	public void toJpaIgnoreSpaces() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(
				"wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"1\"    ; 2 ; 3;4 ; \t  \"5\";   ' 6 '   ; 7 \"   ;  8   \n"),
				true);
		Assert.assertEquals(1, jpa.size());
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
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new StringReader("wneCnty;wneDesc;wneGrpe;id;name;wnePict;wneRegn;wneYear\n\"World\n, hold\non\";2;3;4;5;6;7;8\n"), true);
		Assert.assertEquals(1, jpa.size());
		final DummyEntity newWine = newWine();
		newWine.setWneCnty("World\n, hold\non");
		assertEquals(newWine, jpa.get(0));
	}

	@Test
	public void toJpaEntity() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader("9;5;3;1;7;8;6;2\n"), false);
		Assert.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaBufferEnd() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class,
				new BufferedReader(new StringReader("9;5;3;1;7;8;6;2\n"), "9;5;3;1;7;8;6;2\n".length()), false);
		Assert.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaNoValues() throws Exception {
		final BufferedReader bufferedReader = new BufferedReader(new StringReader("\n"), 1);
		bufferedReader.read();
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, bufferedReader, false);
		Assert.assertTrue(jpa.isEmpty());
	}

	@Test
	public void toJpaNoValues2() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new BufferedReader(new StringReader("\r")), false);
		Assert.assertTrue(jpa.isEmpty());
	}

	@Test
	public void toJpaBufferEnd2() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new BufferedReader(new StringReader("9;5;3;1;7;8;6;2;\n\n\r")),
				false);
		Assert.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test
	public void toJpaBufferEnd3() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new BufferedReader(new StringReader("\n\r9;5;3;1;7;8;6;2")),
				false);
		Assert.assertEquals(1, jpa.size());
		assertEquals(newWine(), jpa.get(0));
	}

	@Test(expected = TechnicalException.class)
	public void toJpaUnknownClass() throws Exception {
		csvForJpa.toJpa(Integer.class, new StringReader("n\n1\n"), true);
	}

	@Test
	public void toJpaEmpty() throws Exception {
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(""), true);
		Assert.assertTrue(jpa.isEmpty());
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
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(stringBuilder.toString()), true);
		Assert.assertEquals(10000, jpa.size());
	}

	@Test(timeout = 2000)
	public void toJpatoJpaEntityPerformance() throws Exception {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 10000; i-- > 0;) {
			stringBuilder.append("1;4;5;3;7;8;6;2\n");
		}
		final List<DummyEntity> jpa = csvForJpa.toJpa(DummyEntity.class, new StringReader(stringBuilder.toString()), false);
		Assert.assertEquals(10000, jpa.size());
	}

	@Test(timeout = 2000)
	public void toCsvPerformance() throws Exception {
		final List<DummyEntity> items = newWines();
		final StringWriter result = new StringWriter();
		csvForJpa.toCsv(items, DummyEntity.class, result);

		// 160 per lines + 56 for header
		Assert.assertEquals(160000 + 56, result.getBuffer().length());
	}

	@Test(expected = TechnicalException.class)
	public void toJpaMissingValues() throws Exception {
		csvForJpa.toJpa(DummyEntity.class, new StringReader("4;3.5;5;5;5;5;5;5;5;5;5;5\n"), false);
	}

	@Test
	public void toCsvEntity() throws Exception {
		final List<DummyEntity> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		items.add(newWine());
		csvForJpa.toCsvEntity(items, DummyEntity.class, result);

		// Check there is only data
		Assert.assertEquals("4;5;3;1;7;8;6;2\n", result.toString());
	}

	@Test
	public void toCsvEntityJodaTime() throws Exception {
		final List<DummyEntity4> items = new ArrayList<>();
		final StringWriter result = new StringWriter();
		final DummyEntity4 entity = new DummyEntity4();
		entity.setDate(DateTime.parse("2018-01-01"));
		items.add(entity);
		csvForJpa.toCsv(items, DummyEntity4.class, result);

		// Check there is only data (2018-01-01T00:00:00.000+01:00)
		Assert.assertTrue(result.toString().startsWith("date\n2018-01-01T00:00:00.000"));
		
		final List<DummyEntity4> bean = csvForJpa.toBean(DummyEntity4.class, new StringReader(result.toString()));
		Assert.assertEquals(1, bean.size());
		Assert.assertEquals(2018, bean.get(0).getDate().getYear());
	}

	@Test(timeout = 1000)
	public void toCsvEntityPerformance() throws Exception {
		final List<DummyEntity> items = newWines();
		final StringWriter result = new StringWriter();
		csvForJpa.toCsvEntity(items, DummyEntity.class, result);
	}

	@Test
	public void toJpaForeignKey() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, "csv/demo/dummyentity2.csv", true);
		Assert.assertFalse(jpa.isEmpty());
	}

	@Test
	public void toJpaForeignKeyPersist() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, "csv/demo/dummyentity2.csv", true, true);
		Assert.assertFalse(jpa.isEmpty());
		Assert.assertNotNull(jpa.get(0).getId());
	}

	@Test
	public void toJpaForeignKeyNoOptimisation() throws IOException {
		final DummyEntity2 entity = new DummyEntity2();
		em.persist(entity);
		em.flush();
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("link.id!\n1"), true);
		Assert.assertEquals(1, jpa.size());
		Assert.assertEquals(entity.getId(), jpa.get(0).getLink().getId());
	}

	@Test
	public void toJpaForeignKeyRecursive() throws IOException {
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialChar;link.dialChar\nA;\nB;A\nC;B"), true,
				true);
		Assert.assertEquals(3, jpa.size());
	}

	@Test
	public void toJpaForeignKeyNotExist1() throws IOException {
		try {
			csvForJpa.toJpa(DummyEntity2.class, new StringReader("link.id!\n8000"), true);
			Assert.fail("TechnicalException expected");
		} catch (final TechnicalException iau) {
			Assert.assertEquals("Missing foreign key DummyEntity2#link.id = 8000", iau.getCause().getMessage());
		}
	}

	@Test
	public void toJpaEnum() throws IOException {
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialEnum\nALL"), true);
		Assert.assertEquals(1, jpa.size());
	}

	@Test
	public void toJpaEnumIgnoreCase() throws IOException {
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialEnum\naLl"), true);
		Assert.assertEquals(1, jpa.size());
	}

	@Test(expected = TechnicalException.class)
	public void toJpaEnumInvalid() throws IOException {
		csvForJpa.toJpa(DummyEntity2.class, new StringReader("dialEnum\n_ERROR_"), true);
	}

	@Test
	public void toJpaForeignKeyNatural() throws IOException {
		final DummyEntity3 systemUser = new DummyEntity3();
		systemUser.setLogin("test");
		em.persist(systemUser);
		em.flush();
		final List<DummyEntity2> jpa = csvForJpa.toJpa(DummyEntity2.class,
				new StringReader("user.login\n" + systemUser.getLogin() + "\n" + systemUser.getLogin()), true);
		Assert.assertEquals(2, jpa.size());
		Assert.assertEquals("test", jpa.get(0).getUser().getLogin());
	}

	@Test
	public void toJpaForeignKeyNaturalNotExist() throws IOException {
		try {
			csvForJpa.toJpa(DummyEntity2.class, new StringReader("user.login\nnobody"), true);
			Assert.fail("TechnicalException expected");
		} catch (final TechnicalException iau) {
			Assert.assertEquals("Missing foreign key DummyEntity2#user.login = nobody", iau.getCause().getMessage());
		}
	}

	@Test
	public void toJpaForeignKeyNaturalNotExist2() throws IOException {
		try {
			csvForJpa.toJpa(DummyEntity2.class, new StringReader("user.login!\nnobody"), true);
			Assert.fail("TechnicalException expected");
		} catch (final TechnicalException iau) {
			Assert.assertEquals("Missing foreign key DummyEntity2#user.login = nobody", iau.getCause().getMessage());
		}
	}

	@Test
	public void toJpaForeignKeyNotExist2() throws IOException {
		try {
			csvForJpa.toJpa(DummyEntity2.class, new StringReader("link.id\n8000"), true);
			Assert.fail("TechnicalException expected");
		} catch (final TechnicalException iau) {
			Assert.assertEquals("Missing foreign key DummyEntity2#link.id = 8000", iau.getCause().getMessage());
		}
	}

	@Test
	public void toJpaForeignKeyNotExist3() throws IOException {
		try {
			csvForJpa.toJpa(DummyEntity2.class, new StringReader("link.id\n-8000"), true);
			Assert.fail("TechnicalException expected");
		} catch (final TechnicalException iau) {
			Assert.assertEquals("Missing foreign key DummyEntity2#link.id = -8000", iau.getCause().getMessage());
		}
	}

	@Test
	public void getJpaHeaders() {
		final String[] jpaHeaders = csvForJpa.getJpaHeaders(DummyEntity2.class);
		Assert.assertArrayEquals(new String[] { "id", "dialChar", "dialBool", "dialShort", "dialLong", "dialDouble", "dialDate",
				"localDate", "dialEnum", "link", "user" }, jpaHeaders);
	}

	@Test
	public void cleanup() {
		em.persist(new DummyEntity2());
		em.flush();
		Assert.assertEquals(1, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
		csvForJpa.cleanup(DummyEntity2.class);
		Assert.assertEquals(0, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
	}

	@Test
	public void insert() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		csvForJpa.insert("csv/demo", DummyEntity.class);
		Assert.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
	}

	@Test
	public void insert2() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		csvForJpa.insert("csv/demo", DummyEntity.class, StandardCharsets.UTF_8.name());
		Assert.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
	}

	@Test
	public void insertConsummer() throws IOException {
		em.persist(new DummyEntity2());
		em.flush();
		AtomicInteger flag = new AtomicInteger();
		csvForJpa.insert("csv/demo", DummyEntity.class, StandardCharsets.UTF_8.name(), d -> flag.getAndIncrement());
		Assert.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
		Assert.assertEquals(12, flag.get());
	}

	@Test
	public void reset() throws IOException {
		em.persist(new DummyEntity());
		em.flush();
		Assert.assertEquals(1, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
		csvForJpa.reset("csv/demo", DummyEntity.class);
		Assert.assertEquals(12, em.createQuery("FROM " + DummyEntity.class.getSimpleName()).getResultList().size());
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
		Assert.assertEquals(5, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
		csvForJpa.reset("csv/demo/", DummyEntity2.class);
		Assert.assertEquals(5, em.createQuery("FROM " + DummyEntity2.class.getSimpleName()).getResultList().size());
	}

	@Test(expected = FileNotFoundException.class)
	public void resetInvalidPath() throws IOException {
		csvForJpa.reset("csv/__", DummyEntity.class);
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
