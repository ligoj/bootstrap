package org.ligoj.bootstrap.http.server;

import org.junit.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import org.ligoj.bootstrap.http.server.Main;

/**
 * Test class of {@link Main}
 *
 */
public class MainTest {

	@Before
	public void init() {
		MainTest.cleanup();
	}

	@After
	public void cleanupInstance() {
		MainTest.cleanup();
	}

	@AfterClass
	public static void cleanup() {
		System.clearProperty("jetty.properties");
		System.clearProperty("jetty.xml");
	}

	/**
	 * Test invalid properties file.
	 */
	@Test
	public void testNoPropertiesFile() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/no.properties");
		final Main main = new Main();
		Assert.assertFalse(main.getServer().isStarting());
		Assert.assertFalse(main.getServer().isStopping());
		Assert.assertFalse(main.getServer().isStarted());
	}

	/**
	 * Test valid properties file.
	 */
	@Test
	public void testCorrectConfiguration() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/jetty-empty-test.properties");
		System.setProperty("jetty.xml", "META-INF/jetty/jetty-test.xml");
		final Main main = new Main();
		Assert.assertFalse(main.getServer().isStarting());
		Assert.assertFalse(main.getServer().isStopping());
		Assert.assertFalse(main.getServer().isStarted());
	}

	/**
	 * Test invalid XML file.
	 */
	@Test
	public void testInvalidXmlFile() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/jetty-empty-test.properties");
		System.setProperty("jetty.xml", "META-INF/jetty/jetty-fail-test.xml");
		try {
			Main.main();
			Assert.fail("Server should faild to start");
		} catch (java.lang.NoSuchMethodException nsme) {
			Assert.assertEquals("class org.eclipse.jetty.server.Server.setUnknownProperty(class java.lang.Object)", nsme.getMessage());
		}
	}

	/**
	 * Test valid XML file.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testKillServer() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/jetty-test.properties");
		System.setProperty("test.test2", "original");
		System.setProperty("jetty.xml", "META-INF/jetty/jetty-test.xml");
		final Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Main.main();
				} catch (final Exception e) {
					Assert.fail("Server failed to start"); // NOSONAR - This a special thread
				}
			}
		});
		thread.start();
		Thread.sleep(1500); // NOSONAR -- Have to pause the thread for the test
		Assert.assertTrue(Main.getLastStartedServer().isStarted());
		thread.stop();
		Assert.assertTrue(Main.getLastStartedServer().isRunning());
		Thread.sleep(500); // NOSONAR -- Have to pause the thread for the test
		Main.getLastStartedServer().stop();
		Assert.assertTrue(Main.getLastStartedServer().isStopped());
	}


	/**
	 * Test valid server start .
	 */
	@Test
	public void testStartServer() throws Exception {
		System.setProperty("jetty.properties", "META-INF/jetty/jetty-test.properties");
		System.setProperty("test.test2", "original");
		System.setProperty("jetty.xml", "META-INF/jetty/jetty-test.xml");
		final Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Main.main();
				} catch (final Exception e) {
					Assert.fail("Server failed to start"); // NOSONAR - This a special thread
				}
			}
		});
		thread.start();
		Thread.sleep(1500); // NOSONAR -- Have to pause the thread for the test
		Assert.assertTrue(Main.getLastStartedServer().isStarted());
		Main.getLastStartedServer().stop();
		Assert.assertTrue(Main.getLastStartedServer().isStopped());
	}
}
