package com.github.sitture.envconfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class EnvConfigPropertiesTest {

    private EnvConfigProperties configProperties;

    @AfterEach
    void tearDown() {
        System.clearProperty(EnvConfigUtils.CONFIG_PATH_KEY);
        System.clearProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY);
        System.clearProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY);
        System.clearProperty(EnvConfigUtils.CONFIG_PROFILE_KEY);
        System.clearProperty(EnvConfigUtils.CONFIG_PROFILES_PATH_KEY);
        System.clearProperty(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY);
    }

    @Test
    void testCanGetBuildDir() {
        Assertions.assertEquals(System.getProperty("user.dir"), new EnvConfigProperties().getBuildDir(), "invalid buildDir!");
    }

    @Test
    void testCanGetConfigProfile() {
        configProperties = new EnvConfigProperties();
        System.clearProperty(EnvConfigUtils.CONFIG_PROFILE_KEY);
        Assertions.assertEquals("", configProperties.getConfigProfile(), "invalid config-profile!");
        System.setProperty(EnvConfigUtils.CONFIG_PROFILE_KEY, "test-profile");
        Assertions.assertEquals("test-profile", configProperties.getConfigProfile());
    }

    @Test
    void testCanGetEnvironmentsList() {
        // when config.environment isn't specified
        System.clearProperty(EnvConfigUtils.CONFIG_ENV_KEY);
        Assertions.assertEquals(List.of("default"), new EnvConfigProperties().getEnvironments());
        Assertions.assertEquals("default", new EnvConfigProperties().getCurrentEnvironment());
        // when a single environment is specified
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test ");
        Assertions.assertEquals(List.of("test", "default"), new EnvConfigProperties().getEnvironments());
        Assertions.assertEquals("test", new EnvConfigProperties().getCurrentEnvironment());
        // when a multiple environments are specified
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test , TEST2");
        Assertions.assertEquals(List.of("test2", "test", "default"), new EnvConfigProperties().getEnvironments());
        Assertions.assertEquals("test2", new EnvConfigProperties().getCurrentEnvironment());
        // when a default specified in environments
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "DEFAULT,alpha,zen");
        Assertions.assertEquals(List.of("zen", "alpha", "default"), new EnvConfigProperties().getEnvironments());
        // when only default specified in environments
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "default");
        Assertions.assertEquals(List.of("default"), new EnvConfigProperties().getEnvironments());
    }

    @Test
    void testCanGetConfigAndProfilePath() {
        // when config.dir isn't specified
        configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Paths.get(configProperties.getBuildDir() + "/config/test"),
                configProperties.getConfigPath("test"), "Incorrect config path");
        Assertions.assertEquals(Paths.get(configProperties.getBuildDir() + "/config/test/profile1"),
                configProperties.getConfigProfilePath("test", "profile1"), "Incorrect config profile path");
    }

    @Test
    void testExceptionWhenConfigPathDoesNotExist() {
        System.setProperty(EnvConfigUtils.CONFIG_PATH_KEY, "/non/existing/dir");
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                () -> new EnvConfigProperties().getConfigPath("env"));
        Assertions.assertEquals("'/non/existing/dir' does not exist or not a valid config directory!",
                exception.getMessage());
    }

    @Test
    void testExceptionWhenConfigProfilePathDoesNotExist() {
        System.setProperty(EnvConfigUtils.CONFIG_PROFILES_PATH_KEY, "/non/existing/dir");
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
        System.setProperty(EnvConfigUtils.CONFIG_PATH_KEY, directory.toString());
        configProperties = new EnvConfigProperties();
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
        System.setProperty(EnvConfigUtils.CONFIG_PATH_KEY, directory.toString());
        configProperties = new EnvConfigProperties();
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
        System.setProperty(EnvConfigUtils.CONFIG_PATH_KEY, directory.toString());
        configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Paths.get(directory.toString(), "foo"),
                configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Paths.get(directory.toString(), "foo", "prof"),
                configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigKeepassEnabled() {
        configProperties = new EnvConfigProperties();
        Assertions.assertFalse(configProperties.isConfigKeepassEnabled(), "Incorrect keepass.enabled");
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY, "true");
        Assertions.assertTrue(configProperties.isConfigKeepassEnabled(), "Incorrect keepass.enabled");
    }

    @Test
    void testCanGetConfigKeepassFileName() {
        configProperties = new EnvConfigProperties();
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY, "foo");
        Assertions.assertEquals(new File(configProperties.getBuildDir()).getName(),
                configProperties.getKeepassProperties().getFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenRelative() {
        configProperties = new EnvConfigProperties();
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY, "foobar.kdbx");
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY, "foo");
        Assertions.assertEquals("foobar.kdbx",
                configProperties.getKeepassProperties().getFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenAbsolute() {
        configProperties = new EnvConfigProperties();
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY, "/dir/foobar.kdbx");
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY, "foo");
        Assertions.assertEquals("foobar.kdbx",
                configProperties.getKeepassProperties().getFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testThrowsExceptionWhenKeepassMasterKeyNotPresent() {
        configProperties = new EnvConfigProperties();
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                configProperties::getKeepassProperties);
        Assertions.assertEquals(String.format("Missing required variable '%s'", EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY),
                exception.getMessage());
    }

}
