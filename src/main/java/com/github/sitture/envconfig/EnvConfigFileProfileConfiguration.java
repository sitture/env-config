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
        return configDir.exists() ? getConfigProperties(configDir) : Collections.emptyList();
    }

    @Override
    public Map<String, Configuration> getConfiguration() {
        final Map<String, Configuration> configurationMap = new HashMap<>();
        this.configProperties.getEnvironments().forEach(env -> configurationMap.put(
            env, getConfiguration(this.configProperties.getConfigProfilePath(env))));
        return configurationMap;
    }
}