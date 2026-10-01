package com.github.sitture.envconfig;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration2.Configuration;

class EnvConfigFileProfileConfiguration extends EnvConfigFileConfiguration implements EnvConfigConfiguration {

    EnvConfigFileProfileConfiguration(final EnvConfigProperties configProperties) {
        super(configProperties);
    }

    @Override
    protected List<File> getConfigFiles(final Path configDirPath) {
        final File configDir = configDirPath.toFile();
        final List<File> files;
        if (!configDir.exists()) {
            files = Collections.emptyList();
        } else if (!configDir.isDirectory()) {
            throw new EnvConfigException(
                "'" + configDirPath + "' does not exist or not a valid config directory!");
        } else {
            files = getConfigProperties(configDir);
        }
        return files;
    }

    @Override
    public Map<String, Configuration> getConfiguration() {
        final Map<String, Configuration> configurationMap = new HashMap<>();
        this.configProperties.getEnvironments().forEach(env -> configurationMap.put(
            env, getConfiguration(this.configProperties.getConfigProfilePath(env))));
        return configurationMap;
    }
}