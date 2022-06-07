package com.github.sitture.envconfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
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
        Assertions.assertEquals(List.of("default"), new EnvConfigProperties().getEnvironments());
        // when a single environment is specified
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test ");
        Assertions.assertEquals(List.of("test", "default"), new EnvConfigProperties().getEnvironments());
        // when a multiple environments are specified
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "test , test2");
        Assertions.assertEquals(List.of("test2", "test", "default"), new EnvConfigProperties().getEnvironments());
        // when a default specified in environments
        System.setProperty(EnvConfigUtils.CONFIG_ENV_KEY, "default,alpha,zen");
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
    void testCanGetConfigPathWhenRelative() {
        // when config.dir is set to relative path
        System.setProperty(EnvConfigUtils.CONFIG_PATH_KEY, "env/dir");
        configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Paths.get(configProperties.getBuildDir() + "/env/dir/foo"),
                configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Paths.get(configProperties.getBuildDir() + "/env/dir/foo/prof"),
                configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsoluteWithin() {
        // when config.dir is set to absolute
        System.setProperty(EnvConfigUtils.CONFIG_PATH_KEY, new EnvConfigProperties().getBuildDir() + "/env/dir");
        configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Paths.get(configProperties.getBuildDir() + "/env/dir/foo"),
                configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Paths.get(configProperties.getBuildDir() + "/env/dir/foo/prof"),
                configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsolute() {
        // when config.dir is set to absolute
        System.setProperty(EnvConfigUtils.CONFIG_PATH_KEY, "/usr/dir/env/dir");
        configProperties = new EnvConfigProperties();
        Assertions.assertEquals(Paths.get("/usr/dir/env/dir/foo"),
                configProperties.getConfigPath("foo"), "Incorrect config path");
        Assertions.assertEquals(Paths.get("/usr/dir/env/dir/foo/prof"),
                configProperties.getConfigProfilePath("foo", "prof"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigKeepassEnabled() {
        configProperties = new EnvConfigProperties();
        Assertions.assertFalse(configProperties.isConfigKeePassEnabled(), "Incorrect keepass.enabled");
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY, "true");
        Assertions.assertTrue(configProperties.isConfigKeePassEnabled(), "Incorrect keepass.enabled");
    }

    @Test
    void testCanGetConfigKeepassFileName() {
        configProperties = new EnvConfigProperties();
        Assertions.assertEquals(new File(configProperties.getBuildDir()).getName(),
                configProperties.getConfigKeePassFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenRelative() {
        configProperties = new EnvConfigProperties();
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY, "foobar.kdbx");
        Assertions.assertEquals("foobar.kdbx",
                configProperties.getConfigKeePassFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenAbsolute() {
        configProperties = new EnvConfigProperties();
        System.setProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY, "/dir/foobar.kdbx");
        Assertions.assertEquals("foobar.kdbx",
                configProperties.getConfigKeePassFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testThrowsExceptionWhenKeepassMasterKeyNotPresent() {
        configProperties = new EnvConfigProperties();
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                configProperties::getConfigKeePassMasterKey);
        Assertions.assertEquals(String.format("Missing required variable '%s'", EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY),
                exception.getMessage());
    }

}
