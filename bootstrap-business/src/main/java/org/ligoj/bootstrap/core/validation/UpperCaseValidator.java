package org.ligoj.bootstrap.core.validation;

/**
 * Simple validator checking the text is fully capitalized. Numbers are accepted.
 * 
 * @author Fabrice Daugan
 * 
 */
public class UpperCaseValidator extends AbstractCharValidator<UpperCase> {

	@Override
	protected boolean isValidChar(final char c) {
		return !Character.isLowerCase(c);
	}

}