package com.github.sitture.envconfig;

import java.io.File;
import java.util.Collections;
import java.util.List;

class EnvConfigProfileFileList extends EnvConfigFileList {

    private final String configProfilePath;

    EnvConfigProfileFileList(final String env, final String profile) {
        super(env);
        this.configProfilePath = getConfigProfilePath(profile);
    }

    @Override
    public List<File> listFiles() {
        final File configDir = new File(configProfilePath);
        return configDir.exists() ? getConfigProperties(configDir) : Collections.emptyList();
    }

    private String getConfigProfilePath(final String profile) {
        return configPath + File.separator + profile;
    }
}