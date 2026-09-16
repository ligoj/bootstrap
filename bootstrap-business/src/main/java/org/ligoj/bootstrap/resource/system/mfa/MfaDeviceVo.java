package org.ligoj.bootstrap.resource.system.mfa;

import java.time.Instant;
import java.util.List;

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

	/**
	 * Passkey only: transports reported by the browser at registration (<code>usb</code>, <code>nfc</code>,
	 * <code>ble</code>, <code>smart-card</code>, <code>internal</code>, <code>hybrid</code>), <code>null</code> when
	 * unknown.
	 */
	private List<String> transports;

	/**
	 * Passkey only: <code>platform</code> or <code>cross-platform</code>, <code>null</code> when unknown.
	 */
	private String attachment;

	/**
	 * Passkey only: the authenticator AAGUID (UUID form), <code>null</code> when the authenticator did not disclose it.
	 */
	private String aaguid;

	/**
	 * Passkey only: the authenticator model derived from the AAGUID (e.g. "YubiKey 5 NFC"), <code>null</code> when
	 * unknown.
	 */
	private String model;
}
