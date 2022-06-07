package com.github.sitture.envconfig;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class EnvConfigProfileFileList extends EnvConfigFileList {

    EnvConfigProfileFileList(final Path configPath) {
        super(configPath);
    }

    @Override
    public List<File> listFiles() {
        final File configDir = configPath.toFile();
        return configDir.exists() ? getConfigProperties(configDir) : Collections.emptyList();
    }
}