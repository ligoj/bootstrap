package org.ligoj.bootstrap.resource.system.mfa;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Passkey assertion: the credential returned by the browser (<code>navigator.credentials.get</code>), fields Base64url
 * encoded.
 */
@Getter
@Setter
public class PasskeyAssertionVo {

	/**
	 * Credential identifier (<code>credential.id</code>).
	 */
	@NotBlank
	private String id;

	/**
	 * <code>response.clientDataJSON</code>.
	 */
	@NotBlank
	private String clientDataJSON;

	/**
	 * <code>response.authenticatorData</code>.
	 */
	@NotBlank
	private String authenticatorData;

	/**
	 * <code>response.signature</code>.
	 */
	@NotBlank
	private String signature;
}
