package com.github.sitture.envconfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnvConfigPropertiesTest {

    @AfterEach
    void tearDown() {
        System.clearProperty(EnvConfigKey.CONFIG_PATH.getProperty());
        System.clearProperty(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getProperty());
        System.clearProperty(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty());
        System.clearProperty(EnvConfigKey.CONFIG_PROFILE.getProperty());
        System.clearProperty(EnvConfigKey.CONFIG_PROFILES_PATH.getProperty());
        System.clearProperty(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty());
    }

    @Test
    void testCanGetBuildDir() {
        Assertions.assertEquals(System.getProperty("user.dir"), new EnvConfigProperties().getBuildDir(), "invalid buildDir!");
    }

    @Test
    void testCanGetConfigProfile() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        System.clearProperty(EnvConfigKey.CONFIG_PROFILE.getProperty());
        Assertions.assertEquals("", configProperties.getConfigProfile(), "invalid config-profile!");
        System.setProperty(EnvConfigKey.CONFIG_PROFILE.getProperty(), "test-profile");
        Assertions.assertEquals("test-profile", configProperties.getConfigProfile());
    }

    @Test
    void testCanGetEnvironmentsList() {
        // when config.environment isn't specified
        System.clearProperty(EnvConfigKey.CONFIG_ENV.getProperty());
        Assertions.assertEquals(List.of("default"), new EnvConfigProperties().getEnvironments());
        Assertions.assertEquals("default", new EnvConfigProperties().getCurrentEnvironment());
        // when a single environment is specified
        System.setProperty(EnvConfigKey.CONFIG_ENV.getProperty(), "test ");
        Assertions.assertEquals(List.of("test", "default"), new EnvConfigProperties().getEnvironments());
        Assertions.assertEquals("test", new EnvConfigProperties().getCurrentEnvironment());
        // when a multiple environments are specified
        System.setProperty(EnvConfigKey.CONFIG_ENV.getProperty(), "test , TEST2");
        Assertions.assertEquals(List.of("test2", "test", "default"), new EnvConfigProperties().getEnvironments());
        Assertions.assertEquals("test2", new EnvConfigProperties().getCurrentEnvironment());
        // when a default specified in environments
        System.setProperty(EnvConfigKey.CONFIG_ENV.getProperty(), "DEFAULT,alpha,zen");
        Assertions.assertEquals(List.of("zen", "alpha", "default"), new EnvConfigProperties().getEnvironments());
        // when only default specified in environments
        System.setProperty(EnvConfigKey.CONFIG_ENV.getProperty(), "default");
        Assertions.assertEquals(List.of("default"), new EnvConfigProperties().getEnvironments());
    }

    @Test
    void testCanGetConfigAndProfilePath() {
        // when config.dir isn't specified
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Paths.get(configProperties.getBuildDir() + "/config/test"),
            configProperties.getConfigPath("test"), "Incorrect config path");
        Assertions.assertEquals(Paths.get(configProperties.getBuildDir() + "/config/test/profile1"),
            configProperties.getConfigProfilePath("test", "profile1"), "Incorrect config profile path");
    }

    @Test
    void testExceptionWhenConfigPathDoesNotExist() {
        System.setProperty(EnvConfigKey.CONFIG_PATH.getProperty(), "/non/existing/dir");
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> new EnvConfigProperties().getConfigPath("env"));
        Assertions.assertEquals("'/non/existing/dir' does not exist or not a valid config directory!",
            exception.getMessage());
    }

    @Test
    void testExceptionWhenConfigProfilePathDoesNotExist() {
        System.setProperty(EnvConfigKey.CONFIG_PROFILES_PATH.getProperty(), "/non/existing/dir");
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> new EnvConfigProperties().getConfigProfilePath("env", "profile"));
        Assertions.assertEquals("'/non/existing/dir' does not exist or not a valid config directory!",
            exception.getMessage());
    }

    @Test
    void testCanGetConfigPathWhenRelative() throws IOException {
        final Path directory = Files.createTempDirectory(Paths.get("config"), "sample-dir");
        directory.toFile().deleteOnExit();
        // when config.dir is set to relative path
        System.setProperty(EnvConfigKey.CONFIG_PATH.getProperty(), directory.toString());
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Paths.get(directory.toAbsolutePath().toString(), "foo"),
            configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Paths.get(directory.toAbsolutePath().toString(), "foo", "prof"),
            configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsoluteWithin() throws IOException {
        final Path directory = Files.createTempDirectory(Path.of(new EnvConfigProperties().getBuildDir()), "sample-dir");
        directory.toFile().deleteOnExit();
        // when config.dir is set to absolute
        System.setProperty(EnvConfigKey.CONFIG_PATH.getProperty(), directory.toString());
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Paths.get(directory.toString(), "foo"),
            configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Paths.get(directory.toString(), "foo", "prof"),
            configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsolute() throws IOException {
        final Path directory = Files.createTempDirectory("sample-dir");
        directory.toFile().deleteOnExit();
        // when config.dir is set to absolute
        System.setProperty(EnvConfigKey.CONFIG_PATH.getProperty(), directory.toString());
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Paths.get(directory.toString(), "foo"),
            configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Paths.get(directory.toString(), "foo", "prof"),
            configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigKeepassEnabled() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        Assertions.assertFalse(configProperties.isConfigKeepassEnabled(), "Incorrect keepass.enabled");
        System.setProperty(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getProperty(), "true");
        Assertions.assertTrue(configProperties.isConfigKeepassEnabled(), "Incorrect keepass.enabled");
    }

    @Test
    void testCanGetConfigKeepassFileName() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        System.setProperty(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), "foo");
        Assertions.assertEquals(new File(configProperties.getBuildDir()).getName(),
            configProperties.getKeepassProperties().getFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenRelative() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        System.setProperty(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "foobar.kdbx");
        System.setProperty(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), "foo");
        Assertions.assertEquals("foobar.kdbx",
            configProperties.getKeepassProperties().getFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenAbsolute() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        System.setProperty(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "/dir/foobar.kdbx");
        System.setProperty(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), "foo");
        Assertions.assertEquals("foobar.kdbx",
            configProperties.getKeepassProperties().getFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testThrowsExceptionWhenKeepassMasterKeyNotPresent() {
        final EnvConfigProperties configProperties = new EnvConfigProperties();
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            configProperties::getKeepassProperties);
        Assertions.assertEquals(String.format("Missing required variable '%s'", "env.config.keepass.masterkey"),
            exception.getMessage());
    }

}
