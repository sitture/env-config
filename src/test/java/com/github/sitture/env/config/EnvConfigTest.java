package com.github.sitture.env.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class EnvConfigTest {

	private static final String CONFIG_ENV_KEY = "config.env";
	private static final String CONFIG_KEEPASS_ENABLED_KEY = "config.keepass.enabled";
	private static final String CONFIG_KEEPASS_MASTERKEY_KEY = "config.keepass.masterkey";
	private static final String CONFIG_KEEPASS_PASSWORD = "envconfig";

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setup() {
		EnvConfig.reset();
	}

	@Test
	public void testCanGetDefaultEnvironment() {
		System.setProperty(CONFIG_ENV_KEY, "default");
		Assert.assertEquals("default", EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetEnvironment() {
		System.setProperty(CONFIG_ENV_KEY, "test");
		Assert.assertEquals("test", EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetParsedInt() {
		System.setProperty("property", "123");
		Assert.assertEquals(123, EnvConfig.getInteger("property"));
	}

	@Test
	public void testCanGetParsedBoolean() {
		System.setProperty("property", "true");
		Assert.assertTrue(EnvConfig.getBool("property"));
		System.setProperty("property", "false");
		Assert.assertFalse(EnvConfig.getBool("property"));
		System.setProperty("property", "falssse");
		Assert.assertFalse(EnvConfig.getBool("property"));
	}

	@Test
	public void testCanGetDefaultForNonExistingProperty() {
		Assert.assertEquals("test", EnvConfig.get("non.existing", "test"));
	}

	@Test
	public void testCanGetEnvVarUsingPropertyKey() {
		Assert.assertEquals(System.getenv("PATH"), EnvConfig.get("path"));
	}

	@Test
	public void testNullForNonExistingProperty() {
		Assert.assertNull(EnvConfig.get("non.existing"));
	}

	@Test(expected = MissingVariableException.class)
	public void testMissingVariableExceptionThrown() {
		EnvConfig.get("non.existing", true);
	}

	@Test
	public void testCanAddANewProperty() {
		EnvConfig.add("1", 1);
		Assert.assertEquals(1, EnvConfig.getInteger("1"));
		Assert.assertEquals("1", EnvConfig.get("1"));
		Assert.assertFalse(EnvConfig.getBool("1"));
		EnvConfig.add("1", "1");
		Assert.assertEquals(1, EnvConfig.getInteger("1"));
		Assert.assertEquals("1", EnvConfig.get("1"));
		EnvConfig.add("flag", true);
		Assert.assertTrue(EnvConfig.getBool("flag"));
	}

	@Test
	public void testCanSetProperty() {
		System.setProperty("property", "value");
		Assert.assertEquals("value", EnvConfig.get("property"));
		// re-assign to an existing property
		EnvConfig.set("property", "updatedValue");
		Assert.assertEquals("updatedValue", EnvConfig.get("property"));
		// set a new property
		EnvConfig.set("property2", "value");
		Assert.assertEquals("value", EnvConfig.get("property2"));
	}

	@Test
	public void testCanClearProperty() {
		System.setProperty("property", "value");
		EnvConfig.clear("property");
		Assert.assertNull(System.getProperty("property"));
	}

	@Test
	public void testCanGetEntryFromKeepassDefaultGroup() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "true");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, "default");
		// when my.keepass.property does not exist in default env.
		// and only exists default group of keepass
		Assert.assertEquals("KEEPASS_VALUE", EnvConfig.get("my.keepass.property"));
	}

	@Test
	public void testCanGetEntryFromKeepassDB() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "true");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, "test");
		// when my.keepass.property exists in test env
		// and only exists in default group of keepass
		// then keepass takes priority
		Assert.assertEquals("KEEPASS_VALUE", EnvConfig.get("my.keepass.property"));
	}

	@Test
	public void testCanGetEntryFromKeepassDisabled() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "false");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, "test");
		// when my.keepass.property exists in test env
		// and only exists in default group of keepass
		// then keepass takes priority
		Assert.assertEquals("my_value", EnvConfig.get("my.keepass.property"));
	}

	@Test
	public void testCanGetKeepassOnlyEntry() {
		environmentVariables.set(CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
		environmentVariables.set(CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, "test");
		// when another.property does not exist in test env
		// and exists in test group of keepass
		Assert.assertEquals("ANOTHER_PROPERTY", EnvConfig.get("another.property"));
	}

}
