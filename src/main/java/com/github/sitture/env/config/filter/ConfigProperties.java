package com.github.sitture.env.config.filter;

import java.io.File;
import java.io.FileFilter;

public class ConfigProperties implements FileFilter {

    @Override
    public boolean accept(final File file) {
        return !file.isDirectory() && file.getName().endsWith(".properties");
    }
}
