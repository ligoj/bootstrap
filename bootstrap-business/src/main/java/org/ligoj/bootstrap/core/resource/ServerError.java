package org.ligoj.bootstrap.core.resource;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * A server error to be forwarded to JSON client.
 */
@Getter
public class ServerError {

	/**
	 * User readable error code.
	 */
	@Setter
	private String code;

	/**
	 * Technical message.
	 */
	@Setter
	private String message;

	/**
	 * Optional parameters.
	 */
	@Setter
	private Object[] parameters;

	/**
	 * Optional cause.
	 */
	private ServerError cause;

	/**
	 * Set the {@link Throwable} object attached to this error.
	 * 
	 * @param throwable
	 *            the {@link Throwable} object attached to this error.
	 */
	public void setThrowable(@NotNull final Throwable throwable) {
		this.message = throwable.getMessage();
		if (throwable.getCause() != null && throwable.getCause().getMessage() != null) {
			this.cause = new ServerError();
			this.cause.setThrowable(throwable.getCause());
		}
	}
}
