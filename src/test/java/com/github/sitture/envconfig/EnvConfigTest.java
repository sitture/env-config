package com.github.sitture.envconfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Arrays;
import java.util.List;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigTest {

    private static final String CONFIG_KEEPASS_PASSWORD = "envconfig";
    private static final String TEST_ENVIRONMENT = "test";
    private static final String TEST_PROPERTY = "property";
    private static final String TEST_VALUE = "value";
    private static final String KEEPASS_VALUE = "KEEPASS_VALUE";
    public static final String MY_KEEPASS_PROPERTY = "my.keepass.property";

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeEach
    void setUp() {
        System.clearProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY);
        System.clearProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY);
        System.clearProperty(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY);
        System.clearProperty(MY_KEEPASS_PROPERTY);
        EnvConfig.reset();
    }

    @Test
    void testStaticAndSingleton() {
        Assertions.assertEquals(EnvConfig.getConfig().hashCode(), EnvConfig.getConfig().hashCode());
    }

    @Test
    void testCanGetDefaultEnvironment() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, EnvConfigUtils.CONFIG_ENV_DEFAULT);
        Assertions.assertEquals(EnvConfigUtils.CONFIG_ENV_DEFAULT, EnvConfig.getEnvironment());
        // when env not set
        System.clearProperty(EnvConfigUtils.CONFIG_ENV_KEY);
        Assertions.assertEquals(EnvConfigUtils.CONFIG_ENV_DEFAULT, EnvConfig.getEnvironment());
    }

    @Test
    void testCanGetEnvironment() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        Assertions.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
    }

    @Test
    void testCanGetEnvironmentWhenMultiple() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "default , test");
        Assertions.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
    }

    @Test
    void testExceptionWhenEnvMissing() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "default, non-existing ");
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                EnvConfig::getEnvironment);
        Assertions.assertTrue(exception.getMessage()
                .endsWith("/env-config/config/non-existing' does not exist or not a valid config directory!"));
    }

    @Test
    void testCanGetEnvironmentWithASpace() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT + " ");
        Assertions.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
    }

    @Test
    void testThrowsExceptionWhenNoPropertiesInEnv() {
        // given env is default and empty-profile exists in env properties
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "empty-env");
        // then an exception is thrown
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                () -> EnvConfig.getOrThrow("non.existing"));
        Assertions.assertTrue(exception.getMessage().startsWith("No property files found under"), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().endsWith("/env-config/config/empty-env'"), exception.getMessage());
    }

    @Test
    void testCanGetProperty() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        Assertions.assertEquals("my_value", EnvConfig.get("my.property"));
    }

    @Test
    void testCanGetPropertyUsingBothFormats() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        // when PROPERTY_SEVEN exists in test environment
        // then should be able to get key using both properties and env var formats

        Assertions.assertEquals("test", EnvConfig.get("PROPERTY_SEVEN"));
        Assertions.assertEquals("test", EnvConfig.get("property.seven"));
    }

    @Test
    void testCanGetPropertyFromGetOrThrow() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        Assertions.assertEquals("my_value", EnvConfig.getOrThrow("my.property"));
    }

    @Test
    void testDoesNotGetsPropertyFromSubDirs() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, EnvConfigUtils.CONFIG_ENV_DEFAULT);
        Assertions.assertNull(EnvConfig.get("property.sub.dir"));
    }

    @Test
    void testCanGetPropertyWhenMultipleEnv() {
        final String testEnv = "test-env";
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test," + testEnv);
        Assertions.assertEquals(testEnv, EnvConfig.getEnvironment());
        // When a property.one exists in all environments, including default
        Assertions.assertEquals(testEnv, EnvConfig.get("property.one"));
        // When a property.two exists in current environment only
        Assertions.assertEquals(testEnv, EnvConfig.get("property.two"));
        // When a property.three does not exist in current environment
        Assertions.assertEquals("test", EnvConfig.get("property.three"));
        // When a property.four exists in default environment only
        Assertions.assertEquals(EnvConfigUtils.CONFIG_ENV_DEFAULT, EnvConfig.get("property.four"));
    }

    @Test
    void testCanGetEntryWhenEnvVarAndDefaultValueSameWithParentEnv() {
        final String key = "PROPERTY_FIVE";
        environmentVariables.set(key, "default");
        final String testEnv = "test-env";
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test," + testEnv);
        // when property.five is set as env variable
        // and exists in default env with same value as env var
        // and exists in test env with different value
        // and does not exist in test-env
        // then value in test env takes priority
        Assertions.assertEquals("test", EnvConfig.get(EnvConfigUtils.getProcessedPropertyKey(key)));
        Assertions.assertEquals("test", EnvConfig.get(key));
    }

    @Test
    void testCanGetEntryWhenEnvVarAndDefaultValueSameWithSingleEnv() {
        final var key = "PROPERTY_SEVEN";
        // when PROPERTY_SEVEN=default is set as env variable
        environmentVariables.set(key, "default");
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        // and exists in default env with same value as env var
        // and exists in test env with PROPERTY_SEVEN=test
        // then value test env takes priority
        Assertions.assertEquals("test", EnvConfig.get(key));
        Assertions.assertEquals(EnvConfig.get(key), EnvConfig.get(EnvConfigUtils.getProcessedPropertyKey(key)));
    }

    @Test
    void testCanGetEntryWhenEnvVarAndDefaultValueDifferent() {
        environmentVariables.set("property.five", "env.default");
        final String testEnv = "test-env";
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test," + testEnv);
        // when property.five is set as env variable
        // and does not exist in test-env
        // and exists in test env
        // and exists in default env with different value to env var
        // then value in env var takes priority
        Assertions.assertEquals("env.default", EnvConfig.get("property.five"));
    }

    @Test
    void testCanGetEntryWhenEnvVarSet() {
        environmentVariables.set("property.six", "env.property.six");
        final String testEnv = "test-env";
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test," + testEnv);
        // when property.six is set as env variable
        // and exists in test-env env
        // and exists in test env
        // and exists in default env with different value to env var
        // then value in env var takes priority
        Assertions.assertEquals("env.property.six", EnvConfig.get("property.six"));
    }

    @Test
    void testCanGetEntryWhenEnvVarAndPropertySet() {
        final String key = "property.precedence";
        final String value = "SYS_PROPERTY_VALUE";
        environmentVariables.set(key, "SYS_ENV_VALUE");
        System.setProperty(key, value);
        // when property.precedence is set as env variable
        // and property.precedence is set system property
        // then value from system property takes priority
        Assertions.assertEquals(value, EnvConfig.get(key));
    }

    @Test
    void testCanGetEntryWhenEnvVarSetInMultiEnvs() {
        final String testEnv = "test-env";
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test," + testEnv);
        // when property.seven is not set in test-env
        // and exists in test env in environment variable format. i.e. PROPERTY_SEVEN=test
        // and exists in default env with different value i.e. PROPERTY_SEVEN=default
        // then i should be able to value from test env using the properties format
        Assertions.assertEquals("test", EnvConfig.get("property.seven"));
    }

    @Test
    void testCanGetEntryWhenEnvVarAndDefaultValueSameWithMultipleEnv() {
        final String key = "property.one";
        // when property.env is set in default env
        // and PROPERTY_ENV is set as env var to same value as default
        environmentVariables.set("PROPERTY_ONE", "default");
        // and property.one is also set in test-env and test env properties
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test,test-env");
        // then value in env test-env takes priority
        Assertions.assertEquals("test-env", EnvConfig.get(key));
    }

    @Test
    void testCanGetPropertyFromEnvVars() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        environmentVariables.set("MY_ENV_PROPERTY", "my_env_value");
        Assertions.assertEquals("my_env_value", EnvConfig.get("my.env.property"));
        Assertions.assertEquals("my_env_value", EnvConfig.get("MY_ENV_PROPERTY"));

        Assertions.assertEquals("my_value", EnvConfig.get("my.property"));
    }

    @Test
    void testCanGetParsedInt() {
        System.setProperty(TEST_PROPERTY, "123");
        Assertions.assertEquals(123, EnvConfig.getInt(TEST_PROPERTY));
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
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
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
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        Assertions.assertEquals("test", EnvConfig.get("non.existing", "test"));
    }

    @Test
    void testCanGetEnvVarUsingPropertyKey() {
        Assertions.assertEquals(System.getenv("PATH"), EnvConfig.get("path"));
    }

    @Test
    void testNullForNonExistingProperty() {
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        Assertions.assertNull(EnvConfig.get("non.existing"));
    }

    @Test
    void testEnvConfigExceptionThrown() {
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                () -> EnvConfig.getOrThrow("non.existing"));
        Assertions.assertEquals("Missing required variable 'non.existing'", exception.getMessage());
    }

    @Test
    void testCanAddANewProperty() {
        EnvConfig.add("1", 1);
        Assertions.assertEquals(1, EnvConfig.getInt("1"));
        Assertions.assertEquals("1", EnvConfig.get("1"));
        Assertions.assertFalse(EnvConfig.getBool("1"));
        EnvConfig.add("1", "1");
        Assertions.assertEquals(1, EnvConfig.getInt("1"));
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

    @Test
    void testCanGetEntryFromKeepassWhenFileNameSpecified() {
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY, "env-config.kdbx");
        enabledKeepass();
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(MY_KEEPASS_PROPERTY));
    }

    private void enabledKeepass() {
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY, "true");
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, EnvConfigUtils.CONFIG_ENV_DEFAULT);
    }

    @Test
    void testCanGetFromKeepassWhenFileNameWithSpace() {
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY, "env config.kdbx");
        enabledKeepass();
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(MY_KEEPASS_PROPERTY));
    }

    @Test
    void testExceptionWhenKeepassFileMissing() {
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY, "non-existing");
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                this::testCanGetEntryFromKeepassDefaultGroup);
        Assertions.assertEquals("Database non-existing.kdbx does not exist!", exception.getMessage());
    }

    @Test
    void testExceptionWhenKeepassMasterKeyMissing() {
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY, "true");
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                () -> EnvConfig.get(MY_KEEPASS_PROPERTY));
        Assertions.assertEquals(String.format("Missing required variable '%s'", EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY),
                exception.getMessage());
    }

    @Test
    void testCanGetEntryFromKeepassDefaultGroup() {
        enabledKeepass();
        // when my.keepass.property does not exist in default env.
        // and only exists default group of keepass
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(MY_KEEPASS_PROPERTY));
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("MY_KEEPASS_PROPERTY"));
    }

    @Test
    void testCanGetEntryFromKeepassDBWhenMultipleEnvironment() {
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY, "true");
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
        final String testEnv = "keepass";
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test," + testEnv);
        // when my.keepass.property exists in test env
        // and only exists in keepass env group of keepass
        // then keepass takes priority
        Assertions.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get(MY_KEEPASS_PROPERTY));
        Assertions.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get("MY_KEEPASS_PROPERTY"));
    }

    @Test
    void testCanGetPropertyWhenInSystemEnvAndKeepass() {
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
        final String sysEnvValue = "SYS_ENV_VALUE";
        environmentVariables.set("MY_KEEPASS_PROPERTY", sysEnvValue);
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "keepass");
        // When my.keepass.property is set as environment variable
        // And my.keepass.property exists in Keepass 'keepass' group
        // And my.keepass.property exists in 'keepass' environment files
        // Then the value from system environment takes priority
        Assertions.assertEquals(sysEnvValue, EnvConfig.get(MY_KEEPASS_PROPERTY));
    }

    @Test
    void testCanGetPropertyWhenInSystemPropertyAndSystemEnvAndKeepass() {
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
        environmentVariables.set("MY_KEEPASS_PROPERTY", "SYS_ENV_VALUE");
        final String sysPropertyValue = "SYS_PROPERTY_VALUE";
        System.setProperty(MY_KEEPASS_PROPERTY, sysPropertyValue);
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "keepass");
        // When my.keepass.property is set as environment variable
        // And my.keepass.property is set as system property
        // And my.keepass.property exists in Keepass 'keepass' group
        // And my.keepass.property exists in 'keepass' environment files
        // Then the value from system property takes priority
        Assertions.assertEquals(sysPropertyValue, EnvConfig.get(MY_KEEPASS_PROPERTY));
    }

    @Test
    void testCanGetPropertyFromKeepassWhenMultipleEnv() {
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
        final String testEnv = "test-no-keepass";
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "keepass," + testEnv);
        Assertions.assertEquals(testEnv, EnvConfig.getEnvironment());
        // When my.keepass.property exists in Keepass 'keepass' group
        // And my.keepass.property exists in 'keepass' environment files
        // Then the value from keepass group takes priority
        Assertions.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get(MY_KEEPASS_PROPERTY));
    }

    @Test
    void testCanGetEntryFromKeepassDB() {
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY, "true");
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        // when my.keepass.property exists in test env
        // and only exists in default group of keepass
        // then keepass takes priority
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(MY_KEEPASS_PROPERTY));
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("MY_KEEPASS_PROPERTY"));
    }

    @Test
    void testCanGetEntryFromKeepassDisabled() {
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY, "false");
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_PASSWORD);
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        // when my.keepass.property exists in test env
        // and only exists in default group of keepass
        // then keepass takes priority
        Assertions.assertEquals("my_value", EnvConfig.get(MY_KEEPASS_PROPERTY));
    }

    @Test
    void testCanGetKeepassOnlyEntry() {
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        // when another.property does not exist in test env
        // and exists in test group of keepass
        Assertions.assertEquals("ANOTHER_PROPERTY", EnvConfig.get("another.property"));
    }

    @Test
    void testCanGetKeepassOnlyEntryWhenEntryWithTrailingSpace() {
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY.replace(".", "_").toUpperCase(), "true");
        environmentVariables.set(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY.replace(".", "_").toUpperCase(), CONFIG_KEEPASS_PASSWORD);
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, TEST_ENVIRONMENT);
        // when another.property does not exist in test env
        // and exists in test group of keepass
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("trailing.space.property"));
    }

}
