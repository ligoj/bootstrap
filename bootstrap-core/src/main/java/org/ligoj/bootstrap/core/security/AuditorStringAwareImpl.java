package org.ligoj.bootstrap.core.security;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;

import lombok.Setter;

/**
 * To declare the auditor of current transaction. As default, when there is no declared auditor
 * {@value SecurityHelper#SYSTEM_USERNAME} user name is used.
 */
@Setter
public class AuditorStringAwareImpl implements AuditorAware<String> {

	/**
	 * Optional security helper. If not provided, the default system user will be used.
	 */
	@Autowired(required = false)
	private SecurityHelper securityHelper;

	@Override
	public Optional<String> getCurrentAuditor() {
		// Return the known user
		return Optional.ofNullable(securityHelper == null ? SecurityHelper.SYSTEM_USERNAME
				: StringUtils.defaultIfBlank(securityHelper.getLogin(), SecurityHelper.SYSTEM_USERNAME));
	}

}