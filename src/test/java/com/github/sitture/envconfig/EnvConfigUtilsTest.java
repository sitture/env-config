package com.github.sitture.envconfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.github.sitture.envconfig.EnvConfigProperties.CONFIG_KEEPASS_FILENAME_KEY;
import static com.github.sitture.envconfig.EnvConfigProperties.CONFIG_PATH_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvConfigUtilsTest {

    @AfterEach
    void tearDown() {
        System.clearProperty(CONFIG_PATH_KEY);
        System.clearProperty(CONFIG_KEEPASS_FILENAME_KEY);
    }

    @Test
    void testCanGetBuildDir() {
        assertEquals(System.getProperty("user.dir"), EnvConfigUtils.getBuildDir(), "invalid buildDir!");
    }

    @Test
    void testCanGetConfigPath() {
        // when config.dir isn't specified
        assertEquals(EnvConfigUtils.getBuildDir() + "/config/test",
                EnvConfigUtils.getConfigPath("test"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenRelative() {
        // when config.dir is set to relative path
        System.setProperty(CONFIG_PATH_KEY, "env/dir");
        assertEquals(EnvConfigUtils.getBuildDir() + "/env/dir/foo",
                EnvConfigUtils.getConfigPath("foo"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsoluteWithin() {
        // when config.dir is set to absolute
        System.setProperty(CONFIG_PATH_KEY, EnvConfigUtils.getBuildDir() + "/env/dir");
        assertEquals(EnvConfigUtils.getBuildDir() + "/env/dir/foo",
                EnvConfigUtils.getConfigPath("foo"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsolute() {
        // when config.dir is set to absolute
        System.setProperty(CONFIG_PATH_KEY, "/usr/dir/env/dir");
        assertEquals("/usr/dir/env/dir/foo",
                EnvConfigUtils.getConfigPath("foo"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigKeepassFileName() {
        assertEquals(new File(EnvConfigUtils.getBuildDir()).getName(),
                EnvConfigUtils.getConfigKeePassFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenRelative() {
        System.setProperty(CONFIG_KEEPASS_FILENAME_KEY, "foobar.kdbx");
        assertEquals("foobar.kdbx",
                EnvConfigUtils.getConfigKeePassFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenAbsolute() {
        System.setProperty(CONFIG_KEEPASS_FILENAME_KEY, "/dir/foobar.kdbx");
        assertEquals("foobar.kdbx",
                EnvConfigUtils.getConfigKeePassFilename(), "Incorrect keepass.filename path");
    }

    @Test
    void testMissingVariableExceptionThrown() {
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                () -> EnvConfig.getOrThrow("non.existing"));
        assertEquals("Missing required variable 'non.existing'", exception.getMessage());
    }

    @Test
    void testThrowsExceptionWhenKeepassMasterKeyNotPresent() {
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                EnvConfigUtils::getConfigKeePassMasterKey);
        assertEquals(String.format("Missing required variable '%s'", EnvConfigProperties.CONFIG_KEEPASS_MASTERKEY_KEY),
                exception.getMessage());
    }

}
