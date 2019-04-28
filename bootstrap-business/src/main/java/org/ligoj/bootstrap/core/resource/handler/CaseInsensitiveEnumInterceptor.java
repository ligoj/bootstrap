/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * Case insensitive {@link Enum} flag into the message for FIQL of JAX-RS.
 */
public class CaseInsensitiveEnumInterceptor extends AbstractPhaseInterceptor<Message> {

	/**
	 * Default constructor.
	 */
	public CaseInsensitiveEnumInterceptor() {
		super(Phase.RECEIVE);
	}

	@Override
	public void handleMessage(final Message message) {
		message.put("enum.conversion.case.sensitive", Boolean.TRUE);
	}

}
