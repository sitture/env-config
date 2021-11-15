package com.github.sitture.env.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigTest {

	private static final String CONFIG_ENV_KEY = "config.env";
	private static final String CONFIG_KEEPASS_ENABLED_KEY = "config.keepass.enabled";
	private static final String CONFIG_KEEPASS_MASTERKEY_KEY = "config.keepass.masterkey";
	private static final String CONFIG_KEEPASS_PASSWORD = "envconfig";
	private static final String TEST_ENVIRONMENT = "test";
	private static final String DEFAULT_ENVIRONMENT = "default";
	private static final String TEST_PROPERTY = "property";
	private static final String TEST_VALUE = "value";
	private static final String KEEPASS_VALUE = "KEEPASS_VALUE";

	@SystemStub
	private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@BeforeEach
	void setUp() throws Exception {
		System.clearProperty("config.keepass.filename");
		EnvConfig.reset();
	}

	@Test
	void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		System.setProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT);
		final Constructor<EnvConfig> constructor = EnvConfig.class.getDeclaredConstructor();
		Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	void testStaticAndSingleton() {
		Assertions.assertEquals(EnvConfig.getConfig().hashCode(), EnvConfig.getConfig().hashCode());
	}

	@Test
	void testCanGetDefaultEnvironment() {
		System.setProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT);
		Assertions.assertEquals(DEFAULT_ENVIRONMENT, EnvConfig.getEnvironment());
		// when env not set
		System.clearProperty(CONFIG_ENV_KEY);
		Assertions.assertEquals(DEFAULT_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test
	void testCanGetEnvironment() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assertions.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test
	void testCanGetEnvironmentWhenMultiple() {
		System.setProperty(CONFIG_ENV_KEY, "default , test");
		Assertions.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test
	void testExceptionWhenEnvMissing() {
		System.setProperty(CONFIG_ENV_KEY, "default, non-existing ");
		final ConfigException exception = Assertions.assertThrows(ConfigException.class,
				EnvConfig::getEnvironment);
		Assertions.assertTrue(exception.getMessage()
				.endsWith("/env-config/config/non-existing' does not exist or not a valid config directory!"));
	}

	@Test
	void testCanGetEnvironmentWithASpace() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT + " ");
		Assertions.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
	}

	@Test
	void testCanGetProperty() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assertions.assertEquals("my_value", EnvConfig.get("my.property"));
	}

	@Test
	void testCanGetPropertyFromGetOrThrow() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assertions.assertEquals("my_value", EnvConfig.getOrThrow("my.property"));
	}

	@Test
	void testDoesNotGetsPropertyFromSubDirs() {
		System.setProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT);
		Assertions.assertNull(EnvConfig.get("property.sub.dir"));
	}

	@Test
	void testCanGetPropertyWhenMultipleEnv() {
		final String testEnv = "test-env";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		Assertions.assertEquals(testEnv, EnvConfig.getEnvironment());
		// When a property.one exists in all environments, including default
		Assertions.assertEquals(testEnv, EnvConfig.get("property.one"));
		// When a property.two exists in current environment only
		Assertions.assertEquals(testEnv, EnvConfig.get("property.two"));
		// When a property.three does not exist in current environment
		Assertions.assertEquals("test", EnvConfig.get("property.three"));
		// When a property.four exists in default environment only
		Assertions.assertEquals(DEFAULT_ENVIRONMENT, EnvConfig.get("property.four"));
	}

	@Test
	void testCanGetFromEntryWhenEnvVarAndDefaultValueSame() {
		environmentVariables.set("property.five", "default");
		final String testEnv = "test-env";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		// when property.five is set as env variable
		// and does not exist in test-env
		// and exists in test env
		// and exists in default env with same value as env var
		// then value in test env takes priority
		Assertions.assertEquals("test", EnvConfig.get("property.five"));
	}

	@Test
	void testCanGetFromEntryWhenEnvVarAndDefaultValueDifferent() {
		environmentVariables.set("property.five", "env.default");
		final String testEnv = "test-env";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		// when property.five is set as env variable
		// and does not exist in test-env
		// and exists in test env
		// and exists in default env with different value to env var
		// then value in env var takes priority
		Assertions.assertEquals("env.default", EnvConfig.get("property.five"));
	}

	@Test
	void testCanGetFromEntryWhenEnvVarSet() {
		environmentVariables.set("property.six", "env.property.six");
		final String testEnv = "test-env";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		// when property.six is set as env variable
		// and exists in test-env env
		// and exists in test env
		// and exists in default env with different value to env var
		// then value in env var takes priority
		Assertions.assertEquals("env.property.six", EnvConfig.get("property.six"));
	}

	@Test
	void testCanGetFromEntryWhenEnvVarSetInMultiEnvs() {
		final String testEnv = "test-env";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		// when property.seven is not set in test-env
		// and exists in test env in environment variable format. i.e. PROPERTY_SEVEN=test
		// and exists in default env with different value i.e. PROPERTY_SEVEN=default
		// then i should be able to value from test env using the properties format
		Assertions.assertEquals("test", EnvConfig.get("property.seven"));
	}

	@Test
	void testCanGetPropertyFromEnvVars() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		environmentVariables.set("MY_ENV_PROPERTY", "my_env_value");
		Assertions.assertEquals("my_env_value", EnvConfig.get("my.env.property"));
		Assertions.assertEquals("my_env_value", EnvConfig.get("MY_ENV_PROPERTY"));

		Assertions.assertEquals("my_value", EnvConfig.get("my.property"));
	}

	@Test
	void testCanGetParsedInt() {
		System.setProperty(TEST_PROPERTY, "123");
		Assertions.assertEquals(123, EnvConfig.getInteger(TEST_PROPERTY));
	}

	@Test
	void testCanGetParsedBoolean() {
		System.setProperty(TEST_PROPERTY, "true");
		Assertions.assertTrue(EnvConfig.getBool(TEST_PROPERTY));
		System.setProperty(TEST_PROPERTY, "false");
		Assertions.assertFalse(EnvConfig.getBool(TEST_PROPERTY));
		System.setProperty(TEST_PROPERTY, "falssse");
		Assertions.assertFalse(EnvConfig.getBool(TEST_PROPERTY));
	}

	@Test
	void testCanGetParsedList() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assertions.assertTrue(EnvConfig.getList(TEST_PROPERTY).isEmpty());
		System.setProperty(TEST_PROPERTY, "env");
		Assertions.assertEquals(1, EnvConfig.getList(TEST_PROPERTY).size());
		System.setProperty(TEST_PROPERTY, "env, config");
		final List<String> actualList = EnvConfig.getList(TEST_PROPERTY);
		Assertions.assertEquals(2, actualList.size());
		final List<String> expectedList = Arrays.asList("env", "config");
		Assertions.assertEquals(expectedList, actualList);
	}

	@Test
	void testCanGetParsedListWithDelimiter() {
		final String delimiter = ";";
		final String property = "test.property";
		Assertions.assertTrue(EnvConfig.getList(property, delimiter).isEmpty());
		System.setProperty(property, "env;");
		Assertions.assertEquals(1, EnvConfig.getList(property, delimiter).size());
		System.setProperty(property, "env;config ");
		final List<String> actualList = EnvConfig.getList(property, delimiter);
		Assertions.assertEquals(2, actualList.size());
		final List<String> expectedList = Arrays.asList("env", "config");
		Assertions.assertEquals(expectedList, actualList);
	}

	@Test
	void testCanGetDefaultForNonExistingProperty() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assertions.assertEquals("test", EnvConfig.get("non.existing", "test"));
	}

	@Test
	void testCanGetEnvVarUsingPropertyKey() {
		Assertions.assertEquals(System.getenv("PATH"), EnvConfig.get("path"));
	}

	@Test
	void testNullForNonExistingProperty() {
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		Assertions.assertNull(EnvConfig.get("non.existing"));
	}

	@Test
	void testMissingVariableExceptionThrown() {
		final MissingVariableException exception = Assertions.assertThrows(MissingVariableException.class,
				() -> EnvConfig.getOrThrow("non.existing"));
		Assertions.assertEquals("Missing required variable 'non.existing'", exception.getMessage());
	}

	@Test
	void testCanAddANewProperty() {
		EnvConfig.add("1", 1);
		Assertions.assertEquals(1, EnvConfig.getInteger("1"));
		Assertions.assertEquals("1", EnvConfig.get("1"));
		Assertions.assertFalse(EnvConfig.getBool("1"));
		EnvConfig.add("1", "1");
		Assertions.assertEquals(1, EnvConfig.getInteger("1"));
		Assertions.assertEquals("1", EnvConfig.get("1"));
		EnvConfig.add("flag", true);
		Assertions.assertTrue(EnvConfig.getBool("flag"));
	}

	@Test
	void testCanSetProperty() {
		System.setProperty(TEST_PROPERTY, TEST_VALUE);
		Assertions.assertEquals(TEST_VALUE, EnvConfig.get(TEST_PROPERTY));
		// re-assign to an existing property
		EnvConfig.set(TEST_PROPERTY, "updatedValue");
		Assertions.assertEquals("updatedValue", EnvConfig.get(TEST_PROPERTY));
		// set a new property
		EnvConfig.set("property2", TEST_VALUE);
		Assertions.assertEquals(TEST_VALUE, EnvConfig.get("property2"));
	}

	@Test
	void testCanClearProperty() {
		System.setProperty(TEST_PROPERTY, TEST_VALUE);
		EnvConfig.clear(TEST_PROPERTY);
		Assertions.assertNull(System.getProperty(TEST_PROPERTY));
	}

	private void enabledKeepass() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "true");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT);
	}

	@Test
	void testCanGetEntryFromKeepassDefaultGroup() {
		enabledKeepass();
		// when my.keepass.property does not exist in default env.
		// and only exists default group of keepass
		Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("my.keepass.property"));
		Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("MY_KEEPASS_PROPERTY"));
	}

	@Test
	void testCanGetEntryFromKeepassWhenFileNameSpecified() {
		System.setProperty("config.keepass.filename", "env-config.kdbx");
		enabledKeepass();
		Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("my.keepass.property"));
	}

	@Test
	void testCanGetFromKeepassWhenFileNameWithSpace() {
		System.setProperty("config.keepass.filename", "env config.kdbx");
		enabledKeepass();
		Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("my.keepass.property"));
	}

	@Test
	void testExceptionWhenKeepassFileMissing() {
		System.setProperty("config.keepass.filename", "non-existing");
		final ConfigException exception = Assertions.assertThrows(ConfigException.class,
				this::testCanGetEntryFromKeepassDefaultGroup);
		Assertions.assertEquals("Database non-existing.kdbx does not exist!", exception.getMessage());
	}

	@Test
	void testCanGetEntryFromKeepassDBWhenMultipleEnvironment() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "true");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		final String testEnv = "keepass";
		System.setProperty(CONFIG_ENV_KEY, "test," + testEnv);
		// when my.keepass.property exists in test env
		// and only exists in keepass env group of keepass
		// then keepass takes priority
		Assertions.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get("my.keepass.property"));
		Assertions.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get("MY_KEEPASS_PROPERTY"));
	}

	@Test
	void testCanGetPropertyFromKeepassWhenMultipleEnv() {
		environmentVariables.set(CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
		environmentVariables.set(CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
		environmentVariables.set("MY_KEEPASS_PROPERTY", "KEEPASS_ENV_VALUE");
//		environmentVariables.set("PROPERTY_ONE", "KEEPASS_ENV");
		final String testEnv = "test-no-keepass";
		System.setProperty(CONFIG_ENV_KEY, "keepass," + testEnv);
		Assertions.assertEquals(testEnv, EnvConfig.getEnvironment());
		// When a property.one exists in all environments, including default
		// Then keepass takes priority
		Assertions.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get("my.keepass.property"));
	}

	@Test
	void testCanGetEntryFromKeepassDB() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "true");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		// when my.keepass.property exists in test env
		// and only exists in default group of keepass
		// then keepass takes priority
		Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("my.keepass.property"));
		Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("MY_KEEPASS_PROPERTY"));
	}

	@Test
	void testCanGetEntryFromKeepassDisabled() {
		System.setProperty(CONFIG_KEEPASS_ENABLED_KEY, "false");
		System.setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		// when my.keepass.property exists in test env
		// and only exists in default group of keepass
		// then keepass takes priority
		Assertions.assertEquals("my_value", EnvConfig.get("my.keepass.property"));
	}

	@Test
	void testCanGetKeepassOnlyEntry() {
		environmentVariables.set(CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
		environmentVariables.set(CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		// when another.property does not exist in test env
		// and exists in test group of keepass
		Assertions.assertEquals("ANOTHER_PROPERTY", EnvConfig.get("another.property"));
	}

	@Test
	void testCanGetKeepassOnlyEntryWhenEntryWithTrailingSpace() {
		environmentVariables.set(CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
		environmentVariables.set(CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
		System.setProperty(CONFIG_ENV_KEY, TEST_ENVIRONMENT);
		// when another.property does not exist in test env
		// and exists in test group of keepass
		Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("trailing.space.property"));
	}

}
