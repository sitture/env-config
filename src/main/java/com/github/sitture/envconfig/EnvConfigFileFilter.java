package com.github.sitture.envconfig;

import java.io.File;
import java.io.FileFilter;

class EnvConfigFileFilter implements FileFilter {

    @Override
    public boolean accept(final File file) {
        return !file.isDirectory() && file.getName().endsWith(".properties");
    }
}
