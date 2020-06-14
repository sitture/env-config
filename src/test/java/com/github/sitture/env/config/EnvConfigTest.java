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
	private static final String TEST_ENVIRONMENT = "test";
	private static final String TEST_PROPERTY = "property";
	private static final String TEST_VALUE = "value";
	private static final String KEEPASS_VALUE = "KEEPASS_VALUE";

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setUp() {
		EnvConfig.reset();
	}

	@Test
	public void testCanGetDefaultEnvironment() {
		System.setProperty(CONFIG_ENV_KEY, "default");
		Assert.assertEquals("default", EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetEnvironment() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assert.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetProperty() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assert.assertEquals("my_value", EnvConfig.get("my.property"));
	}

	@Test
	public void testCanGetPropertyFromEnvVars() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		environmentVariables.set("MY_PROPERTY", "my_env_value");
		Assert.assertEquals("my_env_value", EnvConfig.get("my.property"));
		Assert.assertEquals("my_env_value", EnvConfig.get("MY_PROPERTY"));
	}

	@Test
	public void testCanGetParsedInt() {
		System.setProperty(TEST_PROPERTY, "123");
		Assert.assertEquals(123, EnvConfig.getInteger(TEST_PROPERTY));
	}

	@Test
	public void testCanGetParsedBoolean() {
		System.setProperty(TEST_PROPERTY, "true");
		Assert.assertTrue(EnvConfig.getBool(TEST_PROPERTY));
		System.setProperty(TEST_PROPERTY, "false");
		Assert.assertFalse(EnvConfig.getBool(TEST_PROPERTY));
		System.setProperty(TEST_PROPERTY, "falssse");
		Assert.assertFalse(EnvConfig.getBool(TEST_PROPERTY));
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
		System.setProperty(TEST_PROPERTY, TEST_VALUE);
		Assert.assertEquals(TEST_VALUE, EnvConfig.get(TEST_PROPERTY));
		// re-assign to an existing property
		EnvConfig.set(TEST_PROPERTY, "updatedValue");
		Assert.assertEquals("updatedValue", EnvConfig.get(TEST_PROPERTY));
		// set a new property
		EnvConfig.set("property2", TEST_VALUE);
		Assert.assertEquals(TEST_VALUE, EnvConfig.get("property2"));
	}

	@Test
	public void testCanClearProperty() {
		System.setProperty(TEST_PROPERTY, TEST_VALUE);
		EnvConfig.clear(TEST_PROPERTY);
		Assert.assertNull(System.getProperty(TEST_PROPERTY));
	}

	@Test
	public void testCanGetEntryFromKeepassDefaultGroup() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "true");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, "default");
		// when my.keepass.property does not exist in default env.
		// and only exists default group of keepass
		Assert.assertEquals(KEEPASS_VALUE, EnvConfig.get("my.keepass.property"));
		Assert.assertEquals(KEEPASS_VALUE, EnvConfig.get("MY_KEEPASS_PROPERTY"));
	}

	@Test
	public void testCanGetEntryFromKeepassDB() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "true");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		// when my.keepass.property exists in test env
		// and only exists in default group of keepass
		// then keepass takes priority
		Assert.assertEquals(KEEPASS_VALUE, EnvConfig.get("my.keepass.property"));
		Assert.assertEquals(KEEPASS_VALUE, EnvConfig.get("MY_KEEPASS_PROPERTY"));
	}

	@Test
	public void testCanGetEntryFromKeepassDisabled() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "false");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		// when my.keepass.property exists in test env
		// and only exists in default group of keepass
		// then keepass takes priority
		Assert.assertEquals("my_value", EnvConfig.get("my.keepass.property"));
	}

	@Test
	public void testCanGetKeepassOnlyEntry() {
		environmentVariables.set(CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
		environmentVariables.set(CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		// when another.property does not exist in test env
		// and exists in test group of keepass
		Assert.assertEquals("ANOTHER_PROPERTY", EnvConfig.get("another.property"));
	}

	@Test
	public void testCanGetKeepassOnlyEntryWhenEntryWithTrailingSpace() {
		environmentVariables.set(CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
		environmentVariables.set(CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		// when another.property does not exist in test env
		// and exists in test group of keepass
		Assert.assertEquals(KEEPASS_VALUE, EnvConfig.get("trailing.space.property"));
	}

}
