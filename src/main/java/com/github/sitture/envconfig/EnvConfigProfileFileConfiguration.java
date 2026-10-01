package com.github.sitture.envconfig;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class EnvConfigProfileFileConfiguration extends EnvConfigFileConfiguration {

    EnvConfigProfileFileConfiguration(final Path configDirPath) {
        super(configDirPath);
    }

    @Override
    public List<File> listFiles() {
        final File configDir = configDirPath.toFile();
        return configDir.exists() ? getConfigProperties(configDir) : Collections.emptyList();
    }
}