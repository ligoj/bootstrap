package org.ligoj.bootstrap.core.resource;

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
	public TechnicalException(final String message, final Object... parameters) {
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
	public TechnicalException(final String message, final Throwable cause, final Object... parameters) {
		super(message, cause, parameters);
	}
}