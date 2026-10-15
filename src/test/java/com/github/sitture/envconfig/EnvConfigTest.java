package com.github.sitture.envconfig;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigTest {

    private static final String CONFIG_KEEPASS_PASSWORD = "envconfig";
    private static final String TEST_ENVIRONMENT = "test";
    private static final String TEST_PROPERTY = "property";
    private static final String TEST_VALUE = "value";
    private static final String KEEPASS_VALUE = "KEEPASS_VALUE";
    public static final String PROPERTY_KEEPASS = "property.keepass";

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @SystemStub
    private final SystemProperties systemProperties = new SystemProperties();

    @BeforeEach
    void setUp() {
        EnvConfig.reset();
    }

    @Test
    void testStaticAndSingleton() {
        Assertions.assertEquals(EnvConfig.getConfig().hashCode(), EnvConfig.getConfig().hashCode());
    }

    @Test
    void testCanGetDefaultEnvironment() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), EnvConfigUtils.CONFIG_ENV_DEFAULT);
        Assertions.assertEquals(EnvConfigUtils.CONFIG_ENV_DEFAULT, EnvConfig.getEnvironment());
        // when env not set
        systemProperties.remove(EnvConfigKey.CONFIG_ENV.getProperty());
        Assertions.assertEquals(EnvConfigUtils.CONFIG_ENV_DEFAULT, EnvConfig.getEnvironment());
    }

    @Test
    void testCanGetEnvironment() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        Assertions.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
    }

    @Test
    void testCanGetEnvironmentWhenMultiple() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "default , test");
        Assertions.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
    }

    @Test
    void testExceptionWhenEnvMissing() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "default, non-existing ");
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            EnvConfig::getEnvironment);
        Assertions.assertTrue(exception.getMessage()
            .endsWith("/env-config/config/non-existing' does not exist or not a valid config directory!"));
    }

    @Test
    void testCanGetEnvironmentWithASpace() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT + " ");
        Assertions.assertEquals(TEST_ENVIRONMENT, EnvConfig.getEnvironment());
    }

    @Test
    void testThrowsExceptionWhenNoPropertiesInEnv() {
        // given env is default and empty-profile exists in env properties
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "empty-env");
        // then an exception is thrown
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> EnvConfig.getOrThrow("non.existing"));
        Assertions.assertTrue(exception.getMessage().startsWith("No property files found under"), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().endsWith("/env-config/config/empty-env'"), exception.getMessage());
    }

    @Test
    void testCanGetProperty() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        Assertions.assertEquals("test", EnvConfig.get("property.one"));
    }

    @Test
    void testCanGetPropertyUsingBothFormats() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        // when PROPERTY_SEVEN exists in test environment
        // then should be able to get key using both properties and env var formats

        Assertions.assertEquals("test", EnvConfig.get("PROPERTY_SEVEN"));
        Assertions.assertEquals("test", EnvConfig.get("property.seven"));
    }

    @Test
    void testAsMapUsesCanonicalKeysOnly() {
        System.setProperty(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);

        final java.util.Map<String, Object> configEntries = EnvConfig.asMap();

        Assertions.assertEquals("test", configEntries.get("property.seven"));
        Assertions.assertFalse(configEntries.containsKey("PROPERTY_SEVEN"));
    }

    @Test
    void testCanGetPropertyFromGetOrThrow() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        Assertions.assertEquals("test", EnvConfig.getOrThrow("property.one"));
    }

    @Test
    void testDoesNotGetsPropertyFromSubDirs() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), EnvConfigUtils.CONFIG_ENV_DEFAULT);
        Assertions.assertNull(EnvConfig.get("property.sub.dir"));
    }

    @Test
    void testCanGetPropertyWhenMultipleEnv() {
        final String testEnv = "test-env";
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test," + testEnv);
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
    void testCanGetEntryWhenEnvVarAndDefaultValueDifferent() {
        environmentVariables.set("property.five", "env.default");
        final String testEnv = "test-env";
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test," + testEnv);
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
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test," + testEnv);
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
        systemProperties.set(key, value);
        // when property.precedence is set as env variable
        // and property.precedence is set system property
        // then value from system property takes priority
        Assertions.assertEquals(value, EnvConfig.get(key));
    }

    @Test
    void testCanGetEntryWhenEnvVarSetInMultiEnvs() {
        final String testEnv = "test-env";
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test," + testEnv);
        // when property.seven is not set in test-env
        // and exists in test env in environment variable format. i.e. PROPERTY_SEVEN=test
        // and exists in default env with different value i.e. PROPERTY_SEVEN=default
        // then i should be able to value from test env using the properties format
        Assertions.assertEquals("test", EnvConfig.get("property.seven"));
    }

    @Test
    void testCanGetPropertyFromEnvVars() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        environmentVariables.set("MY_ENV_PROPERTY", "my_env_value");
        Assertions.assertEquals("my_env_value", EnvConfig.get("my.env.property"));
        Assertions.assertEquals("my_env_value", EnvConfig.get("MY_ENV_PROPERTY"));

        Assertions.assertEquals("test", EnvConfig.get("property.one"));
    }

    @Test
    void testCanGetParsedInt() {
        systemProperties.set(TEST_PROPERTY, "123");
        Assertions.assertEquals(123, EnvConfig.getInt(TEST_PROPERTY));
    }

    @Test
    void testCanGetParsedBoolean() {
        systemProperties.set(TEST_PROPERTY, "true");
        Assertions.assertTrue(EnvConfig.getBool(TEST_PROPERTY));
        systemProperties.set(TEST_PROPERTY, "false");
        Assertions.assertFalse(EnvConfig.getBool(TEST_PROPERTY));
        systemProperties.set(TEST_PROPERTY, "falssse");
        Assertions.assertFalse(EnvConfig.getBool(TEST_PROPERTY));
    }

    @Test
    void testCanGetParsedList() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        Assertions.assertTrue(EnvConfig.getList(TEST_PROPERTY).isEmpty());
        systemProperties.set(TEST_PROPERTY, "env");
        Assertions.assertEquals(1, EnvConfig.getList(TEST_PROPERTY).size());
        systemProperties.set(TEST_PROPERTY, "env, config");
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
        systemProperties.set(property, "env;");
        Assertions.assertEquals(1, EnvConfig.getList(property, delimiter).size());
        systemProperties.set(property, "env;config ");
        final List<String> actualList = EnvConfig.getList(property, delimiter);
        Assertions.assertEquals(2, actualList.size());
        final List<String> expectedList = Arrays.asList("env", "config");
        Assertions.assertEquals(expectedList, actualList);
    }

    @Test
    void testCanGetDefaultForNonExistingProperty() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        Assertions.assertEquals("test", EnvConfig.get("non.existing", "test"));
    }

    @Test
    void testCanGetEnvVarUsingPropertyKey() {
        Assertions.assertEquals(System.getenv("PATH"), EnvConfig.get("path"));
    }

    @Test
    void testNullForNonExistingProperty() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        Assertions.assertNull(EnvConfig.get("non.existing"));
    }

    @Test
    void testEnvConfigExceptionThrown() {
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> EnvConfig.getOrThrow("non.existing"));
        Assertions.assertEquals("Missing required key 'non.existing'", exception.getMessage());
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
        systemProperties.set(TEST_PROPERTY, TEST_VALUE);
        Assertions.assertEquals(TEST_VALUE, EnvConfig.get(TEST_PROPERTY));
        // re-assign to an existing property
        EnvConfig.set(TEST_PROPERTY, "updatedValue");
        Assertions.assertEquals("updatedValue", EnvConfig.get(TEST_PROPERTY));
        // set a new property
        EnvConfig.set("property2", TEST_VALUE);
        Assertions.assertEquals(TEST_VALUE, EnvConfig.get("property2"));
    }

    @Test
    void testCanSetPropertyUsingEnvFormat() {
        EnvConfig.set("PROPERTY_SET_WITH_ENV_FORMAT", TEST_VALUE);

        Assertions.assertEquals(TEST_VALUE, EnvConfig.get("PROPERTY_SET_WITH_ENV_FORMAT"));
        Assertions.assertEquals(TEST_VALUE, EnvConfig.get("property.set.with.env.format"));
    }

    @Test
    void testCanClearProperty() {
        systemProperties.set(TEST_PROPERTY, TEST_VALUE);
        EnvConfig.clear(TEST_PROPERTY);
        Assertions.assertNull(System.getProperty(TEST_PROPERTY));
    }

    @Test
    void testCanGetEntryFromKeepassWhenFileNameSpecified() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "env-config.kdbx");
        setKeepassEnabled();
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(PROPERTY_KEEPASS));
    }

    @Test
    void testCanGetEntryFromKeepassWithKdbx3() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "kdbx3.kdbx");
        setKeepassEnabled();
        Assertions.assertEquals("KDBX3_VALUE", EnvConfig.get(PROPERTY_KEEPASS));
    }

    @Test
    void testCanGetEntryFromKeepassWithKdbx4() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "kdbx4.kdbx");
        setKeepassEnabled();
        Assertions.assertEquals("KDBX4_VALUE", EnvConfig.get(PROPERTY_KEEPASS));
    }

    private void setKeepassEnabled() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getProperty(), true);
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), CONFIG_KEEPASS_PASSWORD);
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), EnvConfigUtils.CONFIG_ENV_DEFAULT);
    }

    @Test
    void testCanGetFromKeepassWhenFileNameWithSpace() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "env config.kdbx");
        setKeepassEnabled();
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(PROPERTY_KEEPASS));
    }

    @Test
    void testExceptionWhenKeepassFileMissing() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "non-existing");
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            this::testCanGetEntryFromKeepassDefaultGroup);
        Assertions.assertEquals("Database non-existing.kdbx does not exist!", exception.getMessage());
    }

    @Test
    void testExceptionWhenKeepassMasterKeyMissing() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getProperty(), true);
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> EnvConfig.get(PROPERTY_KEEPASS));
        Assertions.assertEquals("Missing required variable '%s'".formatted("env.config.keepass.masterkey"),
            exception.getMessage());
    }

    @Test
    void testCanGetEntryFromKeepassDefaultGroup() {
        setKeepassEnabled();
        // when property.keepass does not exist in default env.
        // and only exists default group of keepass
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(PROPERTY_KEEPASS));
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(PROPERTY_KEEPASS)));
    }

    @Test
    void testCanGetPropertyFromKeepassWhenMultipleEnv() {
        environmentVariables.set(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getEnvProperty(), "true");
        environmentVariables.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getEnvProperty(), CONFIG_KEEPASS_PASSWORD);
        final String testEnv = "test-no-keepass";
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "keepass," + testEnv);
        Assertions.assertEquals(testEnv, EnvConfig.getEnvironment());
        // When property.keepass exists in Keepass 'keepass' group
        // And property.keepass exists in 'keepass' environment files
        // Then the value from keepass group takes priority
        Assertions.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get(PROPERTY_KEEPASS));
    }

    @Test
    void testCanGetEntryFromKeepassDB() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getProperty(), true);
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), CONFIG_KEEPASS_PASSWORD);
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        // when property.keepass exists in test env
        // and only exists in default group of keepass
        // then keepass takes priority
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(PROPERTY_KEEPASS));
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(PROPERTY_KEEPASS)));
    }

    @Test
    void testCanGetEntryFromKeepassDisabled() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getProperty(), false);
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), CONFIG_KEEPASS_PASSWORD);
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        // when property.keepass exists in test env
        // and only exists in default group of keepass
        // then keepass takes priority
        Assertions.assertEquals("test", EnvConfig.get(PROPERTY_KEEPASS));
    }

    @Test
    void testCanGetKeepassOnlyEntry() {
        environmentVariables.set(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getEnvProperty(), "true");
        environmentVariables.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getEnvProperty(), CONFIG_KEEPASS_PASSWORD);
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        // when another.property does not exist in test env
        // and exists in test group of keepass
        Assertions.assertEquals("ANOTHER_PROPERTY", EnvConfig.get("another.property"));
    }

    @Test
    void testCanGetKeepassOnlyEntryWhenEntryWithTrailingSpace() {
        environmentVariables.set(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getEnvProperty(), "true");
        environmentVariables.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getEnvProperty(), CONFIG_KEEPASS_PASSWORD);
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), TEST_ENVIRONMENT);
        // when another.property does not exist in test env
        // and exists in test group of keepass
        Assertions.assertEquals(KEEPASS_VALUE, EnvConfig.get("trailing.space.property"));
    }

}
