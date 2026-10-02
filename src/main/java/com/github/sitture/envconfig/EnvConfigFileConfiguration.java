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

class EnvConfigFileConfiguration extends AbstractEnvConfigConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EnvConfigFileConfiguration.class);
    protected final EnvConfigProperties configProperties;

    EnvConfigFileConfiguration(final EnvConfigProperties configProperties) {
        super(configProperties.getEnvironments());
        this.configProperties = configProperties;
    }

    List<File> getConfigFiles(final Path configDirPath) {
        final File configDirectory = configDirPath.toFile();
        if (!configDirectory.exists() || !configDirectory.isDirectory()) {
            throw new EnvConfigException(
                "'" + configDirPath + "' does not exist or not a valid config directory!");
        }
        return getConfigProperties(configDirectory);
    }

    List<File> getConfigProperties(final File configDir) {
        final List<File> files = Arrays.asList(Objects.requireNonNull(configDir.listFiles(new EnvConfigFileFilter())));
        if (files.isEmpty()) {
            throw new EnvConfigException("No property files found under '" + configDir.getPath() + "'");
        }
        return files;
    }

    @Override
    protected Configuration getConfigurationForEnvironment(final String env) {
        final Path configDirPath = getConfigurationDirectoryPath(env);
        final List<File> files = getConfigFiles(configDirPath);
        if (files.isEmpty() && LOG.isDebugEnabled()) {
            LOG.debug("No property files found under {}", configDirPath);
        }
        final CompositeConfiguration fileConfiguration = new CompositeConfiguration();
        files.forEach(file -> fileConfiguration.addConfiguration(getFileConfigurationMap(file)));
        return fileConfiguration;
    }

    protected Path getConfigurationDirectoryPath(final String env) {
        return this.configProperties.getConfigPath(env);
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
