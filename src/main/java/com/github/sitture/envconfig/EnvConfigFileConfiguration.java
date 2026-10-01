package com.github.sitture.envconfig;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EnvConfigFileConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EnvConfigFileConfiguration.class);
    protected final Path configDirPath;
    private final EnvConfigProperties configProperties;

    EnvConfigFileConfiguration(final Path configDirPath) {
        this.configDirPath = configDirPath;
        this.configProperties = new EnvConfigProperties();
    }

    public List<File> listFiles() {
        final File configDirPathFile = configDirPath.toFile();
        if (!configDirPathFile.exists() || !configDirPathFile.isDirectory()) {
            throw new EnvConfigException(
                "'" + configDirPath + "' does not exist or not a valid config directory!");
        }
        return getConfigProperties(configDirPathFile);
    }

    protected List<File> getConfigProperties(final File configDir) {
        final List<File> files = Arrays.asList(Objects.requireNonNull(configDir.listFiles(new EnvConfigFileFilter())));
        if (files.isEmpty()) {
            throw new EnvConfigException("No property files found under '" + configDir.getPath() + "'");
        }
        return files;
    }

    public Configuration getConfiguration() {
        final List<File> files = listFiles();
        if (files.isEmpty() && LOG.isDebugEnabled()) {
            LOG.debug("No property files found under {}", configDirPath);
        }
        final CompositeConfiguration fileConfiguration = new CompositeConfiguration();
        files.forEach(file -> fileConfiguration.addConfiguration(getFileConfigurationMap(file)));
        return fileConfiguration;
    }

    private Map<String, Configuration> getEnvironmentConfiguration(final List<String> environments) {
        final Map<String, Configuration> configurationMap = new HashMap<>();
        environments.forEach(env -> configurationMap.put(
            env, new EnvConfigFileConfiguration(this.configProperties.getConfigPath(env)).getConfiguration()));
        return configurationMap;
    }

    private Configuration getFileConfigurationMap(final File file) {
        final Map<String, Object> configurationMap = new HashMap<>();
        final Configuration properties = getConfigurationProperties(file);
        properties.getKeys().forEachRemaining(key -> {
            final Object value = properties.getProperty(key);
            configurationMap.put(EnvConfigUtils.getProcessedPropertyKey(key), value);
            configurationMap.put(EnvConfigUtils.getProcessedEnvKey(key), value);
        });
        return new MapConfiguration(configurationMap);
    }

    private Configuration getConfigurationProperties(final File file) {
        final Configuration configurationProperties;
        try {
            LOG.debug("Getting config from {}", file);
            configurationProperties = new Configurations().properties(file);
        } catch (ConfigurationException e) {
            throw new EnvConfigException(e);
        }
        return configurationProperties;
    }


}
