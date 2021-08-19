/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

/**
 * Validate that the string does not contain malicious code.
 *
 * It uses <a href="http://www.jsoup.org">JSoup</a> as the underlying parser/sanitizer library.
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @author Marko Bekhta
 * @author Fabrice Daugan
 */
public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, CharSequence> {
	private final Safelist whitelist = Safelist.relaxed().addProtocols("a", "href", "#");
	private static final String BASE_URI = "https://h.o.s.t";

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		return value == null || new Cleaner(whitelist).isValid(getFragmentAsDocument(value));
	}

	/**
	 * Returns a document whose {@code <body>} element contains the given HTML fragment.
	 */
	private Document getFragmentAsDocument(CharSequence value) {
		// using the XML parser ensures that all elements in the input are retained, also if they actually are not
		// allowed at the given
		// location; E.g. a <td> element isn't allowed directly within the <body> element, so it would be used by the
		// default HTML parser.
		// we need to retain it though to apply the given white list properly; See HV-873
		var fragment = Jsoup.parse(value.toString(), BASE_URI, Parser.xmlParser());
		var document = Document.createShell(BASE_URI);

		// add the fragment's nodes to the body of resulting document
		fragment.childNodes().stream().map(Node::clone).forEach(c -> document.body().appendChild(c));
		return document;
	}
}
