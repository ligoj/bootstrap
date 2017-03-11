package org.ligoj.bootstrap.core.resource;

import java.io.Serializable;

/**
 * A technical exception.
 */
public class TechnicalException extends AbstractParameteredException {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor with a message.
	 * 
	 * @param message
	 *            message format to display
	 * @param parameters
	 *            optional parameters.
	 */
	public TechnicalException(final String message, final Serializable... parameters) {
		this(message, null, parameters);
	}

	/**
	 * Constructor with a message.
	 * 
	 * @param message
	 *            message format to display.
	 * @param cause
	 *            exception's cause.
	 * @param parameters
	 *            optional parameters.
	 */
	public TechnicalException(final String message, final Throwable cause, final Serializable... parameters) {
		super(message, cause, parameters);
	}
}