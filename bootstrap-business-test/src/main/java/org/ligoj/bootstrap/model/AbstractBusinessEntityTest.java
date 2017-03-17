package org.ligoj.bootstrap.model;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;

/**
 * Test business keyed entities basic ORM operations : hash code and equals.
 */
public abstract class AbstractBusinessEntityTest {

	/**
	 * Bean utility.
	 */
	private BeanUtilsBean beanUtilsBean;

	/**
	 * Prepare {@link BeanUtilsBean}
	 */
	@Before
	public void setup() {
		this.beanUtilsBean = BeanUtilsBean.getInstance();
	}

	/**
	 * Test equals and hash code operation with all possible combinations
	 * 
	 * @param modelClass
	 *            the entity to test.
	 * @throws Exception
	 *             due to reflection.
	 * @param <T>
	 *            The type of the entity to test.
	 */
	protected <T> void testEqualsAndHash(final Class<T> modelClass) throws Exception {
		testEqualsAndHash(modelClass, "id");
	}

	/**
	 * Test equals and hash code operation with all possible combinations
	 * 
	 * @param modelClass
	 *            the entity to test.
	 * @param idProperties
	 *            the list of business key parts.
	 * @throws Exception
	 *             due to reflection.
	 * @param <T>
	 *            The type of the entity to test.
	 */
	protected <T> void testEqualsAndHash(final Class<T> modelClass, final String... idProperties)
			throws InstantiationException, IllegalAccessException, InvocationTargetException {
		final T systemUser = modelClass.newInstance();
		final T systemUser2 = modelClass.newInstance();
		Assert.assertFalse(systemUser.equals(null)); // NOPMD NOSONAR -- for coverage
		Assert.assertEquals(systemUser, systemUser);
		Assert.assertEquals(systemUser, systemUser2);
		Assert.assertFalse(systemUser.equals(1));
		Assert.assertNotSame(0, systemUser.hashCode());

		// Get all identifier combinations
		final List<List<String>> combinations = combinations(idProperties);

		// For each, compute equality and hash code
		testCombinations(modelClass, combinations);

		// Test inheritance "canEqual" if available (as Scala)
		final T mockCanEqual = Mockito.mock(modelClass);
		systemUser.equals(mockCanEqual);
	}

	private <T> void testCombinations(final Class<T> modelClass, final List<List<String>> combinations)
			throws InstantiationException, IllegalAccessException, InvocationTargetException {
		for (final List<String> combination : combinations) {
			final T beanValued = modelClass.newInstance();
			setValues(beanValued, combination);
			testCombinations(modelClass, combinations, combination, beanValued);
			Assert.assertNotSame(0, beanValued.hashCode());
		}
	}

	private <T> void setValues(final T beanValued, final List<String> combination) throws IllegalAccessException, InvocationTargetException {
		for (final String propertyString : combination) {
			beanUtilsBean.setProperty(beanValued, propertyString, 1);
		}
	}

	/**
	 * Test the given combinations.
	 */
	private <T> void testCombinations(final Class<T> modelClass, final List<List<String>> combinations, final List<String> combination,
			final T beanValued) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		for (final List<String> properties : combinations) {
			testCombination(modelClass, combination, beanValued, properties);
		}
	}

	/**
	 * Test the given combination.
	 */
	private <T> void testCombination(final Class<T> modelClass, final List<String> combination, final T beanValued, final List<String> properties)
			throws InstantiationException, IllegalAccessException, InvocationTargetException {
		final T beanValued2 = modelClass.newInstance();
		setValues(beanValued2, properties);
		Assert.assertEquals(properties.equals(combination), beanValued.equals(beanValued2));
	}

	/**
	 * Generates all combinations and returns them in a list of lists.
	 */
	private List<List<String>> combinations(final String... array) {
		final long count = 2 << array.length - 1;
		final List<List<String>> totalCombinations = new LinkedList<>();

		for (int i = 0; i < count; i++) {
			final List<String> combinations = new LinkedList<>();
			addPropertyCombinations(i, combinations, array);
			totalCombinations.add(combinations);
		}

		return totalCombinations;
	}

	private void addPropertyCombinations(final int i, final List<String> combinations, final String... array) {
		for (int j = 0; j < array.length; j++) {
			if ((i & (1 << j)) != 0) {
				combinations.add(array[j]);
			}
		}
	}
}
