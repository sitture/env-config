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
    protected List<File> getConfigFiles(final Path configDirPath) {
        final File configDir = configDirPath.toFile();
        return configDir.exists() ? getConfigProperties(configDir) : Collections.emptyList();
    }
}