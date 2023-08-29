/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.persistence.criteria.JoinType;
import jakarta.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.json.jqgrid.BasicRule;
import org.ligoj.bootstrap.core.json.jqgrid.BasicRule.RuleOperator;
import org.ligoj.bootstrap.core.json.jqgrid.UIRule;
import org.ligoj.bootstrap.core.json.jqgrid.UiFilter;
import org.ligoj.bootstrap.core.json.jqgrid.UiFilter.FilterOperator;
import org.ligoj.bootstrap.core.json.jqgrid.UiPageRequest;
import org.ligoj.bootstrap.core.json.jqgrid.UiSort;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemDialect;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.model.test.DummyBusinessEntity3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.svenjacobs.loremipsum.LoremIpsum;

/**
 * Test class of {@link PaginationDao} {@link DynamicSpecification}, {@link FetchHelper}, {@link CustomSpecification}
 * and {@link AbstractSpecification}
 */
@ExtendWith(SpringExtension.class)
class PaginationDaoTest extends AbstractBootTest {

	@Autowired
	private PaginationDao paginationDao;

	private static final int COUNT = 50;

	/**
	 * Last know identifier.
	 */
	private int lastKnownEntity;

	@BeforeEach
	void setup() {
		final var loremIpsum = new LoremIpsum();
		var dial1 = new SystemDialect();
		for (var i = 0; i < COUNT; i++) {
			dial1 = new SystemDialect();
			dial1.setDialLong((long) i);
			dial1.setDialChar(loremIpsum.getWords(1, i % 50));
			dial1.setDialDate(new Date(System.currentTimeMillis() + i));
			dial1.setAuthorization(
					i % 7 == 0 ? null : AuthorizationType.values()[i % AuthorizationType.values().length]);
			em.persist(dial1);
		}
		em.flush();
		lastKnownEntity = dial1.getId();
		em.clear();
	}

