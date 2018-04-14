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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.UriInfo;

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
public class PaginationDaoTest extends AbstractBootTest {

	@Autowired
	private PaginationDao paginationDao;

	private static final int COUNT = 50;

	/**
	 * Last know identifier.
	 */
	private int lastKnownEntity;

	@BeforeEach
	public void setup() {
		final LoremIpsum loremIpsum = new LoremIpsum();
		SystemDialect dial1 = new SystemDialect();
		for (int i = 0; i < COUNT; i++) {
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
	public void testFindAllUriInfo() {
		final UriInfo uriInfo = newUriInfo();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uriInfo);
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
	public void testFindAll() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setPage(0);
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, null, null, null);
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
	public void testFindAll2() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, null, null, null);
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
	public void testFindAll3() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		uiPageRequest.getUiFilter().setRules(new ArrayList<UIRule>());
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, null, null, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(10, findAll.getContent().size());
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all, empty rules.
	 */
	@Test
	public void testFindAll4() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		uiPageRequest.getUiFilter().setRules(new ArrayList<UIRule>());
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, null, null, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
		Assertions.assertEquals(10, findAll.getContent().size());
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all, empty rules, with fetch.
	 */
	@Test
	public void testFindAllWithFetchMany() {
		final List<UIRule> rulesGroupOr = new ArrayList<>();
		final SystemDialect dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		final BasicRule ruleEQ = new BasicRule();
		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("link.linkImplicitId");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final BasicRule ruleNE = new BasicRule();
		ruleNE.setData(String.valueOf(lastKnownEntity));
		ruleNE.setField("children.dialLong");
		ruleNE.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleNE);

		final UiPageRequest uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final Map<String, String> mapping = new HashMap<>();
		mapping.put("link", "link.id");
		mapping.put("link.linkImplicitId", "link.link");
		mapping.put("children.dialLong", "children.dialLong");
		mapping.put("A.link", "A.link.id");

