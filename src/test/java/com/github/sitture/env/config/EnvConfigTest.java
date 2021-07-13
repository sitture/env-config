package com.github.sitture.env.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.util.Arrays;
import java.util.List;

public class EnvConfigTest {

	private static final String CONFIG_ENV_KEY = "config.env";
	private static final String CONFIG_KEEPASS_ENABLED_KEY = "config.keepass.enabled";
	private static final String CONFIG_KEEPASS_MASTERKEY_KEY = "config.keepass.masterkey";
	private static final String CONFIG_KEEPASS_PASSWORD = "envconfig";
	private static final String TEST_ENVIRONMENT = "test";
	private static final String DEFAULT_ENVIRONMENT = "default";
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
		System.setProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT);
		Assert.assertEquals(DEFAULT_ENVIRONMENT, EnvConfig.getEnvironment());
		// when env not set
		System.clearProperty(CONFIG_ENV_KEY);
		Assert.assertEquals(DEFAULT_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetEnvironment() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assert.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetEnvironmentWhenMultiple() {
		System.setProperty(CONFIG_ENV_KEY, "default , test");
		Assert.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test(expected = ConfigException.class)
	public void testExceptionWhenEnvMissing() {
		System.setProperty(CONFIG_ENV_KEY, "default, non-existing ");
		Assert.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetEnvironmentWithASpace() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT + " ");
		Assert.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetProperty() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assert.assertEquals("my_value", EnvConfig.get("my.property"));
	}

	@Test
	public void testDoesNotGetsPropertyFromSubDirs() {
		System.setProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT);
		Assert.assertNull(EnvConfig.get("property.sub.dir"));
	}

	@Test
	public void testCanGetPropertyWhenMultipleEnv() {
		final String testEnv = "test-env";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		Assert.assertEquals(testEnv, EnvConfig.getEnvironment());
		// When a property.one exists in all environments, including default
		Assert.assertEquals(testEnv, EnvConfig.get("property.one"));
		// When a property.two exists in current environment only
		Assert.assertEquals(testEnv, EnvConfig.get("property.two"));
		// When a property.three does not exist in current environment
		Assert.assertEquals("test", EnvConfig.get("property.three"));
		// When a property.four exists in default environment only
		Assert.assertEquals(DEFAULT_ENVIRONMENT, EnvConfig.get("property.four"));
	}

	@Test
	public void testCanGetFromEntryWhenEnvVarAndDefaultValueSame() {
		environmentVariables.set("property.five", "default");
		final String testEnv = "test-env";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		// when property.five is set as env variable
		// and does not exist in test-env
		// and exists in test env
		// and exists in default env with same value as env var
		// then value in test env takes priority
		Assert.assertEquals("test", EnvConfig.get("property.five"));
	}

	@Test
	public void testCanGetFromEntryWhenEnvVarAndDefaultValueDifferent() {
		environmentVariables.set("property.five", "env.default");
		final String testEnv = "test-env";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		// when property.five is set as env variable
		// and does not exist in test-env
		// and exists in test env
		// and exists in default env with different value to env var
		// then value in env var takes priority
		Assert.assertEquals("env.default", EnvConfig.get("property.five"));
	}

	@Test
	public void testCanGetFromEntryWhenEnvVarSet() {
		environmentVariables.set("property.six", "env.property.six");
		final String testEnv = "test-env";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		// when property.six is set as env variable
		// and exists in test-env env
		// and exists in test env
		// and exists in default env with different value to env var
		// then value in env var takes priority
		Assert.assertEquals("env.property.six", EnvConfig.get("property.six"));
	}

	@Test
	public void testCanGetPropertyFromEnvVars() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		environmentVariables.set("MY_ENV_PROPERTY", "my_env_value");
		Assert.assertEquals("my_env_value", EnvConfig.get("my.env.property"));
		Assert.assertEquals("my_env_value", EnvConfig.get("MY_ENV_PROPERTY"));

		Assert.assertEquals("my_value", EnvConfig.get("my.property"));
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
	public void testCanGetParsedList() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assert.assertTrue(EnvConfig.getList(TEST_PROPERTY).isEmpty());
		System.setProperty(TEST_PROPERTY, "env");
		Assert.assertEquals(1, EnvConfig.getList(TEST_PROPERTY).size());
		System.setProperty(TEST_PROPERTY, "env, config");
		final List<String> actualList = EnvConfig.getList(TEST_PROPERTY);
		Assert.assertEquals(2, actualList.size());
		final List<String> expectedList = Arrays.asList("env", "config");
		Assert.assertEquals(expectedList, actualList);
	}

	@Test
	public void testCanGetParsedListWithDelimiter() {
		final String delimiter = ";";
		final String property = "test.property";
		Assert.assertTrue(EnvConfig.getList(property, delimiter).isEmpty());
		System.setProperty(property, "env;");
		Assert.assertEquals(1, EnvConfig.getList(property, delimiter).size());
		System.setProperty(property, "env;config ");
		final List<String> actualList = EnvConfig.getList(property, delimiter);
		Assert.assertEquals(2, actualList.size());
		final List<String> expectedList = Arrays.asList("env", "config");
		Assert.assertEquals(expectedList, actualList);
	}

	@Test
	public void testCanGetDefaultForNonExistingProperty() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assert.assertEquals("test", EnvConfig.get("non.existing", "test"));
	}

	@Test
	public void testCanGetEnvVarUsingPropertyKey() {
		Assert.assertEquals(System.getenv("PATH"), EnvConfig.get("path"));
	}

	@Test
	public void testNullForNonExistingProperty() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assert.assertNull(EnvConfig.get("non.existing"));
	}

	@Test(expected = MissingVariableException.class)
	public void testMissingVariableExceptionThrown() {
		EnvConfig.getOrThrow("non.existing");
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
		System.setProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT);
		// when my.keepass.property does not exist in default env.
		// and only exists default group of keepass
		Assert.assertEquals(KEEPASS_VALUE, EnvConfig.get("my.keepass.property"));
		Assert.assertEquals(KEEPASS_VALUE, EnvConfig.get("MY_KEEPASS_PROPERTY"));
	}

	@Test
	public void testCanGetEntryFromKeepassDBWhenMultipleEnvironment() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "true");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		final String testEnv = "keepass";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		// when my.keepass.property exists in test env
		// and only exists in keepass env group of keepass
		// then keepass takes priority
		Assert.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get("my.keepass.property"));
		Assert.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get("MY_KEEPASS_PROPERTY"));
	}

	@Test
	public void testCanGetPropertyFromKeepassWhenMultipleEnv() {
		environmentVariables.set(CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
		environmentVariables.set(CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
		environmentVariables.set("MY_KEEPASS_PROPERTY", "KEEPASS_ENV_VALUE");
//		environmentVariables.set("PROPERTY_ONE", "KEEPASS_ENV");
		final String testEnv = "test-no-keepass";
		System.setProperty(CONFIG_ENV_KEY, "keepass," + testEnv);
		Assert.assertEquals(testEnv, EnvConfig.getEnvironment());
		// When a property.one exists in all environments, including default
		// Then keepass takes priority
		Assert.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get("my.keepass.property"));
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
