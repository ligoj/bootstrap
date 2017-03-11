package org.ligoj.bootstrap.core.resource;

import java.io.Serializable;

import lombok.Getter;

/**
 * An exception having optional object parameters.
 */
public abstract class AbstractParameteredException extends RuntimeException {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Optional parameters.
	 */
	@Getter
	private final Serializable[] parameters;

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
	protected AbstractParameteredException(final String message, final Throwable cause, final Serializable... parameters) {
		super(message, cause);
		this.parameters = parameters;
	}

}
