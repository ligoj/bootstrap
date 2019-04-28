/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link Main}
 *
 */
class MainTest {

	@BeforeEach
    void init() {
		MainTest.cleanup();
	}

	@AfterEach
    void cleanupInstance() {
		MainTest.cleanup();
	}

	@AfterAll
    static void cleanup() {
		System.clearProperty("jetty.properties");
		System.clearProperty("jetty.xml");
	}

	/**
	 * Test invalid properties file.
	 */
	@Test
    void testNoPropertiesFile() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/no.properties");
		final var main = new Main();
		Assertions.assertFalse(main.getServer().isStarting());
		Assertions.assertFalse(main.getServer().isStopping());
		Assertions.assertFalse(main.getServer().isStarted());
	}

	/**
	 * Test valid properties file.
	 */
	@Test
    void testCorrectConfiguration() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/jetty-empty-test.properties");
		System.setProperty("jetty.xml", "META-INF/jetty/jetty-test.xml");
		final var main = new Main();
		Assertions.assertFalse(main.getServer().isStarting());
		Assertions.assertFalse(main.getServer().isStopping());
		Assertions.assertFalse(main.getServer().isStarted());
	}

	/**
	 * Test invalid XML file.
	 */
	@Test
    void testInvalidXmlFile() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/jetty-empty-test.properties");
		System.setProperty("jetty.xml", "META-INF/jetty/jetty-fail-test.xml");
		try {
			Main.main();
			Assertions.fail("Server should failed to start");
		} catch (java.lang.NoSuchMethodException e) {
			Assertions.assertEquals("class org.eclipse.jetty.server.Server.setUnknownProperty(class java.lang.Object)", e.getMessage());
		}
	}

	/**
	 * Test valid XML file.
	 */
	@SuppressWarnings("deprecation")
	@Test
    void testKillServer() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/jetty-test.properties");
		System.setProperty("test.test2", "original");
		System.setProperty("jetty.xml", "META-INF/jetty/jetty-test.xml");
		final var thread = new Thread(() -> {
			try {
				Main.main();
			} catch (final Exception e) {
				Assertions.fail("Server failed to start"); // NOSONAR - This a special thread
			}
		});
		thread.start();
		Thread.sleep(1500); // NOSONAR -- Have to pause the thread for the test
		Assertions.assertTrue(Main.getLastStartedServer().isStarted());
		thread.stop();
		Assertions.assertTrue(Main.getLastStartedServer().isRunning());
		Thread.sleep(500); // NOSONAR -- Have to pause the thread for the test
		Main.getLastStartedServer().stop();
		Assertions.assertTrue(Main.getLastStartedServer().isStopped());
	}


	/**
	 * Test valid server start .
	 */
	@Test
    void testStartServer() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/jetty-test.properties");
		System.setProperty("test.test2", "original");
		System.setProperty("jetty.xml", "META-INF/jetty/jetty-test.xml");
		final var thread = new Thread(() -> {
			try {
				Main.main();
			} catch (final Exception e) {
				Assertions.fail("Server failed to start"); // NOSONAR - This a special thread
			}
		});
		thread.start();
		Thread.sleep(1500); // NOSONAR -- Have to pause the thread for the test
		Assertions.assertTrue(Main.getLastStartedServer().isStarted());
		Main.getLastStartedServer().stop();
		Assertions.assertTrue(Main.getLastStartedServer().isStopped());
	}
}
