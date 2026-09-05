package org.ligoj.bootstrap.resource.system.mfa;

import java.time.Instant;

import org.ligoj.bootstrap.core.NamedBean;

import lombok.Getter;
import lombok.Setter;

/**
 * A registered MFA device, without its secret.
 */
@Getter
@Setter
public class MfaDeviceVo extends NamedBean<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Device type, such as <code>TOTP</code>.
	 */
	private String type;

	/**
	 * Registration date.
	 */
	private Instant createdDate;

	/**
	 * Last successful verification, <code>null</code> when never used.
	 */
	private Instant lastUsed;

	/**
	 * When <code>true</code>, this device is proposed first at verification.
	 */
	private boolean defaultDevice;
}
