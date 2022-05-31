package com.github.sitture.envconfig;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class EnvConfigFileList {

    protected final String configPath;

    EnvConfigFileList(final String env) {
        this.configPath = EnvConfigUtils.getConfigPath(env);
    }

    public List<File> listFiles() {
        final File configDir = new File(configPath);
        if (!configDir.exists() || !configDir.isDirectory()) {
            throw new EnvConfigException(
                    "'" + configPath + "' does not exist or not a valid config directory!");
        }
        return getConfigProperties(configDir);
    }

    protected List<File> getConfigProperties(final File configDir) {
        final List<File> files = Arrays.asList(Objects.requireNonNull(configDir.listFiles(new EnvConfigFileFilter())));
        if (files.isEmpty()) {
            throw new EnvConfigException("No property files found under '" + configDir.getPath() + "'");
        }
        return files;
    }
}
