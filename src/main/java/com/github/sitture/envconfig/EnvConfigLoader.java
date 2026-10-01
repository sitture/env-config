package com.github.sitture.envconfig;

import java.util.List;
import java.util.Map;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EnvConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(EnvConfigLoader.class);
    public static final int ENVIRONMENTS_WITH_PARENT = 2;
    protected final CompositeConfiguration configuration = new CompositeConfiguration();
    protected final EnvConfigProperties configProperties = new EnvConfigProperties();

    EnvConfigLoader() {
        final Map<String, Configuration> envConfiguration = new EnvConfigFileConfiguration(this.configProperties).getConfiguration();
        loadEnvConfigurations(envConfiguration);
        final List<String> environments = this.configProperties.getEnvironments();
        loadVaultConfigurations(environments);
        loadKeepassConfigurations(environments);
        final String configProfile = this.configProperties.getConfigProfile();
        if (!configProfile.isEmpty()) {
            final Map<String, Configuration> profileConfiguration = new EnvConfigFileProfileConfiguration(this.configProperties).getConfiguration();
            LOG.debug("Loading config from profile {} under environments {}", configProfile, environments);
            environments.forEach(env -> this.configuration.addConfiguration(profileConfiguration.get(env)));
        }
        LOG.debug("Loading config from environment directories {}", environments);
        environments.forEach(env -> this.configuration.addConfiguration(envConfiguration.get(env)));
    }

    private void loadVaultConfigurations(final List<String> environments) {
        if (this.configProperties.isConfigVaultEnabled()) {
            final EnvConfigVaultProperties vaultProperties = this.configProperties.getVaultProperties();
            final String address = vaultProperties.getAddress();
            final String namespace = vaultProperties.getNamespace();
            LOG.debug("Loading config from vault {} namespace {}", address, namespace);
            final EnvConfigVaultConfiguration entries = new EnvConfigVaultConfiguration(vaultProperties);
            environments.forEach(env -> {
                this.configuration.addConfiguration(entries.getConfiguration(env, vaultProperties.getSecretPath()));
                vaultProperties.getDefaultPath().ifPresent(path -> this.configuration.addConfiguration(entries.getConfiguration(env, path)));
            });
        }
    }

    private void loadKeepassConfigurations(final List<String> environments) {
        if (this.configProperties.isConfigKeepassEnabled()) {
            final EnvConfigKeepassProperties keepassProperties = this.configProperties.getKeepassProperties();
            final String groupName = keepassProperties.filename();
            LOG.debug("Loading config from keepass {}", groupName);
            final EnvConfigKeepassConfiguration entries = new EnvConfigKeepassConfiguration(keepassProperties);
            environments.forEach(env -> this.configuration.addConfiguration(entries.getConfiguration(env)));
        }
    }

    private void loadEnvConfigurations(final Map<String, Configuration> configurationMap) {
        final EnvConfigSystemConfiguration variables = new EnvConfigSystemConfiguration();
        LOG.debug("Loading config from system.properties");
        this.configuration.addConfiguration(variables.getSystemConfiguration());
        final Configuration envOverrides = variables.getEnvironmentConfiguration();
        final Configuration currentEnvironment = configurationMap.get(this.configProperties.getCurrentEnvironment());
        currentEnvironment.getKeys().forEachRemaining(key -> {
            if (envOverrides.containsKey(key)
                && envOverrides.getProperty(key).equals(currentEnvironment.getString(key))) {
                envOverrides.clearProperty(key);
            }
        });
        if (!EnvConfigUtils.CONFIG_ENV_DEFAULT.equals(this.configProperties.getCurrentEnvironment())) {
            final Configuration defaultEnvironment = configurationMap.get(EnvConfigUtils.CONFIG_ENV_DEFAULT);
            defaultEnvironment.getKeys().forEachRemaining(key -> {
                if (envOverrides.containsKey(key)
                    && envOverrides.getProperty(key).equals(defaultEnvironment.getString(key))
                    && (!currentEnvironment.containsKey(key) || configurationMap.size() > ENVIRONMENTS_WITH_PARENT)) {
                    envOverrides.clearProperty(key);
                }
            });
        }
        LOG.debug("Loading config from system.env");
        this.configuration.addConfiguration(envOverrides);
    }
}
