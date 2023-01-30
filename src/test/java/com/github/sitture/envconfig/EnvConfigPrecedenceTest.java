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
public class EnvConfigPrecedenceTest {

    public static final String MY_KEEPASS_PROPERTY = "my.keepass.property";
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
    void testSystemVariablesTakesPriorityOverKeepass() {
        final String key = MY_KEEPASS_PROPERTY;
        // when keepass loading is enabled
        // and property exists in keepass
        setKeepassEnabled();
        // and property is set as system property
        systemProperties.set(MY_KEEPASS_PROPERTY, SYS_PROPERTY_VALUE);
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
        final String key = MY_KEEPASS_PROPERTY;
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
    void testCanGetEntryWhenEnvVarAndDefaultValueSameWithSingleEnv() {
        final var key = MY_KEEPASS_PROPERTY;
        // when PROPERTY_SEVEN=test is set as env variable
        environmentVariables.set(key, "test");
        setEnvironment("test");
        // and exists in default env with same value as env var
        // and exists in test env with PROPERTY_SEVEN=test
        // then value from test env takes priority
        Assertions.assertEquals("KEEPASS_VALUE", EnvConfig.get(key));
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
