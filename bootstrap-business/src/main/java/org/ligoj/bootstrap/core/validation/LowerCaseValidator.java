package org.ligoj.bootstrap.core.validation;

/**
 * Simple validator checking the text is using lower case characters. Numbers are accepted.
 * 
 * @author Fabrice Daugan
 * 
 */
public class LowerCaseValidator extends AbstractCharValidator<LowerCase> {

	@Override
	protected boolean isValidChar(final char c) {
		return !Character.isUpperCase(c);
	}

}