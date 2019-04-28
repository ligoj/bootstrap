/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import jodd.bean.BeanUtil;

/**
 * Test business keyed entities basic ORM operations : hash code and equals.
 */
public abstract class AbstractBusinessEntityTest {

	/**
	 * Bean utility.
	 */
	private static final BeanUtil BEAN_UTIL = BeanUtil.declaredSilent;

	/**
	 * Test equals and hash code operation with all possible combinations
	 *
	 * @param modelClass
	 *            the entity to test.
	 * @throws ReflectiveOperationException
	 *             Due to reflection.
	 * @param <T>
	 *            The type of the entity to test.
	 */
	protected <T> void testEqualsAndHash(final Class<T> modelClass) throws ReflectiveOperationException {
		testEqualsAndHash(modelClass, "id");
	}

	/**
	 * Test equals and hash code operation with all possible combinations
	 *
	 * @param modelClass
	 *            the entity to test.
	 * @param idProperties
	 *            the list of business key parts.
	 * @param <T>
	 *            The type of the entity to test.
	 * @throws ReflectiveOperationException
	 *             due to reflection.
	 */
	protected <T> void testEqualsAndHash(final Class<T> modelClass, final String... idProperties)
			throws ReflectiveOperationException {
		final var systemUser = modelClass.getDeclaredConstructor().newInstance();
		final var systemUser2 = modelClass.getDeclaredConstructor().newInstance();
		Assertions.assertFalse(systemUser.equals(null)); // NOPMD NOSONAR -- for coverage
		Assertions.assertEquals(systemUser, systemUser);
		Assertions.assertEquals(systemUser, systemUser2);
		Assertions.assertFalse(systemUser.equals(1));
		Assertions.assertNotSame(0, systemUser.hashCode());

		// Get all identifier combinations
		final var combinations = combinations(idProperties);

		// For each, compute equality and hash code
		testCombinations(modelClass, combinations);

		// Test inheritance "canEqual" if available (as Scala)
		final var mockCanEqual = Mockito.mock(modelClass);
		systemUser.equals(mockCanEqual);
	}

	private <T> void setValues(final T beanValued, final List<String> combination) {
		for (final var propertyString : combination) {
			BEAN_UTIL.setProperty(beanValued, propertyString, 1);
		}
	}

	private <T> void testCombinations(final Class<T> modelClass, final List<List<String>> combinations)
			throws ReflectiveOperationException {
		for (final var combination : combinations) {
			final var beanValued = modelClass.getDeclaredConstructor().newInstance();
			setValues(beanValued, combination);
			testCombinations(modelClass, combinations, combination, beanValued);
			Assertions.assertNotSame(0, beanValued.hashCode());
		}
	}

	/**
	 * Test the given combinations.
	 */
	private <T> void testCombinations(final Class<T> modelClass, final List<List<String>> combinations,
			final List<String> combination, final T beanValued) throws ReflectiveOperationException {
		for (final var properties : combinations) {
			testCombination(modelClass, combination, beanValued, properties);
		}
	}

	/**
	 * Test the given combination.
	 */
	private <T> void testCombination(final Class<T> modelClass, final List<String> combination, final T beanValued,
			final List<String> properties) throws ReflectiveOperationException {
		final var beanValued2 = modelClass.getDeclaredConstructor().newInstance();
		setValues(beanValued2, properties);
		Assertions.assertEquals(properties.equals(combination), beanValued.equals(beanValued2));
	}

	/**
	 * Generates all combinations and returns them in a list of lists.
	 */
	private List<List<String>> combinations(final String... array) {
		final long count = 2 << array.length - 1;
		final List<List<String>> totalCombinations = new LinkedList<>();

		for (var i = 0; i < count; i++) {
			final List<String> combinations = new LinkedList<>();
			addPropertyCombinations(i, combinations, array);
			totalCombinations.add(combinations);
		}

		return totalCombinations;
	}

	private void addPropertyCombinations(final int i, final List<String> combinations, final String... array) {
		for (var j = 0; j < array.length; j++) {
			if ((i & (1 << j)) != 0) {
				combinations.add(array[j]);
			}
		}
	}
}
