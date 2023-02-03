package com.github.sitture.envconfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigPrecedenceTest {

    public static final String PROPERTY_KEEPASS = "property.keepass";
    public static final String SYS_ENV_VALUE = "sys.env.value";
    public static final String SYS_PROPERTY_VALUE = "sys.property.value";
    private static final String CONFIG_KEEPASS_MASTERKEY = "envconfig";

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @SystemStub
    private final SystemProperties systemProperties = new SystemProperties();

    @BeforeEach
    void setUp() {
        EnvConfig.reset();
    }

    @Test
    void testSystemPropertiesTakesPriorityOverEnvironmentVariables() {
        final String key = "property.one";
        // when property is set as system property
        systemProperties.set(key, SYS_PROPERTY_VALUE)
                .set(EnvConfigUtils.getProcessedEnvKey(key), SYS_PROPERTY_VALUE);
        // and property is set as environment variable
        environmentVariables.set(EnvConfigUtils.getProcessedEnvKey(key), SYS_ENV_VALUE)
                .set(key, SYS_ENV_VALUE);
        // and exists in default env with different value
        // then value from system property takes priority
        Assertions.assertEquals(SYS_PROPERTY_VALUE, EnvConfig.get(key));
        Assertions.assertEquals(SYS_PROPERTY_VALUE, EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testSystemVariablesTakesPriorityOverKeepass() {
        final String key = PROPERTY_KEEPASS;
        // when keepass loading is enabled
        // and property exists in keepass
        setKeepassEnabled();
        // and property is set as system property
        systemProperties.set(PROPERTY_KEEPASS, SYS_PROPERTY_VALUE);
        // and property is set as environment variable
        environmentVariables.set(EnvConfigUtils.getProcessedEnvKey(key), SYS_ENV_VALUE);
        // and property is set in environment file
        setEnvironment("test");
        // then value from system property and environment variable takes priority
        Assertions.assertEquals(SYS_PROPERTY_VALUE, EnvConfig.get(key));
        Assertions.assertEquals(SYS_ENV_VALUE, EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testKeepassTakesPriorityOverFiles() {
        final String key = "property.eight";
        // given property only exists in default config files
        // and property is set as environment variable with same value as config files
        environmentVariables.set(key, "default");
        // when keepass loading is enabled
        setKeepassEnabled();
        // and property only exists in default group of keepass
        // then property from keepass group takes priority
        Assertions.assertEquals("KEEPASS_VALUE", EnvConfig.get(key));
        Assertions.assertEquals("KEEPASS_VALUE", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testKeepassTakesPriorityOverFilesWhenOnlyInDefaultFilesAndKeepass() {
        final String key = "property.eight";
        // given property only exists in default config files
        // and property is set as environment variable with same value as config files
        environmentVariables.set(key, "default");
        // and property does not exist in current environment config files
        setEnvironment("test");
        // when keepass loading is enabled
        setKeepassEnabled();
        // and property only exists in default group of keepass
        // then property from keepass group takes priority
        Assertions.assertEquals("KEEPASS_VALUE", EnvConfig.get(key));
        Assertions.assertEquals("KEEPASS_VALUE", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testKeepassTakesPriorityOverFilesWhenNotInDefaultGroup() {
        final String key = PROPERTY_KEEPASS;
        // given property does not exist in config files
        // when keepass loading is enabled
        setKeepassEnabled();
        // and property only exists in default group of keepass
        // then property from keepass is returned
        Assertions.assertEquals("KEEPASS_VALUE", EnvConfig.get(key));
        Assertions.assertEquals("KEEPASS_VALUE", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
        EnvConfig.reset();
        setEnvironment("test, keepass");
        // when property exists in environment group of keepass
        // and property exists in test environment config files
        // and property does not exist in keepass environment config files
        // then value from environment keepass group takes priority
        Assertions.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get(key));
        Assertions.assertEquals("KEEPASS_ENVIRONMENT", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testKeepassTakesPriorityOverFilesWhenNotInDefaultFilesWithEnvVarAndEnvFileValueSame() {
        final var key = PROPERTY_KEEPASS;
        // when property does not exist in default config files
        // and property exists in current environment config files
        setEnvironment("test");
        // and property is also set as env var with same value as environment config file
        environmentVariables.set(key, "test");
        // when keepass loading is enabled
        setKeepassEnabled();
        // and property exists in keepass with different value
        // then value from keepass takes priority
        Assertions.assertEquals("KEEPASS_VALUE", EnvConfig.get(key));
        Assertions.assertEquals(EnvConfig.get(key), EnvConfig.get(EnvConfigUtils.getProcessedPropertyKey(key)));
    }

    @Test
    void testEnvironmentVariableTakesPriorityOverDefaultEnvironment() {
        final String key = "PROPERTY_ONE";
        // when property is set as environment variable
        environmentVariables.set(key, SYS_ENV_VALUE);
        // and exists in default env with different value
        // then value from environment variable takes priority
        Assertions.assertEquals(SYS_ENV_VALUE, EnvConfig.get(key));
        Assertions.assertEquals(SYS_ENV_VALUE, EnvConfig.get(EnvConfigUtils.getProcessedPropertyKey(key)));
    }

    @Test
    void testEnvironmentVariableTakesPriorityOverEnvironment() {
        final String key = "PROPERTY_ONE";
        // when property is set as environment variable
        environmentVariables.set(key, SYS_ENV_VALUE);
        // and property exists in environment config files
        setEnvironment("test");
        // and exists in default env with different value
        // then value from environment variable takes priority
        Assertions.assertEquals(SYS_ENV_VALUE, EnvConfig.get(key));
        Assertions.assertEquals(SYS_ENV_VALUE, EnvConfig.get(EnvConfigUtils.getProcessedPropertyKey(key)));
    }

    @Test
    void testEnvironmentVariableTakesPriorityOverEnvironmentWhenEnvVarAndDefaultValueSame() {
        final String key = "PROPERTY_ONE";
        // when property is set as environment variable
        environmentVariables.set(key, "default");
        // and exists in default env with same value as env var
        // and property exists in environment config files
        setEnvironment("test");
        // then value from environment variable takes priority
        Assertions.assertEquals("default", EnvConfig.get(key));
        Assertions.assertEquals(EnvConfig.get(key), EnvConfig.get(EnvConfigUtils.getProcessedPropertyKey(key)));
    }

    @Test
    void testCurrentEnvironmentTakesPriorityWhenEnvVarAndDefaultValueSame() {
        final String key = "property.one";
        // when property is set as environment variable
        environmentVariables.set(key, "default");
        // and exists in default config files with same value as env var
        // and exists in parent environment config files with different value
        // and exists in current environment config files with different value
        setEnvironment("test,test-env");
        // then value from current environment config files takes priority
        Assertions.assertEquals("test-env", EnvConfig.get(key));
        Assertions.assertEquals(EnvConfig.get(key), EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testParentEnvironmentTakesPriorityWhenEnvVarAndDefaultValueSame() {
        final String key = "PROPERTY_FIVE";
        // when property is set as environment variable
        environmentVariables.set(key, "default");
        // and exists in default config files with same value as env var
        // and exists in parent environment config files with different value
        // and does not exist in current environment config files with different value
        setEnvironment("test,test-env");
        // then value from parent environment config files takes priority
        Assertions.assertEquals("test", EnvConfig.get(key));
        Assertions.assertEquals(EnvConfig.get(key), EnvConfig.get(EnvConfigUtils.getProcessedPropertyKey(key)));
    }

    private void setKeepassEnabled() {
        systemProperties.set(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY, true);
        systemProperties.set(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY, CONFIG_KEEPASS_MASTERKEY);
    }

    private void setEnvironment(final String environment) {
        systemProperties.set(EnvConfigUtils.CONFIG_ENV_KEY, environment);
    }

}
