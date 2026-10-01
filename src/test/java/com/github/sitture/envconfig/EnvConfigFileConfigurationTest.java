package com.github.sitture.envconfig;

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
class EnvConfigFileConfigurationTest {

    @TempDir
    Path tempDir;

    @SystemStub
    private final SystemProperties systemProperties = new SystemProperties();

    @Test
    void testGetConfigFilesThrowsExceptionWhenDirectoryDoesNotExist() {
        final Path missingPath = this.tempDir.resolve("missing-dir");
        final EnvConfigFileConfiguration configuration = new EnvConfigFileConfiguration(new EnvConfigProperties());

        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> configuration.getConfigFiles(missingPath));

        Assertions.assertEquals("'" + missingPath + "' does not exist or not a valid config directory!", exception.getMessage());
    }

    @Test
    void testGetConfigFilesThrowsExceptionWhenPathIsAFile() throws IOException {
        final Path filePath = Files.writeString(this.tempDir.resolve("config.properties"), "property.one=value");
        final EnvConfigFileConfiguration configuration = new EnvConfigFileConfiguration(new EnvConfigProperties());

        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> configuration.getConfigFiles(filePath));

        Assertions.assertEquals("'" + filePath + "' does not exist or not a valid config directory!", exception.getMessage());
    }

    @Test
    void testGetConfigPropertiesReturnsOnlyPropertiesFiles() throws IOException {
        Files.writeString(this.tempDir.resolve("included.properties"), "property.one=value");
        Files.writeString(this.tempDir.resolve("ignored.txt"), "property.one=ignored");
        Files.createDirectories(this.tempDir.resolve("nested"));
        Files.writeString(this.tempDir.resolve("nested").resolve("nested.properties"), "property.one=nested");

        final List<java.io.File> files = new EnvConfigFileConfiguration(new EnvConfigProperties())
            .getConfigProperties(this.tempDir.toFile());

        Assertions.assertEquals(1, files.size());
        Assertions.assertEquals("included.properties", files.get(0).getName());
    }

    @Test
    void testGetConfigPropertiesThrowsExceptionWhenNoPropertiesFilesArePresent() {
        final Path emptyConfigPath = getTestConfigPath("empty-env");
        final java.io.File emptyConfigDir = emptyConfigPath.toFile();
        final ThrowingGetConfigProperties throwingGetConfigProperties = new ThrowingGetConfigProperties(emptyConfigDir);

        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            throwingGetConfigProperties::invoke);

        Assertions.assertTrue(exception.getMessage().startsWith("No property files found under"), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().endsWith("/env-config/config/empty-env'"), exception.getMessage());
    }

    @Test
    void testFileConfigurationLoadsNormalizedKeysFromPropertiesFiles() {
        final Path testConfigPath = getTestConfigPath("test");
        final Configuration configuration = new EnvConfigFileConfiguration(new EnvConfigProperties()).getConfiguration(testConfigPath);

        Assertions.assertEquals("test", configuration.getString("property.one"));
        Assertions.assertEquals("test", configuration.getString("PROPERTY_ONE"));
        Assertions.assertEquals("test", configuration.getString("property.seven"));
        Assertions.assertEquals("test", configuration.getString("PROPERTY_SEVEN"));
    }

    @Test
    void testFileConfigurationLoadsPropertiesFromAllFilesInDirectory() throws IOException {
        Files.writeString(this.tempDir.resolve("one.properties"), "property.one=value.one\n");
        Files.writeString(this.tempDir.resolve("two.properties"), "property.two=value.two\n");

        final Configuration configuration = new EnvConfigFileConfiguration(new EnvConfigProperties()).getConfiguration(this.tempDir);

        Assertions.assertEquals("value.one", configuration.getString("property.one"));
        Assertions.assertEquals("value.two", configuration.getString("property.two"));
    }

    @Test
    void testFileConfigurationWrapsInvalidPropertiesParsingErrors() throws IOException {
        Files.writeString(this.tempDir.resolve("broken.properties"), "property.one=\\u00ZZ\n");
        final EnvConfigFileConfiguration configuration = new EnvConfigFileConfiguration(new EnvConfigProperties());

        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> configuration.getConfiguration(this.tempDir));

        Assertions.assertNotNull(exception.getCause());
    }

    @Test
    void testFileConfigurationBuildsConfigurationForConfiguredEnvironments() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test,test-env");
        final Map<String, Configuration> configurationMap = new EnvConfigFileConfiguration(new EnvConfigProperties()).getConfiguration();

        Assertions.assertEquals(3, configurationMap.size());
        Assertions.assertEquals("default", configurationMap.get("default").getString("property.one"));
        Assertions.assertEquals("test", configurationMap.get("test").getString("property.one"));
        Assertions.assertEquals("test-env", configurationMap.get("test-env").getString("property.one"));
    }

    private Path getTestConfigPath(final String... paths) {
        Path configPath = Path.of(new EnvConfigProperties().getBuildDir(), "config");
        for (final String path : paths) {
            configPath = configPath.resolve(path);
        }
        return configPath;
    }

    private static final class ThrowingGetConfigProperties {

        private final java.io.File configDir;

        private ThrowingGetConfigProperties(final java.io.File configDir) {
            this.configDir = configDir;
        }

        private void invoke() {
            new EnvConfigFileConfiguration(new EnvConfigProperties()).getConfigProperties(this.configDir);
        }
    }
}


