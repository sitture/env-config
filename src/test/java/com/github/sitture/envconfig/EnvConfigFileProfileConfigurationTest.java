package com.github.sitture.envconfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigFileProfileConfigurationTest {

    @TempDir
    Path tempDir;

    @SystemStub
    private final SystemProperties systemProperties = new SystemProperties();

    @Test
    void testProfileConfigurationReturnsEmptyFileListWhenEnvironmentProfileDirectoryDoesNotExist() {
        final Path missingProfilePath = this.tempDir.resolve("missing-profile");

        final List<File> files = new EnvConfigFileProfileConfiguration(new EnvConfigProperties()).getConfigFiles(missingProfilePath);

        Assertions.assertTrue(files.isEmpty());
    }

    @Test
    void testProfileConfigurationThrowsExceptionWhenProfilePathIsAFile() throws IOException {
        final Path profileFile = Files.writeString(this.tempDir.resolve("profile.properties"), "property.one=value");
        final EnvConfigFileProfileConfiguration configuration = new EnvConfigFileProfileConfiguration(new EnvConfigProperties());

        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> configuration.getConfigFiles(profileFile));

        Assertions.assertEquals("'" + profileFile + "' does not exist or not a valid config directory!", exception.getMessage());
    }

    @Test
    void testProfileFileConfigurationReturnsEmptyWhenEnvironmentProfileDirectoryDoesNotExist() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test");
        systemProperties.set(EnvConfigKey.CONFIG_PROFILE.getProperty(), "missing-profile");
        final Map<String, Configuration> configurationMap = new EnvConfigFileProfileConfiguration(new EnvConfigProperties()).getConfiguration();

        Assertions.assertFalse(configurationMap.get("test").getKeys().hasNext());
        Assertions.assertNull(configurationMap.get("test").getString("missing-profile.one"));
    }

    @Test
    void testProfileFileConfigurationBuildsConfigurationForConfiguredEnvironments() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test,test-env");
        systemProperties.set(EnvConfigKey.CONFIG_PROFILE.getProperty(), "prof2");

        final Map<String, Configuration> configurationMap = new EnvConfigFileProfileConfiguration(new EnvConfigProperties()).getConfiguration();

        Assertions.assertEquals(3, configurationMap.size());
        Assertions.assertEquals("prof2.value", configurationMap.get("default").getString("prof2.one"));
        Assertions.assertEquals("test.prof2.value", configurationMap.get("test").getString("prof2.one"));
        Assertions.assertFalse(configurationMap.get("test-env").getKeys().hasNext());
    }

    @Test
    void testProfileFileConfigurationThrowsWhenProfileDirectoryContainsNoPropertyFiles() {
        systemProperties.set(EnvConfigKey.CONFIG_PROFILE.getProperty(), "empty-profile");
        final EnvConfigFileProfileConfiguration configuration = new EnvConfigFileProfileConfiguration(new EnvConfigProperties());

        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> configuration.getConfigurationForEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT));

        Assertions.assertTrue(exception.getMessage().startsWith("No property files found under"), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().endsWith("/env-config/config/default/empty-profile'"), exception.getMessage());
    }
}