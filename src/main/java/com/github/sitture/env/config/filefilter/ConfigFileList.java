package com.github.sitture.env.config.filefilter;

import com.github.sitture.env.config.ConfigException;
import com.github.sitture.env.config.utils.BuildDirUtils;
import com.github.sitture.env.config.utils.PropertyUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConfigFileList {

    private static final String CONFIG_DIR_KEY = "config.dir";
    private static final String DEFAULT_ENV_DIRECTORY = "config";
    protected final String configPath;
    private final FileFilter fileFilter;

    public ConfigFileList(final String env) {
        this.configPath = getConfigPath(env);
        this.fileFilter = new ConfigProperties();
    }

    public List<File> listFiles() {
        final File configDir = new File(configPath);
        if (!configDir.exists() || !configDir.isDirectory()) {
            throw new ConfigException(
                    "'" + configPath + "' does not exist or not a valid config directory!");
        }
        return getConfigProperties(configDir);
    }

    protected List<File> getConfigProperties(final File configDir) {
        final List<File> files = Arrays.asList(Objects.requireNonNull(configDir.listFiles(fileFilter)));
        if (files.isEmpty()) {
            throw new ConfigException("No property files found under '" + configDir.getPath() + "'");
        }
        return files;
    }

    private String getConfigDir() {
        return PropertyUtils.getProperty(CONFIG_DIR_KEY, DEFAULT_ENV_DIRECTORY);
    }

    private String getConfigPath(final String env) {
        return BuildDirUtils.getBuildDir() + File.separator + getConfigDir() + File.separator + env;
    }
}
