package com.github.sitture.env.config.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Optional;

public class ProfileSpecificProperties implements FileFilter {

    private final String profile;
    private static final String CONFIG_ENV_PROFILE_KEY = "CONFIG_ENV_PROFILE";

    public ProfileSpecificProperties() {
        this.profile = loadProfile();
    }

    @Override
    public boolean accept(final File file) {
        final String filename = file.getName();
        boolean matched = file.getName().endsWith(".properties");
        if (filename.matches(".*-.*.properties")) {
            matched = this.profile!=null && filename.matches(String.format(".*-%s.properties", profile));
        }
        return matched;
    }

    private String loadProfile() {
        return Optional.ofNullable(System.getenv(CONFIG_ENV_PROFILE_KEY)).orElse(System.getProperty(CONFIG_ENV_PROFILE_KEY));
    }
}
