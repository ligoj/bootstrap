/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import lombok.Getter;
import lombok.Setter;
import org.ligoj.bootstrap.model.system.HookMatch;
import org.ligoj.bootstrap.model.system.SystemHook;

/**
 * Extension of {@link SystemHook} with parsed matcher.
 */
@Getter
@Setter
public class SystemHookParse extends SystemHook {

	/**
	 * Converted JSON object of <code>match</code> JSON string.
	 */
	private HookMatch matchObject;

}
