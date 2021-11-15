package com.github.sitture.env.config.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildDirUtilsTest {

    private static final String CONFIG_DIR_KEY = "config.dir";
    private static final String CONFIG_KEEPASS_FILENAME_KEY = "config.keepass.filename";

    @AfterEach
    void tearDown() {
        System.clearProperty(CONFIG_DIR_KEY);
        System.clearProperty(CONFIG_KEEPASS_FILENAME_KEY);
    }

    @Test
    void testCanGetBuildDir() {
        assertEquals("invalid buildDir!", System.getProperty("user.dir"), BuildDirUtils.getBuildDir());
    }

    @Test
    void testCanGetConfigPath() {
        // when config.dir isn't specified
        assertEquals("Incorrect config path",
                BuildDirUtils.getBuildDir() + "/config/test", BuildDirUtils.getConfigPath("test"));
    }

    @Test
    void testCanGetConfigPathWhenRelative() {
        // when config.dir is set to relative path
        System.setProperty(CONFIG_DIR_KEY, "env/dir");
        assertEquals("Incorrect config path",
                BuildDirUtils.getBuildDir() + "/env/dir/foo", BuildDirUtils.getConfigPath("foo"));
    }

    @Test
    void testCanGetConfigPathWhenAbsoluteWithin() {
        // when config.dir is set to absolute
        System.setProperty(CONFIG_DIR_KEY, BuildDirUtils.getBuildDir() + "/env/dir");
        assertEquals("Incorrect config path",
                BuildDirUtils.getBuildDir() + "/env/dir/foo", BuildDirUtils.getConfigPath("foo"));
    }

    @Test
    void testCanGetConfigPathWhenAbsolute() {
        // when config.dir is set to absolute
        System.setProperty(CONFIG_DIR_KEY, "/usr/dir/env/dir");
        assertEquals("Incorrect config path",
                "/usr/dir/env/dir/foo", BuildDirUtils.getConfigPath("foo"));
    }

    @Test
    void testCanGetConfigKeepassFileName() {
        // when config.keepass.filename isn't specified
        assertEquals("Incorrect config.keepass.filename path",
                new File(BuildDirUtils.getBuildDir()).getName(), BuildDirUtils.getConfigKeePassFilename());
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenRelative() {
        // when config.keepass.filename isn't specified
        System.setProperty(CONFIG_KEEPASS_FILENAME_KEY, "foobar.kdbx");
        assertEquals("Incorrect config.keepass.filename path",
                "foobar.kdbx", BuildDirUtils.getConfigKeePassFilename());
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenAbsolute() {
        // when config.keepass.filename isn't specified
        System.setProperty(CONFIG_KEEPASS_FILENAME_KEY, "/dir/foobar.kdbx");
        assertEquals("Incorrect config.keepass.filename path",
                "foobar.kdbx", BuildDirUtils.getConfigKeePassFilename());
    }

}
