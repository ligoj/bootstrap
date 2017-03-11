package org.ligoj.bootstrap.core.resource;

import java.io.Serializable;

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
	public BusinessException(final String message, final Serializable... parameters) {
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
	public BusinessException(final String message, final Throwable cause, final Serializable... parameters) {
		super(message, cause, parameters);
	}
}