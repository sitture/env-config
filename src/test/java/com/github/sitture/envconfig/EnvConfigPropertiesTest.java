package com.github.sitture.envconfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigPropertiesTest {

    @SystemStub
    private final SystemProperties systemProperties = new SystemProperties();

    @Test
    void testCanGetBuildDir() {
        Assertions.assertEquals(System.getProperty("user.dir"), new EnvConfigProperties().getBuildDir(), "invalid buildDir!");
    }

    @Test
    void testCanGetConfigProfile() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        systemProperties.remove(EnvConfigKey.CONFIG_PROFILE.getProperty());
        Assertions.assertEquals("", configProperties.getConfigProfile(), "invalid config-profile!");
        systemProperties.set(EnvConfigKey.CONFIG_PROFILE.getProperty(), "test-profile");
        Assertions.assertEquals("test-profile", configProperties.getConfigProfile());
    }

    @Test
    void testCanGetEnvironmentsList() {
        // when config.environment isn't specified
        systemProperties.remove(EnvConfigKey.CONFIG_ENV.getProperty());
        Assertions.assertEquals(List.of("default"), new EnvConfigProperties().getEnvironments());
        Assertions.assertEquals("default", new EnvConfigProperties().getCurrentEnvironment());
        // when a single environment is specified
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test ");
        Assertions.assertEquals(List.of("test", "default"), new EnvConfigProperties().getEnvironments());
        Assertions.assertEquals("test", new EnvConfigProperties().getCurrentEnvironment());
        // when a multiple environments are specified
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test , TEST2");
        Assertions.assertEquals(List.of("test2", "test", "default"), new EnvConfigProperties().getEnvironments());
        Assertions.assertEquals("test2", new EnvConfigProperties().getCurrentEnvironment());
        // when a default specified in environments
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "DEFAULT,alpha,zen");
        Assertions.assertEquals(List.of("zen", "alpha", "default"), new EnvConfigProperties().getEnvironments());
        // when only default specified in environments
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "default");
        Assertions.assertEquals(List.of("default"), new EnvConfigProperties().getEnvironments());
    }

    @Test
    void testCanGetConfigAndProfilePath() {
        // when config.dir isn't specified
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Path.of(configProperties.getBuildDir() + "/config/test"),
            configProperties.getConfigPath("test"), "Incorrect config path");
        Assertions.assertEquals(Path.of(configProperties.getBuildDir() + "/config/test/profile1"),
            configProperties.getConfigProfilePath("test", "profile1"), "Incorrect config profile path");
    }

    @Test
    void testCanGetConfigProfilePathUsingConfiguredProfile() {
        systemProperties.set(EnvConfigKey.CONFIG_PROFILE.getProperty(), "profile1");
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Path.of(configProperties.getBuildDir() + "/config/test/profile1"),
            configProperties.getConfigProfilePath("test"), "Incorrect configured config profile path");
    }

    @Test
    void testExceptionWhenConfigPathDoesNotExist() {
        systemProperties.set(EnvConfigKey.CONFIG_PATH.getProperty(), "/non/existing/dir");
        final ThrowingConfigPath configPath = new ThrowingConfigPath();
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            configPath::invoke);
        Assertions.assertEquals("'/non/existing/dir' does not exist or not a valid config directory!",
            exception.getMessage());
    }

    @Test
    void testExceptionWhenConfigProfilePathDoesNotExist() {
        systemProperties.set(EnvConfigKey.CONFIG_PROFILES_PATH.getProperty(), "/non/existing/dir");
        final ThrowingConfigProfilePath configProfilePath = new ThrowingConfigProfilePath();
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            configProfilePath::invoke);
        Assertions.assertEquals("'/non/existing/dir' does not exist or not a valid config directory!",
            exception.getMessage());
    }

    @Test
    void testCanGetConfigPathWhenRelative() throws IOException {
        final Path directory = Files.createTempDirectory(Path.of("config"), "sample-dir");
        directory.toFile().deleteOnExit();
        // when config.dir is set to relative path
        systemProperties.set(EnvConfigKey.CONFIG_PATH.getProperty(), directory.toString());
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Path.of(directory.toAbsolutePath().toString(), "foo"),
            configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Path.of(directory.toAbsolutePath().toString(), "foo", "prof"),
            configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsoluteWithin() throws IOException {
        final Path directory = Files.createTempDirectory(Path.of(new EnvConfigProperties().getBuildDir()), "sample-dir");
        directory.toFile().deleteOnExit();
        // when config.dir is set to absolute
        systemProperties.set(EnvConfigKey.CONFIG_PATH.getProperty(), directory.toString());
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Path.of(directory.toString(), "foo"),
            configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Path.of(directory.toString(), "foo", "prof"),
            configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsolute() throws IOException {
        final Path directory = Files.createTempDirectory("sample-dir");
        directory.toFile().deleteOnExit();
        // when config.dir is set to absolute
        systemProperties.set(EnvConfigKey.CONFIG_PATH.getProperty(), directory.toString());
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Path.of(directory.toString(), "foo"),
            configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Path.of(directory.toString(), "foo", "prof"),
            configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigKeepassEnabled() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertFalse(configProperties.isConfigKeepassEnabled(), "Incorrect keepass.enabled");
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getProperty(), "true");
        Assertions.assertTrue(configProperties.isConfigKeepassEnabled(), "Incorrect keepass.enabled");
    }

    @Test
    void testCanGetConfigKeepassFileName() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), "foo");
        Assertions.assertEquals(new File(configProperties.getBuildDir()).getName(),
            configProperties.getKeepassProperties().filename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenRelative() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "foobar.kdbx");
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), "foo");
        Assertions.assertEquals("foobar.kdbx",
            configProperties.getKeepassProperties().filename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenAbsolute() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "/dir/foobar.kdbx");
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), "foo");
        Assertions.assertEquals("foobar.kdbx",
            configProperties.getKeepassProperties().filename(), "Incorrect keepass.filename path");
    }

    @Test
    void testThrowsExceptionWhenKeepassMasterKeyNotPresent() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            configProperties::getKeepassProperties);
        Assertions.assertEquals("Missing required variable '%s'".formatted("env.config.keepass.masterkey"),
            exception.getMessage());
    }

    private static final class ThrowingConfigPath {

        private void invoke() {
            new EnvConfigProperties().getConfigPath("env");
        }
    }

    private static final class ThrowingConfigProfilePath {

        private void invoke() {
            new EnvConfigProperties().getConfigProfilePath("env", "profile");
        }
    }

}
