package com.github.sitture.env.config.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class BuildDirUtils {

    private static final String CONFIG_DIR_KEY = "config.dir";
    private static final String DEFAULT_ENV_DIRECTORY = "config";
    private static final String CONFIG_KEEPASS_FILENAME_KEY = "config.keepass.filename";

    private BuildDirUtils() {
    }

    public static String getBuildDir() {
        final String workingDirectory = System.getProperty("user.dir");
        return Paths.get(System.getProperty("project.build.directory", workingDirectory)).toString();
    }

    private static Path getConfigDir() {
        return Paths.get(PropertyUtils.getProperty(CONFIG_DIR_KEY, DEFAULT_ENV_DIRECTORY)).toAbsolutePath();
    }

    public static String getConfigPath(final String env) {
        return Paths.get(getConfigDir().toString(), env).toString();
    }

    public static String getConfigKeePassFilename() {
        return new File(PropertyUtils.getProperty(CONFIG_KEEPASS_FILENAME_KEY, getBuildDir())).getName();
    }

}
