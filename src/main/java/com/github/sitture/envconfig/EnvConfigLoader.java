package com.github.sitture.envconfig;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EnvConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(EnvConfigLoader.class);
    protected final CompositeConfiguration configuration = new CompositeConfiguration();
    protected final EnvConfigProperties configProperties = new EnvConfigProperties();

    EnvConfigLoader() {
        final List<String> environments = this.configProperties.getEnvironments();
        final String configProfile = this.configProperties.getConfigProfile();
        loadEnvConfigurations(environments);
        loadKeepassConfigurations(environments);
        if (!configProfile.isEmpty()) {
            LOG.debug("Loading properties from profile {} under environments {}", configProfile, environments);
            environments.forEach(env -> loadFileConfigurations(new EnvConfigProfileFileList(this.configProperties.getConfigProfilePath(env, configProfile))));
        }
        LOG.debug("Loading properties from environment directories {}", environments);
        environments.forEach(env -> loadFileConfigurations(new EnvConfigFileList(this.configProperties.getConfigPath(env))));
    }

    private void loadKeepassConfigurations(final List<String> environments) {
        if (this.configProperties.isConfigKeePassEnabled()) {
            final String groupName = this.configProperties.getConfigKeePassFilename();
            final String masterKey = this.configProperties.getConfigKeePassMasterKey();
            LOG.debug("Loading properties from keepass {}", groupName);
            final KeePassEntries keepassEntries = new KeePassEntries(masterKey, groupName);
            environments.forEach(env -> this.configuration.addConfiguration(keepassEntries.getEntriesConfiguration(env)));
        }
    }

    private void loadEnvConfigurations(final List<String> environments) {
        final EnvironmentVariables envVars = new EnvironmentVariables();
        LOG.debug("Loading properties from system.properties");
        this.configuration.addConfiguration(envVars.getSystemConfiguration());
        final Configuration envOverrides = envVars.getEnvironmentConfiguration();
        environments.forEach(env -> {
            for (final File file : new EnvConfigFileList(this.configProperties.getConfigPath(env)).listFiles()) {
                final Map<String, Object> configurationMap = getFileConfigurationMap(file);
                configurationMap.keySet().forEach(key -> {
                    if (envOverrides.containsKey(key)
                            && configurationMap.get(key).equals(envOverrides.getProperty(key))) {
                        envOverrides.clearProperty(key);
                    }
                });
            }
        });
        LOG.debug("Loading properties from system.env");
        this.configuration.addConfiguration(envOverrides);
    }

    private void loadFileConfigurations(final EnvConfigFileList fileList) {
        if (fileList.listFiles().isEmpty()) {
            LOG.debug("No property files found under {}", fileList.configPath);
        }
        fileList.listFiles().forEach(file ->
                this.configuration.addConfiguration(new MapConfiguration(getFileConfigurationMap(file))));
    }

    private Map<String, Object> getFileConfigurationMap(final File file) {
        final Map<String, Object> configurationMap = new HashMap<>();
        final Configuration properties = getConfigurationProperties(file);
        properties.getKeys().forEachRemaining(key -> {
            final Object value = properties.getProperty(key);
            configurationMap.put(EnvConfigUtils.getProcessedPropertyKey(key), value);
            configurationMap.put(EnvConfigUtils.getProcessedEnvKey(key), value);
        });
        return configurationMap;
    }

    private Configuration getConfigurationProperties(final File file) {
        final Configuration configurationProperties;
        try {
            LOG.debug("Loading properties from {}", file);
            configurationProperties = new Configurations().properties(file);
        } catch (ConfigurationException e) {
            throw new EnvConfigException(e);
        }
        return configurationProperties;
    }
}
