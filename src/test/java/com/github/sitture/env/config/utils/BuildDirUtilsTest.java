package com.github.sitture.env.config.utils;

import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class BuildDirUtilsTest {

    private static final String CONFIG_DIR_KEY = "config.dir";
    private static final String CONFIG_KEEPASS_FILENAME_KEY = "config.keepass.filename";

    @After
    public void tearDown() {
        System.clearProperty(CONFIG_DIR_KEY);
        System.clearProperty(CONFIG_KEEPASS_FILENAME_KEY);
    }

    @Test
    public void testCanGetBuildDir() {
        assertEquals("invalid buildDir!", System.getProperty("user.dir"), BuildDirUtils.getBuildDir());
    }

    @Test
    public void testCanGetConfigPath() {
        // when config.dir isn't specified
        assertEquals("Incorrect config path",
                BuildDirUtils.getBuildDir() + "/config/test", BuildDirUtils.getConfigPath("test"));
    }

    @Test
    public void testCanGetConfigPathWhenRelative() {
        // when config.dir is set to relative path
        System.setProperty(CONFIG_DIR_KEY, "env/dir");
        assertEquals("Incorrect config path",
                BuildDirUtils.getBuildDir() + "/env/dir/foo", BuildDirUtils.getConfigPath("foo"));
    }

    @Test
    public void testCanGetConfigPathWhenAbsoluteWithin() {
        // when config.dir is set to absolute
        System.setProperty(CONFIG_DIR_KEY, BuildDirUtils.getBuildDir() + "/env/dir");
        assertEquals("Incorrect config path",
                BuildDirUtils.getBuildDir() + "/env/dir/foo", BuildDirUtils.getConfigPath("foo"));
    }

    @Test
    public void testCanGetConfigPathWhenAbsolute() {
        // when config.dir is set to absolute
        System.setProperty(CONFIG_DIR_KEY, "/usr/dir/env/dir");
        assertEquals("Incorrect config path",
                "/usr/dir/env/dir/foo", BuildDirUtils.getConfigPath("foo"));
    }

    @Test
    public void testCanGetConfigKeepassFileName() {
        // when config.keepass.filename isn't specified
        assertEquals("Incorrect config.keepass.filename path",
                new File(BuildDirUtils.getBuildDir()).getName(), BuildDirUtils.getConfigKeePassFilename());
    }

    @Test
    public void testCanGetConfigKeepassFileNameWhenRelative() {
        // when config.keepass.filename isn't specified
        System.setProperty(CONFIG_KEEPASS_FILENAME_KEY, "foobar.kdbx");
        assertEquals("Incorrect config.keepass.filename path",
                "foobar.kdbx", BuildDirUtils.getConfigKeePassFilename());
    }

    @Test
    public void testCanGetConfigKeepassFileNameWhenAbsolute() {
        // when config.keepass.filename isn't specified
        System.setProperty(CONFIG_KEEPASS_FILENAME_KEY, "/dir/foobar.kdbx");
        assertEquals("Incorrect config.keepass.filename path",
                "foobar.kdbx", BuildDirUtils.getConfigKeePassFilename());
    }

}
