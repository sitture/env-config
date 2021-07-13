package com.github.sitture.env.config.utils;

public final class BuildDirUtils {

    private BuildDirUtils() {

    }

    public static String getBuildDir() {
        final String workingDirectory = System.getProperty("user.dir");
        return System.getProperty("project.build.directory", workingDirectory);
    }

}
