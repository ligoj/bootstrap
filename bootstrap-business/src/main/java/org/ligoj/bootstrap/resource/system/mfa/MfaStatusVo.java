/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.mfa;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * MFA state of the current user.
 */
@Getter
@Setter
public class MfaStatusVo {

	/**
	 * When <code>true</code>, at least one device is registered: a successful verification is required after any
	 * authentication.
	 */
	private boolean required;

	/**
	 * Last authentication of the user, <code>null</code> when unknown.
	 */
	private Instant lastConnection;

	/**
	 * Last successful verification since the last authentication, <code>null</code> when not verified.
	 */
	private Instant verifiedDate;

	/**
	 * Registered devices, without secrets.
	 */
	private List<MfaDeviceVo> devices;
}
