package com.github.sitture.env.config.filefilter;

import com.github.sitture.env.config.utils.BuildDirUtils;
import com.github.sitture.env.config.ConfigException;
import com.github.sitture.env.config.utils.PropertyUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

public class ConfigFileList {

    private static final String CONFIG_DIR_KEY = "config.dir";
    private static final String DEFAULT_ENV_DIRECTORY = "config";
    private final String configPath;
    private final FileFilter fileFilter;

    public ConfigFileList(final String env, final FileFilter fileFilter) {
        this.configPath = getConfigPath(env);
        this.fileFilter = fileFilter;
    }

    public List<File> listFiles() {
        final File configDir = new File(configPath);
        if (!configDir.exists() || !configDir.isDirectory()) {
            throw new ConfigException(
                    "'" + configPath + "' does not exist or not a valid config directory!");
        }
        return getConfigProperties(configDir);
    }

    private String getConfigDir() {
        return PropertyUtils.getProperty(CONFIG_DIR_KEY, DEFAULT_ENV_DIRECTORY);
    }


    private String getConfigPath(final String env) {
        return BuildDirUtils.getBuildDir() + File.separator + getConfigDir() + File.separator + env;
    }

    private List<File> getConfigProperties(final File configDir) {
        final List<File> files = Arrays.asList(configDir.listFiles(fileFilter));
        if (files.isEmpty()) {
            throw new ConfigException("No property files found under '" + configPath + "'");
        }
        return files;
    }
}