	/**
	 * Default find all without pagination from {@link UriInfo}.
	 */
	@Test
	void testFindAllUriInfo() {
		final var uriInfo = newUriInfo();
		final var findAll = paginationDao.findAll(SystemDialect.class, uriInfo);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(5, findAll.getTotalPages());
		Assertions.assertEquals(10, findAll.getContent().size());
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all without pagination.
	 */
	@Test
	void testFindAll() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setPage(0);
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, null, null, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(1, findAll.getTotalPages());
		Assertions.assertEquals(COUNT, findAll.getContent().size());
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all (no rule).
	 */
	@Test
	void testFindAll2() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, null, null, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(5, findAll.getTotalPages());
		Assertions.assertEquals(10, findAll.getContent().size());
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all, empty rules.
	 */
	@Test
	void testFindAll3() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		uiPageRequest.getUiFilter().setRules(new ArrayList<>());
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, null, null, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(10, findAll.getContent().size());
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all, empty rules.
	 */
	@Test
	void testFindAll4() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		uiPageRequest.getUiFilter().setRules(new ArrayList<>());
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, null, null, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(10, findAll.getContent().size());
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all, empty rules, with fetch.
	 */
	@Test
	void testFindAllWithFetchMany() {
		final var rulesGroupOr = new ArrayList<UIRule>();
		final var dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		final var ruleEQ = new BasicRule();
		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("link.linkImplicitId");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final var ruleNE = new BasicRule();
		ruleNE.setData(String.valueOf(lastKnownEntity));
		ruleNE.setField("children.dialLong");
		ruleNE.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleNE);

		final var uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final var mapping = new HashMap<String, String>();
		mapping.put("link", "link.id");
		mapping.put("link.linkImplicitId", "link.link");
		mapping.put("children.dialLong", "children.dialLong");
		mapping.put("A.link", "A.link.id");

		final var fetch = new LinkedHashMap<String, JoinType>();
		fetch.put("link", JoinType.LEFT);
		fetch.put("link.link", JoinType.LEFT);
		fetch.put("linkedChildren.link", JoinType.LEFT);

		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				fetch);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getContent().size());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, empty rules, no fetch, and reuse previous join
	 */
	@Test
	void testFindAllJoinsReuse() {
		final var rulesGroupOr = new ArrayList<UIRule>();
		final var dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		final var ruleEQ = new BasicRule();
		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("link.dialLong");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final var ruleNE2 = new BasicRule();
		ruleNE2.setData(String.valueOf(lastKnownEntity));
		ruleNE2.setField("link.dialLong");
		ruleNE2.setOp(RuleOperator.NE);
		rulesGroupOr.add(ruleNE2);

		final var uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final var mapping = new HashMap<String, String>();
		mapping.put("link", "link.id");
		mapping.put("link.dialLong", "link.dialLong");

		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getContent().size());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, empty rules, with sorting but no mapped column.
	 */
	@Test
	void testFindAllWithSorting() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiSort(new UiSort());
		uiPageRequest.setPageSize(10);
		final var mapping = new HashMap<String, String>();
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		assertAll(findAll);
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Find all, empty rules, with sorting, no mapped column, no page size.
	 */
	@Test
	void testFindAllWithSortingNoPageSize() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiSort(new UiSort());
		uiPageRequest.setPageSize(0);
		uiPageRequest.setPage(1);
		final var mapping = new HashMap<String, String>();
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getSize());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(COUNT, findAll.getTotalPages());
		Assertions.assertEquals(1, findAll.getContent().size());
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Find all, empty rules, without sorting, no mapped column, page size and page start.
	 */
	@Test
	void testFindAllWithoutSortingNoPageSize() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setPageSize(10);
		uiPageRequest.setPage(1);
		final var mapping = new HashMap<String, String>();
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		assertAll(findAll);
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Find all, empty rules, with sorting, no mapped column, no start page.
	 */
	@Test
	void testFindAllWithSortingNoPageStart() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiSort(new UiSort());
		uiPageRequest.setPageSize(10);
		uiPageRequest.setPage(0);
		final var mapping = new HashMap<String, String>();
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		assertAll(findAll);
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all, empty rules, with sorting on a mapped column.
	 */
	@Test
	void testFindAllWithSorting2() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiSort(new UiSort());
		uiPageRequest.getUiSort().setColumn("any");
		uiPageRequest.getUiSort().setDirection(Direction.DESC);
		uiPageRequest.setPageSize(10);
		final var mapping = new HashMap<String, String>();
		mapping.put("any", "dialLong");
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		assertAll(findAll);
		Assertions.assertEquals(Long.valueOf(COUNT - 1), findAll.getContent().get(0).getDialLong());
	}

	/**
	 * Default find all, empty rules, with sorting on a mapped raw column.
	 */
	@Test
	void testFindAllWithSortingRawColumn() {
		final var uriInfo = newUriInfo();
		uriInfo.getQueryParameters().putSingle("sidx", "any");
		uriInfo.getQueryParameters().putSingle("sord", "desc");
		final var findAll = paginationDao.findAll(SystemDialect.class, uriInfo, "any:dialLong");
		assertAll(findAll);
		Assertions.assertEquals(Long.valueOf(COUNT - 1), findAll.getContent().get(0).getDialLong());
	}

	/**
	 * Default find all, empty rules, with sorting on a mapped raw column.
	 */
	@Test
	void testFindAllWithSortingRawColumnSimple() {
		final var uriInfo = newUriInfo();
		uriInfo.getQueryParameters().putSingle("sidx", "dialLong");
		uriInfo.getQueryParameters().putSingle("sord", "desc");
		final var findAll = paginationDao.findAll(SystemDialect.class, uriInfo, "dialLong");
		assertAll(findAll);
		Assertions.assertEquals(Long.valueOf(COUNT - 1), findAll.getContent().get(0).getDialLong());
	}

	private void assertAll(final Page<?> findAll) {
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(10, findAll.getSize());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(COUNT / 10, findAll.getTotalPages());
		Assertions.assertEquals(10, findAll.getContent().size());
	}

	/**
	 * Default find all, not mapped column.
	 */
	@Test
	void testFindAllWithRulesNotMappedColumn() {
		final List<UIRule> rules = new ArrayList<>();
		final var ruleEW = new BasicRule();
		ruleEW.setData("t");
		ruleEW.setField("some");
		ruleEW.setOp(RuleOperator.EW);
		rules.add(ruleEW);
		final var uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final var mapping = new HashMap<String, String>();
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(10, findAll.getContent().size());
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all, plenty of string rules : bw, ew, cn
	 */
	@Test
	void testFindAllWithRulesString() {
		final List<UIRule> rules = new ArrayList<>();
		final var ruleEW = new BasicRule();
		ruleEW.setData("t");
		ruleEW.setField("dialChar");
		ruleEW.setOp(RuleOperator.EW);
		rules.add(ruleEW);
		final var ruleBW = new BasicRule();
		ruleBW.setData("e");
		ruleBW.setField("dialChar");
		ruleBW.setOp(RuleOperator.BW);
		rules.add(ruleBW);
		final var ruleCN = new BasicRule();
		ruleCN.setData("s");
		ruleCN.setField("dialChar");
		ruleCN.setOp(RuleOperator.CN);
		rules.add(ruleCN);

		final var uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final var mapping = newBaseMapping();
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(1, findAll.getContent().size());
		// 44th element is "est"
		Assertions.assertEquals("est", findAll.getContent().get(0).getDialChar());
	}

	/**
	 * Default find all, plenty of string rules : lte, gte
	 */
	@Test
	void testFindAllWithRulesRange() {
		final List<UIRule> rules = new ArrayList<>();
		final var ruleLTE = new BasicRule();
		ruleLTE.setData("3");
		ruleLTE.setField("dialLong");
		ruleLTE.setOp(RuleOperator.LTE);
		rules.add(ruleLTE);
		final var ruleGTE = new BasicRule();
		ruleGTE.setData("1");
		ruleGTE.setField("dialLong");
		ruleGTE.setOp(RuleOperator.GTE);
		rules.add(ruleGTE);

		final var uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final var mapping = newBaseMapping();
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(3, findAll.getTotalElements());
		Assertions.assertEquals(3, findAll.getContent().size());
	}

	/**
	 * Default find all, plenty of integer rules and 2 depth grouping : 'eq', 'lt', 'gt', 'ne'.
	 */
	@Test
	void testFindAllWithRules2() {
		final var rulesGroupOr = new ArrayList<UIRule>();
		final var ruleEQ = new BasicRule();
		ruleEQ.setData("Lorem");
		ruleEQ.setField("dialChar");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final var groupAnd = new UiFilter();
		groupAnd.setGroupOp(FilterOperator.AND);
		final List<UIRule> rulesGroupAnd = new ArrayList<>();
		final var ruleNE = new BasicRule();
		ruleNE.setData("20");
		ruleNE.setField("dialLong");
		ruleNE.setOp(RuleOperator.NE);
		rulesGroupAnd.add(ruleNE);
		final var ruleGT = new BasicRule();
		ruleGT.setData("5");
		ruleGT.setField("dialLong");
		ruleGT.setOp(RuleOperator.GT);
		rulesGroupAnd.add(ruleGT);
		final var ruleLT = new BasicRule();
		ruleLT.setData("45");
		ruleLT.setField("dialLong");
		ruleLT.setOp(RuleOperator.LT);
		rulesGroupAnd.add(ruleLT);

		groupAnd.setRules(rulesGroupAnd);
		rulesGroupOr.add(groupAnd);

		final var uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);
		final var mapping = newBaseMapping();
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());

		// LT-GT-1 - 1*NE + 2*EQ
		// Since there are two "amet" out of LT/GT/NE range
		Assertions.assertEquals(Integer.parseInt(ruleLT.getData()) - Integer.parseInt(ruleGT.getData()) - 1 - 1 + 2,
				findAll.getTotalElements());
		Assertions.assertEquals(4, findAll.getTotalPages());
	}

	private Map<String, String> newBaseMapping() {
		final var mapping = new HashMap<String, String>();
		mapping.put("dialLong", "dialLong");
		mapping.put("dialChar", "dialChar");
		return mapping;
	}

	/**
	 * Default find all, EQ comparison on entity instance.
	 */
	@Test
	void testFindAllWithRules3() {
		final var rulesGroupOr = new ArrayList<UIRule>();
		final var ruleEQ = new BasicRule();
		final var dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("link");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final var uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final var mapping = new HashMap<String, String>();
		mapping.put("link", "link.id");
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, EQ comparison on entity instance.
	 */
	@Test
	void testFindAllWithRules4() {
		final var rulesGroupOr = new ArrayList<UIRule>();
		final var ruleEQ = new BasicRule();
		final var dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("link.link");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final var uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final var mapping = new HashMap<String, String>();
		mapping.put("link", "link");
		mapping.put("link.link", "link.link.id");
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Find all based on date comparison.
	 */
	@Test
	void testFindAllWithDateCompare() {
		final var rulesGroupOr = new ArrayList<UIRule>();
		final var ruleEQ = new BasicRule();
		final var dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		ruleEQ.setData(String.valueOf(System.currentTimeMillis() + 2000));
		ruleEQ.setField("dialDate");
		ruleEQ.setOp(RuleOperator.LT);
		rulesGroupOr.add(ruleEQ);

		final var uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final var mapping = new HashMap<String, String>();
		mapping.put("dialDate", "dialDate");
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
	}

	/**
	 * Find all based on auto incremented value comparison.
	 */
	@Test
	void testFindAllWithAutoIncrementCompare() {
		final var rulesGroupOr = new ArrayList<UIRule>();
		final var ruleEQ = new BasicRule();
		final var dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("id");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final var uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final var mapping = new HashMap<String, String>();
		mapping.put("id", "id");
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, EQ comparison on native type property.
	 */
	@Test
	void testFindAllWithRules5() {
		final var rulesGroupOr = new ArrayList<UIRule>();
		final var ruleEQ = new BasicRule();
		final var dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setDialDouble(7);
		em.flush();

		ruleEQ.setData(String.valueOf(7));
		ruleEQ.setField("dialDouble");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final var uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final var mapping = new HashMap<String, String>();
		mapping.put("dialDouble", "dialDouble");
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	private UiPageRequest newOr10() {
		final var uiPageRequest = newOr();
		uiPageRequest.setPageSize(10);
		return uiPageRequest;
	}

	private UiPageRequest newOr() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		uiPageRequest.getUiFilter().setGroupOp(FilterOperator.OR);
		return uiPageRequest;
	}

	/**
	 * Default find all, empty rules with empty data.
	 */
	@Test
	void testFindAllEmpty() {
		final var uiPageRequest = new UiPageRequest();
		final var findAll = paginationDao.findAll(SystemUser.class, uiPageRequest, null, null, null);
		assertEmpty(findAll);
	}

	private void assertEmpty(final Page<SystemUser> findAll) {
		Assertions.assertFalse(findAll.hasContent());
		Assertions.assertEquals(10, findAll.getSize());
		Assertions.assertEquals(0, findAll.getNumberOfElements());
		Assertions.assertEquals(0, findAll.getTotalElements());
		Assertions.assertEquals(0, findAll.getTotalPages());
		Assertions.assertEquals(0, findAll.getContent().size());
	}

	/**
	 * Default find all, empty rules with empty data.
	 */
	@Test
	void testFindAllEmpty2() {
		final var uiPageRequest = new UiPageRequest();
		final var findAll = paginationDao.findAll(SystemUser.class, uiPageRequest, null, null, null);
		assertEmpty(findAll);
	}

	/**
	 * Default find all with custom specification.
	 */
	@Test
	void testFindAllWithCustomSpec() {
		final List<UIRule> rules = new ArrayList<>();
		final var ruleCT = new BasicRule();
		ruleCT.setData("Lorem");
		ruleCT.setField("myCustom");
		ruleCT.setOp(RuleOperator.CT);
		rules.add(ruleCT);

		final var uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final var mapping = newBaseMapping();
		final Map<String, CustomSpecification> specifications = new HashMap<>();
		specifications.put("myCustom", (root, query, cb, rule) -> {
			Assertions.assertEquals(ruleCT.getData(), rule.getData());
			return cb.equal(root.get("dialChar"), rule.getData());
		});
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping,
				specifications, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(2, findAll.getTotalElements());
		Assertions.assertEquals("Lorem", findAll.getContent().get(0).getDialChar());
		Assertions.assertEquals("Lorem", findAll.getContent().get(1).getDialChar());
	}

	private UiPageRequest newAnd10() {
		final var uiPageRequest = newAnd();
		uiPageRequest.setPageSize(10);
		return uiPageRequest;
	}

	private UiPageRequest newAnd() {
		final var uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		uiPageRequest.getUiFilter().setGroupOp(FilterOperator.AND);
		return uiPageRequest;
	}

	/**
	 * Default find all with custom specification.
	 */
	@Test
	void testFindAllWithCustomSpecInvalid() {
		final List<UIRule> rules = new ArrayList<>();
		final var ruleCT = new BasicRule();
		ruleCT.setField("myCustom");
		ruleCT.setOp(RuleOperator.CT);
		rules.add(ruleCT);

		final var uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final var mapping = new HashMap<String, String>();
		final Map<String, CustomSpecification> specifications = new HashMap<>();
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping,
				specifications, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
	}

	/**
	 * Default find all with enumeration filter.
	 */
	@Test
	void testFindAllWithEnumeration() {
		assertEnumeration(AuthorizationType.API.name());
	}

	private void assertEnumeration(final String data) {
		final List<UIRule> rules = new ArrayList<>();
		final var ruleCT = new BasicRule();
		ruleCT.setField("authorization");
		ruleCT.setOp(RuleOperator.EQ);
		ruleCT.setData(data);
		rules.add(ruleCT);

		final var uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final var mapping = new HashMap<String, String>();
		mapping.put("authorization", "authorization");
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(21, findAll.getTotalElements());
		Assertions.assertEquals(AuthorizationType.API, findAll.getContent().get(0).getAuthorization());
	}

	/**
	 * Default find all with enumeration filter.
	 */
	@Test
	void testFindAllWithGenericType() {
		var parent = new DummyBusinessEntity3();
		parent.setId(1900);
		em.persist(parent);
		var son = new DummyBusinessEntity3();
		son.setId(1930);
		son.setParent(parent);
		em.persist(son);
		var sonOfSon = new DummyBusinessEntity3();
		sonOfSon.setId(1960);
		sonOfSon.setParent(son);
		em.persist(sonOfSon);
		em.flush();
		em.clear();

		final var ruleCT = new BasicRule();
		ruleCT.setField("parent2");
		ruleCT.setOp(RuleOperator.EQ);
		ruleCT.setData("1900");

		final var uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(Collections.singletonList(ruleCT));
		final var mapping = new HashMap<String, String>();
		mapping.put("parent2", "parent.parent");
		final var findAll = paginationDao.findAll(DummyBusinessEntity3.class, uiPageRequest,
				mapping, null, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(1960, findAll.getContent().get(0).getId().intValue());
	}

	/**
	 * Default find all with enumeration filter.
	 */
	@Test
	void testFindAllWithEnumerationLowerCase() {
		final List<UIRule> rules = new ArrayList<>();
		final var ruleCT = new BasicRule();
		ruleCT.setField("authorization");
		ruleCT.setOp(RuleOperator.EQ);
		ruleCT.setData(AuthorizationType.API.name().toLowerCase(Locale.ENGLISH));
		rules.add(ruleCT);

		final var uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final var mapping = new HashMap<String, String>();
		mapping.put("*", "*");
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(21, findAll.getTotalElements());
		Assertions.assertEquals(AuthorizationType.API, findAll.getContent().get(0).getAuthorization());
	}

	/**
	 * Default find all with enumeration filter.
	 */
	@Test
	void testFindAllWithEnumerationOrdinal() {
		assertEnumeration(String.valueOf(AuthorizationType.API.ordinal()));
	}

	@Test
	void testToString() {
		final var uiFilter = new UiFilter();
		uiFilter.setGroupOp(FilterOperator.AND);
		final List<UIRule> rules = new ArrayList<>();
		final var ruleCT = new BasicRule();
		ruleCT.setField("authorization");
		ruleCT.setOp(RuleOperator.EQ);
		ruleCT.setData(AuthorizationType.API.name());
		rules.add(ruleCT);
		uiFilter.setRules(rules);

		// Only there for coverage
		Assertions.assertEquals("BasicRule(field=authorization, op=EQ, data=API)", ruleCT.toString());
		Assertions.assertEquals("UiFilter(groupOp=AND, rules=[BasicRule(field=authorization, op=EQ, data=API)])",
				uiFilter.toString());
	}

	@Test
	void testEnum() {
		// Only there for coverage
		RuleOperator.valueOf(RuleOperator.BW.name());
		Assertions.assertEquals("BW", RuleOperator.values()[RuleOperator.valueOf(RuleOperator.BW.name()).ordinal()].name());

		// Only there for coverage
		Assertions.assertEquals("AND", FilterOperator.values()[FilterOperator.valueOf(FilterOperator.AND.name()).ordinal()].name());
	}

	/**
	 * Default find all, empty rules, no fetch, no reuse, multiple join.
	 */
	@Test
	void testFindAllJoins() {
		final List<UIRule> rulesGroup = new ArrayList<>();
		final var dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		var user = new SystemUser();
		user.setLogin("anonymous");
		em.persist(user);
		dialect.setUser(user);
		em.flush();

		final var ruleEQ = new BasicRule();
		ruleEQ.setField("user1");
		ruleEQ.setData("anonymous");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroup.add(ruleEQ);

		final var ruleEQ2 = new BasicRule();
		ruleEQ2.setField("user2");
		ruleEQ2.setData("anonymous");
		ruleEQ2.setOp(RuleOperator.EQ);
		rulesGroup.add(ruleEQ2);

		final var ruleEQ3 = new BasicRule();
		ruleEQ3.setField("user3");
		ruleEQ3.setData("anonymous");
		ruleEQ3.setOp(RuleOperator.EQ);
		rulesGroup.add(ruleEQ3);

		final var uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rulesGroup);
		final var mapping = new HashMap<String, String>();
		mapping.put("user1", "link.user.login");
		mapping.put("user2", "link.user.login");
		mapping.put("user3", "user.login");

		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getContent().size());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, empty rules, with fetch.
	 */
	@Test
	void testFindAllWithFetchAndJoin() {
		final List<UIRule> rulesGroup = new ArrayList<>();
		final var dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		var user = new SystemUser();
		user.setLogin("anonymous");
		em.persist(user);
		dialect.setUser(user);
		em.flush();

		final var ruleEQ = new BasicRule();
		ruleEQ.setField("user");
		ruleEQ.setData("anonymous");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroup.add(ruleEQ);

		final var uiPageRequest = newAnd();
		uiPageRequest.getUiFilter().setRules(rulesGroup);
		uiPageRequest.setPageSize(10);
		final var mapping = new HashMap<String, String>();
		mapping.put("user", "link.user.login");

		final var fetch = new LinkedHashMap<String, JoinType>();
		fetch.put("link.user", JoinType.INNER);
		final var findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				fetch);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getContent().size());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, empty rules, with fetch.
	 */
	@Test
	void testFindAllWithFetchAndJoin2() {
		// Prepare data
		var user1 = new SystemUser();
		user1.setLogin(DEFAULT_USER);
		em.persist(user1);
		var user2 = new SystemUser();
		user2.setLogin(DEFAULT_USER + "b");
		em.persist(user2);
		var role1 = new SystemRole();
		role1.setName(DEFAULT_ROLE);
		em.persist(role1);
		var auth1 = new SystemRoleAssignment();
		auth1.setUser(user1);
		auth1.setRole(role1);
		em.persist(auth1);
		em.flush();

		final List<UIRule> rulesGroup = new ArrayList<>();
		var ruleBW = new BasicRule();
		ruleBW.setField("role");
		ruleBW.setData(DEFAULT_USER);
		ruleBW.setOp(RuleOperator.BW);
		rulesGroup.add(ruleBW);
		ruleBW = new BasicRule();
		ruleBW.setField("login");
		ruleBW.setData(DEFAULT_USER);
		ruleBW.setOp(RuleOperator.BW);
		rulesGroup.add(ruleBW);

		final var sort = new UiSort();
		sort.setColumn("login");
		sort.setDirection(Direction.ASC);
		final var uiPageRequest = newOr();
		uiPageRequest.getUiFilter().setRules(rulesGroup);
		uiPageRequest.setUiSort(sort);
		uiPageRequest.setPageSize(10);
		final var mapping = new HashMap<String, String>();
		mapping.put("role", "roles.role.name");
		mapping.put("login", "login");

		final var fetch = new LinkedHashMap<String, JoinType>();
		fetch.put("roles", JoinType.LEFT);
		final var findAll = paginationDao.findAll(SystemUser.class, uiPageRequest, mapping, null, fetch);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(2, findAll.getContent().size());
		Assertions.assertEquals(DEFAULT_USER, findAll.getContent().get(0).getLogin());
		Assertions.assertEquals(DEFAULT_USER + "b", findAll.getContent().get(1).getLogin());
	}
}
