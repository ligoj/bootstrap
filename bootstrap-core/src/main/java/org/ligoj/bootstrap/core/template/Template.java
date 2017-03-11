package org.ligoj.bootstrap.core.template;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;

/**
 * A simple template able to manage loop, raw data, and computed data. <T> Root context data type
 * 
 * @param <T>
 *            the context type.
 */
public class Template<T> {

	private static final int MAX_TAG_LENGTH = 100;

	/**
	 * The template input.
	 */
	private final String input;

	/**
	 * Build a new template.
	 * 
	 * @param input
	 *            The template definition.
	 */
	public Template(final String input) {
		this.input = input;
	}

	/**
	 * Write to the given writer the proceeded template with given tag processors.
	 * 
	 * @param writer
	 *            the merged template and data target.
	 * @param tags
	 *            tag processors.
	 * @param rootContext
	 *            Root context.
	 * @throws IOException
	 *             Unable to write the data.
	 */
	public void write(final Writer writer, final Map<String, Processor<?>> tags, final T rootContext) throws IOException {
		final Deque<Object> contextData = new LinkedList<>();
		contextData.add(rootContext);
		write(writer, tags, 0, input.length(), contextData);
		writer.flush();
	}

	/**
	 * 
	 * Write to the given writer the proceeded template with given tag processors.
	 * 
	 * @param writer
	 *            the merged template and data target.
	 * @param tags
	 *            tag processors.
	 * @param start
	 *            The template position start index.
	 * @param end
	 *            The template position end index.
	 */
	private void write(final Writer writer, final Map<String, Processor<?>> tags, final int start, final int end, final Deque<Object> contextData)
			throws IOException {
		int cursor = start;
		TagContext tagContext = getNextTag(tags, cursor, end);
		while (tagContext != null) {
			// New tag found
			writer.write(input.substring(cursor, tagContext.cursor));
			if (tagContext.collection) {
				final int closingTag = input.indexOf("{{/" + tagContext.tag + "}}", tagContext.cursor);
				if (closingTag == -1 || closingTag >= end) {
					throw new IllegalStateException("Closing tag {{/" + tagContext.tag + "}} not found");
				}
				writeCollection(writer, tags, tagContext, closingTag, contextData);
				cursor = closingTag + 5 + tagContext.tag.length();
			} else {
				writeData(writer, tagContext, contextData);
				cursor = tagContext.cursor + 4 + tagContext.tag.length();
			}
			tagContext = getNextTag(tags, cursor, end);
		}

		// End of template
		writer.write(input.substring(cursor, end));
	}

	/**
	 * Write a collection tag.
	 * 
	 * @param writer
	 *            the merged template and data target.
	 * @param tags
	 *            tag processors.
	 * @param tagContext
	 *            The context of the current tag.
	 * @param end
	 *            The template position end index.
	 * @param contextData
	 *            Bean context stack.
	 */
	private void writeCollection(final Writer writer, final Map<String, Processor<?>> tags, final TagContext tagContext, final int end,
			final Deque<Object> contextData) throws IOException {
		final Object parent = tagContext.processor.getValue(contextData);

		// Check nullability -> empty list
		if (parent != null) {
			if (parent.getClass().isArray()) {
				// Array case
				int index = 0;
				for (final Object value : (Object[]) parent) {
					contextData.add(index++);
					writeItem(writer, tags, tagContext, end, contextData, value);
					contextData.removeLast();
				}
			} else if (parent instanceof Iterable<?>) {
				// Collection case
				int index = 0;
				for (final Object value : (Collection<?>) parent) {
					contextData.add(index++);
					writeItem(writer, tags, tagContext, end, contextData, value);
					contextData.removeLast();
				}
			} else {
				// Bean case
				writeItem(writer, tags, tagContext, end, contextData, parent);
			}
		}
	}

	/**
	 * Write a collection item.
	 */
	private void writeItem(final Writer writer, final Map<String, Processor<?>> tags, final TagContext tagContext, final int end,
			final Deque<Object> contextData, final Object value) throws IOException {
		contextData.add(value);
		write(writer, tags, tagContext.cursor + tagContext.tag.length() + 4, end, contextData);
		contextData.removeLast();
	}

	/**
	 * 
	 * Write to the given writer the proceeded template with given tag processors.
	 * 
	 * @param writer
	 *            the merged template and data target.
	 * @param tagContext
	 *            The context of the current tag.
	 * @param contextData
	 *            Bean context stack.
	 */
	private void writeData(final Writer writer, final TagContext tagContext, final Deque<Object> contextData) throws IOException {
		final Object data = getRawData(tagContext, contextData);
		if (data != null) {
			writer.write(data.toString());
		}
	}

	/**
	 * Get the raw data.
	 */
	private Object getRawData(final TagContext tagContext, final Deque<Object> contextData) {
		return tagContext.processor.getValue(contextData);
	}

	/**
	 * Find and check the next tag marker, validate the syntax, find the corresponding {@link Processor} and return the
	 * corresponding context of this match.Is <code>null</code> when no tag has been found.
	 */
	private TagContext getNextTag(final Map<String, Processor<?>> tags, final int start, final int end) {
		final int nextTag = input.indexOf("{{", start);
		if (nextTag == -1 || nextTag >= end) {
			// End of template
			return null;
		}
		final int nextEndTag = input.indexOf("}}", nextTag);
		if (nextEndTag == -1 || nextEndTag >= end) {
			// Opening tag syntax
			throw new IllegalStateException("Invalid opening tag syntax '{{' without '}}' at position " + nextTag);
		}
		if (nextEndTag - nextTag > MAX_TAG_LENGTH) {
			// Too long tag
			throw new IllegalStateException("Too long (max is " + MAX_TAG_LENGTH + " tag " + input.substring(nextTag + 2, nextTag + 30)
					+ "...}} found at position " + start);
		}
		final String tag = input.substring(nextTag + 2, nextEndTag);
		final String tagClean = StringUtils.removeEnd(tag, "/");
		if (StringUtils.trimToEmpty(tagClean).length() == 0) {
			// Empty tag
			throw new IllegalStateException("Empty tag {{}} found at position " + start);
		}
		final Processor<?> processor = tags.get(tagClean);
		if (processor == null) {
			throw new IllegalStateException("Not mapped template tag {{" + tagClean + "}} found at position " + nextTag);
		}
		return new TagContext(nextTag, processor, tag, tagClean.length() == tag.length());
	}

	@AllArgsConstructor
	private class TagContext {

		private int cursor;
		private Processor<?> processor;
		private String tag;
		private boolean collection;
	}

}
