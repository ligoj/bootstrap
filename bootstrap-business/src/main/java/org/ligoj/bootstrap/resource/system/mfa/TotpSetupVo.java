package org.ligoj.bootstrap.resource.system.mfa;

import lombok.Getter;
import lombok.Setter;

/**
 * A new TOTP secret to enroll: shown as a QR code (URI) or typed (secret), then confirmed with a first code. Nothing
 * is persisted until the confirmation.
 */
@Getter
@Setter
public class TotpSetupVo {

	/**
	 * Base32 secret.
	 */
	private String secret;

	/**
	 * <code>otpauth://totp/...</code> URI, the QR code content.
	 */
	private String uri;

	/**
	 * Issuer shown by the authenticator application.
	 */
	private String issuer;

	/**
	 * Account shown by the authenticator application: the user login.
	 */
	private String account;
}
