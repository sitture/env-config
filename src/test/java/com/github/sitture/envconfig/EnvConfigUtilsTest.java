package com.github.sitture.envconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigUtilsTest {

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @SystemStub
    private final SystemProperties systemProperties = new SystemProperties();

    @Test
    void testCanGetProcessedPropertyKey() {
        assertEquals("foo.key", EnvConfigUtils.getProcessedPropertyKey("FOO_KEY"));
    }

    @Test
    void testCanGetProcessedEnvVarKey() {
        assertEquals("FOO_KEY", EnvConfigUtils.getProcessedEnvKey("Foo.Key"));
    }

    @Test
    void testCanGetListOfValues() {
        assertEquals(List.of("foo", "bar", ""), EnvConfigUtils.getListOfValues("foo , bar, ", EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
        assertEquals(List.of("one"), EnvConfigUtils.getListOfValues("one", EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
        assertEquals(List.of(), EnvConfigUtils.getListOfValues(null, EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
    }

    @Test
    void testCanGetConfigProperty() {
        // When no system property or environment variable is set
        assertEquals("default", EnvConfigUtils.getConfigProperty(EnvConfigKey.CONFIG_ENV, "default"));
        // When system property is set
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "property");
        assertEquals("property", EnvConfigUtils.getConfigProperty(EnvConfigKey.CONFIG_ENV, "default"));
        // When environment var is also set
        environmentVariables.set(EnvConfigKey.CONFIG_ENV.getEnvProperty(), "env");
        // Then system property takes precedence
        assertEquals("property", EnvConfigUtils.getConfigProperty(EnvConfigKey.CONFIG_ENV, "default"));
        // when only environment variable is set
        systemProperties.remove(EnvConfigKey.CONFIG_ENV.getProperty());
        assertEquals("env", EnvConfigUtils.getConfigProperty(EnvConfigKey.CONFIG_ENV, "default"));
    }

    @Test
    void testExceptionWhenRequiredConfigMissing() {
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> EnvConfigUtils.getRequiredConfigProperty(EnvConfigKey.CONFIG_KEEPASS_ENABLED));
        assertEquals(String.format("Missing required variable '%s'", EnvConfigKey.CONFIG_KEEPASS_ENABLED.getProperty()), exception.getMessage());
    }

}