		final Map<String, JoinType> fetchs = new LinkedHashMap<>();
		fetchs.put("link", JoinType.LEFT);
		fetchs.put("link.link", JoinType.LEFT);
		fetchs.put("linkedChildren.link", JoinType.LEFT);

		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				fetchs);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getContent().size());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, empty rules, no fetch, and reuse previous join
	 */
	@Test
	public void testFindAllJoinsReuse() {
		final List<UIRule> rulesGroupOr = new ArrayList<>();
		final SystemDialect dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		final BasicRule ruleEQ = new BasicRule();
		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("link.dialLong");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final BasicRule ruleNE2 = new BasicRule();
		ruleNE2.setData(String.valueOf(lastKnownEntity));
		ruleNE2.setField("link.dialLong");
		ruleNE2.setOp(RuleOperator.NE);
		rulesGroupOr.add(ruleNE2);

		final UiPageRequest uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final Map<String, String> mapping = new HashMap<>();
		mapping.put("link", "link.id");
		mapping.put("link.dialLong", "link.dialLong");

		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getContent().size());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, empty rules, with sorting but no mapped column.
	 */
	@Test
	public void testFindAllWithSorting() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiSort(new UiSort());
		uiPageRequest.setPageSize(10);
		final Map<String, String> mapping = new HashMap<>();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		assertAll(findAll);
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Find all, empty rules, with sorting, no mapped column, no page size.
	 */
	@Test
	public void testFindAllWithSortingNoPageSize() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiSort(new UiSort());
		uiPageRequest.setPageSize(0);
		uiPageRequest.setPage(1);
		final Map<String, String> mapping = new HashMap<>();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
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
	public void testFindAllWithoutSortingNoPageSize() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setPageSize(10);
		uiPageRequest.setPage(1);
		final Map<String, String> mapping = new HashMap<>();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		assertAll(findAll);
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Find all, empty rules, with sorting, no mapped column, no start page.
	 */
	@Test
	public void testFindAllWithSortingNoPageStart() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiSort(new UiSort());
		uiPageRequest.setPageSize(10);
		uiPageRequest.setPage(0);
		final Map<String, String> mapping = new HashMap<>();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		assertAll(findAll);
		Assertions.assertFalse(findAll.getSort().isSorted());
	}

	/**
	 * Default find all, empty rules, with sorting on a mapped column.
	 */
	@Test
	public void testFindAllWithSorting2() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiSort(new UiSort());
		uiPageRequest.getUiSort().setColumn("any");
		uiPageRequest.getUiSort().setDirection(Direction.DESC);
		uiPageRequest.setPageSize(10);
		final Map<String, String> mapping = new HashMap<>();
		mapping.put("any", "dialLong");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		assertAll(findAll);
		Assertions.assertEquals(Long.valueOf(COUNT - 1), findAll.getContent().get(0).getDialLong());
	}

	/**
	 * Default find all, empty rules, with sorting on a mapped raw column.
	 */
	@Test
	public void testFindAllWithSortingRawColumn() {
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().putSingle("sidx", "any");
		uriInfo.getQueryParameters().putSingle("sord", "desc");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uriInfo, "any:dialLong");
		assertAll(findAll);
		Assertions.assertEquals(Long.valueOf(COUNT - 1), findAll.getContent().get(0).getDialLong());
	}

	/**
	 * Default find all, empty rules, with sorting on a mapped raw column.
	 */
	@Test
	public void testFindAllWithSortingRawColumnSimple() {
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().putSingle("sidx", "dialLong");
		uriInfo.getQueryParameters().putSingle("sord", "desc");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uriInfo, "dialLong");
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
	public void testFindAllWithRulesNotMappedColumn() {
		final List<UIRule> rules = new ArrayList<>();
		final BasicRule ruleEW = new BasicRule();
		ruleEW.setData("t");
		ruleEW.setField("some");
		ruleEW.setOp(RuleOperator.EW);
		rules.add(ruleEW);
		final UiPageRequest uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final Map<String, String> mapping = new HashMap<>();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
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
	public void testFindAllWithRulesString() {
		final List<UIRule> rules = new ArrayList<>();
		final BasicRule ruleEW = new BasicRule();
		ruleEW.setData("t");
		ruleEW.setField("dialChar");
		ruleEW.setOp(RuleOperator.EW);
		rules.add(ruleEW);
		final BasicRule ruleBW = new BasicRule();
		ruleBW.setData("e");
		ruleBW.setField("dialChar");
		ruleBW.setOp(RuleOperator.BW);
		rules.add(ruleBW);
		final BasicRule ruleCN = new BasicRule();
		ruleCN.setData("s");
		ruleCN.setField("dialChar");
		ruleCN.setOp(RuleOperator.CN);
		rules.add(ruleCN);

		final UiPageRequest uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final Map<String, String> mapping = newBaseMapping();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
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
	public void testFindAllWithRulesRange() {
		final List<UIRule> rules = new ArrayList<>();
		final BasicRule ruleLTE = new BasicRule();
		ruleLTE.setData("3");
		ruleLTE.setField("dialLong");
		ruleLTE.setOp(RuleOperator.LTE);
		rules.add(ruleLTE);
		final BasicRule ruleGTE = new BasicRule();
		ruleGTE.setData("1");
		ruleGTE.setField("dialLong");
		ruleGTE.setOp(RuleOperator.GTE);
		rules.add(ruleGTE);

		final UiPageRequest uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final Map<String, String> mapping = newBaseMapping();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(3, findAll.getTotalElements());
		Assertions.assertEquals(3, findAll.getContent().size());
	}

	/**
	 * Default find all, plenty of integer rules and 2 depth grouping : eq, lt, gt, ne.
	 */
	@Test
	public void testFindAllWithRules2() {
		final List<UIRule> rulesGroupOr = new ArrayList<>();
		final BasicRule ruleEQ = new BasicRule();
		ruleEQ.setData("Lorem");
		ruleEQ.setField("dialChar");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final UiFilter groupAnd = new UiFilter();
		groupAnd.setGroupOp(FilterOperator.AND);
		final List<UIRule> rulesGroupAnd = new ArrayList<>();
		final BasicRule ruleNE = new BasicRule();
		ruleNE.setData("20");
		ruleNE.setField("dialLong");
		ruleNE.setOp(RuleOperator.NE);
		rulesGroupAnd.add(ruleNE);
		final BasicRule ruleGT = new BasicRule();
		ruleGT.setData("5");
		ruleGT.setField("dialLong");
		ruleGT.setOp(RuleOperator.GT);
		rulesGroupAnd.add(ruleGT);
		final BasicRule ruleLT = new BasicRule();
		ruleLT.setData("45");
		ruleLT.setField("dialLong");
		ruleLT.setOp(RuleOperator.LT);
		rulesGroupAnd.add(ruleLT);

		groupAnd.setRules(rulesGroupAnd);
		rulesGroupOr.add(groupAnd);

		final UiPageRequest uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);
		final Map<String, String> mapping = newBaseMapping();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());

		// LT-GT-1 - 1*NE + 2*EQ
		// Since there are two "amet" out of LT/GT/NE range
		Assertions.assertEquals(Integer.valueOf(ruleLT.getData()) - Integer.valueOf(ruleGT.getData()) - 1 - 1 + 2,
				findAll.getTotalElements());
		Assertions.assertEquals(4, findAll.getTotalPages());
	}

	private Map<String, String> newBaseMapping() {
		final Map<String, String> mapping = new HashMap<>();
		mapping.put("dialLong", "dialLong");
		mapping.put("dialChar", "dialChar");
		return mapping;
	}

	/**
	 * Default find all, EQ comparison on entity instance.
	 */
	@Test
	public void testFindAllWithRules3() {
		final List<UIRule> rulesGroupOr = new ArrayList<>();
		final BasicRule ruleEQ = new BasicRule();
		final SystemDialect dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("link");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final UiPageRequest uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final Map<String, String> mapping = new HashMap<>();
		mapping.put("link", "link.id");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, EQ comparison on entity instance.
	 */
	@Test
	public void testFindAllWithRules4() {
		final List<UIRule> rulesGroupOr = new ArrayList<>();
		final BasicRule ruleEQ = new BasicRule();
		final SystemDialect dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("link.link");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final UiPageRequest uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final Map<String, String> mapping = new HashMap<>();
		mapping.put("link", "link");
		mapping.put("link.link", "link.link.id");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Find all based on date comparison.
	 */
	@Test
	public void testFindAllWithDateCompare() {
		final List<UIRule> rulesGroupOr = new ArrayList<>();
		final BasicRule ruleEQ = new BasicRule();
		final SystemDialect dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		ruleEQ.setData(String.valueOf(System.currentTimeMillis() + 2000));
		ruleEQ.setField("dialDate");
		ruleEQ.setOp(RuleOperator.LT);
		rulesGroupOr.add(ruleEQ);

		final UiPageRequest uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final Map<String, String> mapping = new HashMap<>();
		mapping.put("dialDate", "dialDate");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
	}

	/**
	 * Find all based on auto incremented value comparison.
	 */
	@Test
	public void testFindAllWithAutoIncrementCompare() {
		final List<UIRule> rulesGroupOr = new ArrayList<>();
		final BasicRule ruleEQ = new BasicRule();
		final SystemDialect dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		em.flush();

		ruleEQ.setData(String.valueOf(lastKnownEntity));
		ruleEQ.setField("id");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final UiPageRequest uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final Map<String, String> mapping = new HashMap<>();
		mapping.put("id", "id");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, EQ comparison on native type property.
	 */
	@Test
	public void testFindAllWithRules5() {
		final List<UIRule> rulesGroupOr = new ArrayList<>();
		final BasicRule ruleEQ = new BasicRule();
		final SystemDialect dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setDialDouble(7);
		em.flush();

		ruleEQ.setData(String.valueOf(7));
		ruleEQ.setField("dialDouble");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroupOr.add(ruleEQ);

		final UiPageRequest uiPageRequest = newOr10();
		uiPageRequest.getUiFilter().setRules(rulesGroupOr);

		final Map<String, String> mapping = new HashMap<>();
		mapping.put("dialDouble", "dialDouble");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	private UiPageRequest newOr10() {
		final UiPageRequest uiPageRequest = newOr();
		uiPageRequest.setPageSize(10);
		return uiPageRequest;
	}

	private UiPageRequest newOr() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		uiPageRequest.getUiFilter().setGroupOp(FilterOperator.OR);
		return uiPageRequest;
	}

	/**
	 * Default find all, empty rules with empty data.
	 */
	@Test
	public void testFindAllEmpty() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		final Page<SystemUser> findAll = paginationDao.findAll(SystemUser.class, uiPageRequest, null, null, null);
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
	public void testFindAllEmpty2() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		final Page<SystemUser> findAll = paginationDao.findAll(SystemUser.class, uiPageRequest, null, null, null);
		assertEmpty(findAll);
	}

	/**
	 * Default find all with custom specification.
	 */
	@Test
	public void testFindAllWithCustomSpec() {
		final List<UIRule> rules = new ArrayList<>();
		final BasicRule ruleCT = new BasicRule();
		ruleCT.setData("Lorem");
		ruleCT.setField("myCustom");
		ruleCT.setOp(RuleOperator.CT);
		rules.add(ruleCT);

		final UiPageRequest uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final Map<String, String> mapping = newBaseMapping();
		final Map<String, CustomSpecification> specifications = new HashMap<>();
		specifications.put("myCustom", new CustomSpecification() {

			@Override
			public Predicate toPredicate(final Root<?> root, final CriteriaQuery<?> query, final CriteriaBuilder cb,
					final BasicRule rule) {
				Assertions.assertEquals(ruleCT.getData(), rule.getData());
				return cb.equal(root.get("dialChar"), rule.getData());
			}

		});
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping,
				specifications, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(2, findAll.getTotalElements());
		Assertions.assertEquals("Lorem", findAll.getContent().get(0).getDialChar());
		Assertions.assertEquals("Lorem", findAll.getContent().get(1).getDialChar());
	}

	private UiPageRequest newAnd10() {
		final UiPageRequest uiPageRequest = newAnd();
		uiPageRequest.setPageSize(10);
		return uiPageRequest;
	}

	private UiPageRequest newAnd() {
		final UiPageRequest uiPageRequest = new UiPageRequest();
		uiPageRequest.setUiFilter(new UiFilter());
		uiPageRequest.getUiFilter().setGroupOp(FilterOperator.AND);
		return uiPageRequest;
	}

	/**
	 * Default find all with custom specification.
	 */
	@Test
	public void testFindAllWithCustomSpecInvalid() {
		final List<UIRule> rules = new ArrayList<>();
		final BasicRule ruleCT = new BasicRule();
		ruleCT.setField("myCustom");
		ruleCT.setOp(RuleOperator.CT);
		rules.add(ruleCT);

		final UiPageRequest uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final Map<String, String> mapping = new HashMap<>();
		final Map<String, CustomSpecification> specifications = new HashMap<>();
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping,
				specifications, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(COUNT, findAll.getTotalElements());
	}

	/**
	 * Default find all with enumeration filter.
	 */
	@Test
	public void testFindAllWithEnumeration() {
		assertEnumeration(AuthorizationType.API.name());
	}

	private void assertEnumeration(final String data) {
		final List<UIRule> rules = new ArrayList<>();
		final BasicRule ruleCT = new BasicRule();
		ruleCT.setField("authorization");
		ruleCT.setOp(RuleOperator.EQ);
		ruleCT.setData(data);
		rules.add(ruleCT);

		final UiPageRequest uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final Map<String, String> mapping = new HashMap<>();
		mapping.put("authorization", "authorization");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(21, findAll.getTotalElements());
		Assertions.assertEquals(AuthorizationType.API, findAll.getContent().get(0).getAuthorization());
	}

	/**
	 * Default find all with enumeration filter.
	 */
	@Test
	public void testFindAllWithGenericType() {
		DummyBusinessEntity3 parent = new DummyBusinessEntity3();
		parent.setId(1900);
		em.persist(parent);
		DummyBusinessEntity3 son = new DummyBusinessEntity3();
		son.setId(1930);
		son.setParent(parent);
		em.persist(son);
		DummyBusinessEntity3 sonOfSon = new DummyBusinessEntity3();
		sonOfSon.setId(1960);
		sonOfSon.setParent(son);
		em.persist(sonOfSon);
		em.flush();
		em.clear();

		final BasicRule ruleCT = new BasicRule();
		ruleCT.setField("parent2");
		ruleCT.setOp(RuleOperator.EQ);
		ruleCT.setData("1900");

		final UiPageRequest uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(Collections.singletonList(ruleCT));
		final Map<String, String> mapping = new HashMap<>();
		mapping.put("parent2", "parent.parent");
		final Page<DummyBusinessEntity3> findAll = paginationDao.findAll(DummyBusinessEntity3.class, uiPageRequest,
				mapping, null, null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getTotalElements());
		Assertions.assertEquals(1960, findAll.getContent().get(0).getId().intValue());
	}

	/**
	 * Default find all with enumeration filter.
	 */
	@Test
	public void testFindAllWithEnumerationLowerCase() {
		final List<UIRule> rules = new ArrayList<>();
		final BasicRule ruleCT = new BasicRule();
		ruleCT.setField("authorization");
		ruleCT.setOp(RuleOperator.EQ);
		ruleCT.setData(AuthorizationType.API.name().toLowerCase(Locale.ENGLISH));
		rules.add(ruleCT);

		final UiPageRequest uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rules);
		final Map<String, String> mapping = new HashMap<>();
		mapping.put("*", "*");
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(21, findAll.getTotalElements());
		Assertions.assertEquals(AuthorizationType.API, findAll.getContent().get(0).getAuthorization());
	}

	/**
	 * Default find all with enumeration filter.
	 */
	@Test
	public void testFindAllWithEnumerationOrdinal() {
		assertEnumeration(String.valueOf(AuthorizationType.API.ordinal()));
	}

	@Test
	public void testToString() {
		final UiFilter uiFilter = new UiFilter();
		uiFilter.setGroupOp(FilterOperator.AND);
		final List<UIRule> rules = new ArrayList<>();
		final BasicRule ruleCT = new BasicRule();
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
	public void testEnum() {
		// Only there for coverage
		RuleOperator.values();
		RuleOperator.valueOf(RuleOperator.BW.name());

		// Only there for coverage
		FilterOperator.values();
		FilterOperator.valueOf(FilterOperator.AND.name());
	}

	/**
	 * Default find all, empty rules, no fetch, no reuse, multiple join.
	 */
	@Test
	public void testFindAllJoins() {
		final List<UIRule> rulesGroup = new ArrayList<>();
		final SystemDialect dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		SystemUser user = new SystemUser();
		user.setLogin("anonymous");
		em.persist(user);
		dialect.setUser(user);
		em.flush();

		final BasicRule ruleEQ = new BasicRule();
		ruleEQ.setField("user1");
		ruleEQ.setData("anonymous");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroup.add(ruleEQ);

		final BasicRule ruleEQ2 = new BasicRule();
		ruleEQ2.setField("user2");
		ruleEQ2.setData("anonymous");
		ruleEQ2.setOp(RuleOperator.EQ);
		rulesGroup.add(ruleEQ2);

		final BasicRule ruleEQ3 = new BasicRule();
		ruleEQ3.setField("user3");
		ruleEQ3.setData("anonymous");
		ruleEQ3.setOp(RuleOperator.EQ);
		rulesGroup.add(ruleEQ3);

		final UiPageRequest uiPageRequest = newAnd10();
		uiPageRequest.getUiFilter().setRules(rulesGroup);
		final Map<String, String> mapping = new HashMap<>();
		mapping.put("user1", "link.user.login");
		mapping.put("user2", "link.user.login");
		mapping.put("user3", "user.login");

		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				null);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getContent().size());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, empty rules, with fetch.
	 */
	@Test
	public void testFindAllWithFetchAndJoin() {
		final List<UIRule> rulesGroup = new ArrayList<>();
		final SystemDialect dialect = em.find(SystemDialect.class, lastKnownEntity);
		dialect.setLink(dialect);
		SystemUser user = new SystemUser();
		user.setLogin("anonymous");
		em.persist(user);
		dialect.setUser(user);
		em.flush();

		final BasicRule ruleEQ = new BasicRule();
		ruleEQ.setField("user");
		ruleEQ.setData("anonymous");
		ruleEQ.setOp(RuleOperator.EQ);
		rulesGroup.add(ruleEQ);

		final UiPageRequest uiPageRequest = newAnd();
		uiPageRequest.getUiFilter().setRules(rulesGroup);
		uiPageRequest.setPageSize(10);
		final Map<String, String> mapping = new HashMap<>();
		mapping.put("user", "link.user.login");

		final Map<String, JoinType> fetchs = new LinkedHashMap<>();
		fetchs.put("link.user", JoinType.INNER);
		final Page<SystemDialect> findAll = paginationDao.findAll(SystemDialect.class, uiPageRequest, mapping, null,
				fetchs);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(1, findAll.getContent().size());
		Assertions.assertEquals(Integer.valueOf(lastKnownEntity), findAll.getContent().get(0).getId());
	}

	/**
	 * Default find all, empty rules, with fetch.
	 */
	@Test
	public void testFindAllWithFetchAndJoin2() {
		// Prepare data
		SystemUser user1 = new SystemUser();
		user1.setLogin(DEFAULT_USER);
		em.persist(user1);
		SystemUser user2 = new SystemUser();
		user2.setLogin(DEFAULT_USER + "b");
		em.persist(user2);
		SystemRole role1 = new SystemRole();
		role1.setName(DEFAULT_ROLE);
		em.persist(role1);
		SystemRoleAssignment auth1 = new SystemRoleAssignment();
		auth1.setUser(user1);
		auth1.setRole(role1);
		em.persist(auth1);
		em.flush();

		final List<UIRule> rulesGroup = new ArrayList<>();
		BasicRule ruleBW = new BasicRule();
		ruleBW.setField("role");
		ruleBW.setData(DEFAULT_USER);
		ruleBW.setOp(RuleOperator.BW);
		rulesGroup.add(ruleBW);
		ruleBW = new BasicRule();
		ruleBW.setField("login");
		ruleBW.setData(DEFAULT_USER);
		ruleBW.setOp(RuleOperator.BW);
		rulesGroup.add(ruleBW);

		final UiSort sort = new UiSort();
		sort.setColumn("login");
		sort.setDirection(Direction.ASC);
		final UiPageRequest uiPageRequest = newOr();
		uiPageRequest.getUiFilter().setRules(rulesGroup);
		uiPageRequest.setUiSort(sort);
		uiPageRequest.setPageSize(10);
		final Map<String, String> mapping = new HashMap<>();
		mapping.put("role", "roles.role.name");
		mapping.put("login", "login");

		final Map<String, JoinType> fetchs = new LinkedHashMap<>();
		fetchs.put("roles", JoinType.LEFT);
		final Page<SystemUser> findAll = paginationDao.findAll(SystemUser.class, uiPageRequest, mapping, null, fetchs);
		Assertions.assertTrue(findAll.hasContent());
		Assertions.assertEquals(2, findAll.getContent().size());
		Assertions.assertEquals(DEFAULT_USER, findAll.getContent().get(0).getLogin());
		Assertions.assertEquals(DEFAULT_USER + "b", findAll.getContent().get(1).getLogin());
	}
}
