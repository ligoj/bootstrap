/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Fake command to handle test of process builder invocation.
 */
class Main {

	/**
	 * Only the Java process bootstrap.
	 *
	 * @param strings The options. The first parameter, when defined and with the form like <code>error=$code</code>
	 *                will be used to generate an exit code <code>System.exit($code)</code>.
	 */
	public static void main(final String... strings) throws IOException {
		// Basic log to check the I/O
		System.out.println("Message standard : " + String.join(",", strings));
		System.err.println("Message error : " + String.join(",", strings));
		System.out.println("Message payload.env : " + System.getenv("PAYLOAD"));
		final var file = new File("hook.log");
		try (final var fw = new FileWriter(file)) {
			fw.write(System.getenv("PAYLOAD"));
			fw.flush();
			System.out.println("Written to " + file.getAbsoluteFile());
		}
		// No error code
		System.exit(2);
	}
}
