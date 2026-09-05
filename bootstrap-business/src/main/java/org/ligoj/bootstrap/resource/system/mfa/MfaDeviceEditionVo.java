package org.ligoj.bootstrap.resource.system.mfa;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * TOTP device enrollment: the secret from the setup, confirmed by a first code.
 */
@Getter
@Setter
public class MfaDeviceEditionVo {

	/**
	 * Device name, unique for the user.
	 */
	@NotBlank
	private String name;

	/**
	 * Base32 secret returned by the setup.
	 */
	@NotBlank
	private String secret;

	/**
	 * Current code of the authenticator, proving the secret has been enrolled.
	 */
	@NotBlank
	private String code;
}
