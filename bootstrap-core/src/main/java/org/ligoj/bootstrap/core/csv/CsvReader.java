package org.ligoj.bootstrap.core.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.ligoj.bootstrap.core.resource.TechnicalException;

/**
 * CSV raw data reader. Instance is not thread.
 */
public class CsvReader {

	/**
	 * Default separator.
	 */
	public static final char DEFAULT_SEPARATOR = ';';

	// Context
	private char current;
	private char previous;
	private Character wrapper;
	private final StringBuilder value = new StringBuilder(); // NOPMD -- Clear is ensured on read
	private final Reader reader;
	private final List<String> values = new ArrayList<>();
	private char separator;

	/**
	 * Attached reader.
	 * 
	 * @param reader
	 *            the source.
	 */
	public CsvReader(final Reader reader) {
		this(reader, DEFAULT_SEPARATOR);
	}

	/**
	 * Attached reader.
	 * 
	 * @param reader
	 *            the source.
	 * @param separator
	 *            the column separator.
	 */
	public CsvReader(final Reader reader, final char separator) {
		this.reader = reader;
		this.separator = separator;
	}

	/**
	 * Return a bean read from the reader.
	 * 
	 * @return the read bean.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public List<String> read() throws IOException {

		// Initialize the context
		current = (char) reader.read();
		previous = '\0';
		wrapper = null;
		value.setLength(0);
		values.clear();
		readWords();

		// EOF management
		if (value.length() != 0) {
			// Commit last value
			addValue(value.toString());
		}
		return values;
	}

	/**
	 * Read and return words.
	 */
	private void readWords() throws IOException {
		while (current != 65535) {
			// Manage opening and closing wrapper
			if (readWrapper()) {
				break;
			}

			previous = current;
			current = (char) reader.read();
		}
	}

	/**
	 * Read and manage wrapper.
	 */
	private boolean readWrapper() throws IOException {
		// Manage opening and closing wrapper
		if (wrapper == null) {
			// Check possible wrapper start
			return readOutsideWrapper();
		}
		return readInsideWrapper();
	}

	/**
	 * Read inside wrapper.
	 */
	private boolean readInsideWrapper() throws IOException {
		if (current == wrapper) {
			if (previous == wrapper) {
				// Double escape wrapper character
				value.append(current);
				current = '\0';
			}
		} else if (previous == wrapper) {
			// Previous delimiter was not doubled, so cause end of value.
			return endOfWord();
		} else {
			// Normal character to add to current value
			value.append(current);
		}
		return false;
	}

	private boolean endOfWord() throws IOException {
		addValue(value.toString());
		wrapper = null;

		// Ignore next whitespace chars until next separator or EOL
		while (!isEndOfInput()) {
			current = (char) reader.read();
		}
		return current != separator;
	}

	/**
	 * Read outside wrapper.
	 */
	private boolean readOutsideWrapper() {
		if (value.length() == 0 && isWrapper()) {
			// Open simple or double quote wrapper and ignore this character
			wrapper = current;
			current = '\0';
		} else if (current == separator) {
			// Real separator, flush the proceeded value
			addValue(value.toString().trim());
		} else if (isNewLine()) {
			// EOL
			return endOfLine();
		} else if (value.length() > 0 || !Character.isWhitespace(current)) {
			// Non empty char outside wrapper
			// Normal character to add to current value
			value.append(current);
		}
		return false;
	}

	private boolean endOfLine() {
		if (value.length() != 0) {
			// Commit last value
			addValue(value.toString().trim());
			return true;
		}

		return !values.isEmpty();
	}

	/**
	 * Add a new value to the list and reset the buffer.
	 */
	private void addValue(final String word) {
		values.add(word);
		value.setLength(0);
	}

	/**
	 * Is a new line char.
	 */
	private boolean isNewLine() {
		return current == '\n' || current == '\r';
	}

	/**
	 * Is a wrapper char.
	 */
	private boolean isWrapper() {
		return current == Wrapper.QUOTE.getDelimiter() || current == Wrapper.DOUBLE_QUOTE.getDelimiter();
	}

	/**
	 * Is a end of word char.
	 */
	private boolean isEndOfWord() {
		return current == separator || isNewLine() || current == 65535;
	}

	/**
	 * Return End Of Input indicator.
	 */
	private boolean isEndOfInput() {
		if (isEndOfWord()) {
			// Real separator, close definitely the previous word parsing
			return true;
		}
		if (Character.isWhitespace(current)) {
			return false;
		}
		throw new TechnicalException("Invalid character '" + current + "', white space, EOL, EOF or separator was expected near entry " + values);

	}
}
