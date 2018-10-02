package com.sitture.env.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class ConfigTest {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setup() {
		environmentVariables.clear("ENV", "ENV_DIR");
	}

	@Test
	public void testCanGetDefaultEnvironment() {
		System.setProperty("env", "default");
		Assert.assertEquals("default", Config.getEnvironment());
	}

	@Test
	public void testCanGetEnvironment() {
		System.setProperty("env", "ios");
		Assert.assertEquals("ios", Config.getEnvironment());
	}

	@Test
	public void testCanGetParsedInt() {
		System.setProperty("property", "123");
		Assert.assertEquals(123, Config.getInteger("property"));
	}

	@Test
	public void testCanGetParsedBoolean() {
		System.setProperty("property", "true");
		Assert.assertTrue(Config.getBool("property"));
		System.setProperty("property", "false");
		Assert.assertFalse(Config.getBool("property"));
		System.setProperty("property", "falssse");
		Assert.assertFalse(Config.getBool("property"));
	}

	@Test
	public void testCanGetDefaultForNonExistingProperty() {
		Assert.assertEquals("test", Config.get("non.existing", "test"));
	}

	@Test
	public void testCanGetEnvVarUsingPropertyKey() {
		Assert.assertEquals(System.getenv("PATH"), Config.get("path"));
	}

	@Test
	public void testNullForNonExistingProperty() {
		Assert.assertNull(Config.get("non.existing"));
	}

	@Test(expected = MissingVariableException.class)
	public void testMissingVariableExceptionThrown() {
		Config.get("non.existing", true);
	}

	@Test
	public void testCanGetDefaultDriverConfiguration() {
		System.setProperty("env", "default");
		Assert.assertEquals("web", Config.driver().getEnvironment());
	}

	@Test
	public void testCanGetDriverConfigurationWithOverride() {
		System.setProperty("env", "test");
		Assert.assertEquals("appium", Config.driver().getEnvironment());
	}

	@Test
	public void testCanGetDefaultBrowserConfiguration() {
		System.setProperty("env", "default");
		Assert.assertEquals("phantomjs", Config.browser().getName());
	}

	@Test
	public void testCanGetDriverConfigurationWithOverrideFromDefaultProperties() {
		System.setProperty("env", "test");
		Assert.assertEquals("firefox", Config.browser().getName());
	}

	@Test
	public void testCanGetDriverConfigurationWithOverrideFromBrowserProperties() {
		System.setProperty("env", "test2");
		Assert.assertEquals("chrome", Config.browser().getName());
	}

	@Test
	public void testCanGetConfigWhenNoDefaultFileWithOnlyEnvProperitesFile() {
		System.setProperty("env", "ios");
		// When these are set in ios/ios.properties
		Assert.assertEquals("firefoxy", Config.browser().getName());
		Assert.assertFalse(Config.browser().isHeadless());
		Assert.assertEquals(15, Config.driver().getTimeout());
		Assert.assertEquals("test", Config.driver().getEnvironment());
		Assert.assertEquals("iPhone", Config.appium().getDeviceName());
		// When this is set to true in default/default.properties
		Assert.assertTrue(Config.appium().isFullReset());
		// When not set, gets default
		Assert.assertTrue(Config.appium().isNoReset());
	}

	@Test
	public void testCanGetPropertyFromEnvVariable() {
		System.setProperty("env", "ios");
		environmentVariables.set("DEVICE_NAME", "TEST DEVICE");
		environmentVariables.set("FULL_RESET", "true");
		Assert.assertEquals("TEST DEVICE", Config.appium().getDeviceName());
		Assert.assertTrue(Config.appium().isFullReset());
	}

	@Test
	public void testCanGetBrowserHeadlessFromEnvVariable() throws MalformedURLException {
		System.setProperty("env", "test");
		environmentVariables.set("BROWSER_BROWSERNAME", "testbrowser");
		environmentVariables.set("BROWSER_HEADLESS", "true");
		environmentVariables.set("BROWSER_REMOTE", "true");
		final String url = "https://samp";
		environmentVariables.set("BROWSER_HUB_URL", url);
		Assert.assertTrue(Config.browser().isHeadless());
		Assert.assertTrue(Config.browser().isRemote());
		Assert.assertEquals("testbrowser", Config.browser().getName());
		Assert.assertEquals(new URL(url), Config.browser().getHubUrl());
	}

	@Test
	public void testCanGetBrowserHeadlessFromProperties() {
		System.setProperty("env", "test");
		Assert.assertTrue(Config.browser().isHeadless());
		Assert.assertFalse(Config.browser().isRemote());
		Assert.assertNull(Config.browser().getHubUrl());
	}

}
