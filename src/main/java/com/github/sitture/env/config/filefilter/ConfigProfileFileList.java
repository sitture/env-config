package com.github.sitture.env.config.filefilter;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ConfigProfileFileList extends ConfigFileList {

    protected final String configProfilePath;

    public ConfigProfileFileList(final String env, final String profile) {
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