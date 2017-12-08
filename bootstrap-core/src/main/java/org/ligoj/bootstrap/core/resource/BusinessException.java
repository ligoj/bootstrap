package org.ligoj.bootstrap.core.resource;

/**
 * A business exception.
 */
public class BusinessException extends AbstractParameteredException {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Unknown identifier. Ordered parameters are <code>type</code> and <code>id</code>.
	 */
	public static final String KEY_UNKNOW_ID = "unknown-id";

	/**
	 * Constructor with a message.
	 * 
	 * @param message
	 *            message format to display
	 * @param parameters
	 *            optional parameters.
	 */
	public BusinessException(final String message, final Object... parameters) {
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
	public BusinessException(final String message, final Throwable cause, final Object... parameters) {
		super(message, cause, parameters);
	}
}