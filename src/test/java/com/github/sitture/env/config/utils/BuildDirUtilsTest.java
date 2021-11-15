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
        assertEquals(System.getProperty("user.dir"), BuildDirUtils.getBuildDir(), "invalid buildDir!");
    }

    @Test
    void testCanGetConfigPath() {
        // when config.dir isn't specified
        assertEquals(BuildDirUtils.getBuildDir() + "/config/test",
                BuildDirUtils.getConfigPath("test"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenRelative() {
        // when config.dir is set to relative path
        System.setProperty(CONFIG_DIR_KEY, "env/dir");
        assertEquals(BuildDirUtils.getBuildDir() + "/env/dir/foo",
                BuildDirUtils.getConfigPath("foo"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsoluteWithin() {
        // when config.dir is set to absolute
        System.setProperty(CONFIG_DIR_KEY, BuildDirUtils.getBuildDir() + "/env/dir");
        assertEquals(BuildDirUtils.getBuildDir() + "/env/dir/foo",
                BuildDirUtils.getConfigPath("foo"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigPathWhenAbsolute() {
        // when config.dir is set to absolute
        System.setProperty(CONFIG_DIR_KEY, "/usr/dir/env/dir");
        assertEquals("/usr/dir/env/dir/foo",
                BuildDirUtils.getConfigPath("foo"), "Incorrect config path");
    }

    @Test
    void testCanGetConfigKeepassFileName() {
        // when config.keepass.filename isn't specified
        assertEquals(new File(BuildDirUtils.getBuildDir()).getName(),
                BuildDirUtils.getConfigKeePassFilename(), "Incorrect config.keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenRelative() {
        // when config.keepass.filename isn't specified
        System.setProperty(CONFIG_KEEPASS_FILENAME_KEY, "foobar.kdbx");
        assertEquals("foobar.kdbx",
                BuildDirUtils.getConfigKeePassFilename(), "Incorrect config.keepass.filename path");
    }

    @Test
    void testCanGetConfigKeepassFileNameWhenAbsolute() {
        // when config.keepass.filename isn't specified
        System.setProperty(CONFIG_KEEPASS_FILENAME_KEY, "/dir/foobar.kdbx");
        assertEquals("foobar.kdbx",
                BuildDirUtils.getConfigKeePassFilename(), "Incorrect config.keepass.filename path");
    }

}
