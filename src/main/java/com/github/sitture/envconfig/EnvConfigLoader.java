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
	private static final int MIN_ENVIRONMENTS = 1;
	protected final CompositeConfiguration configuration = new CompositeConfiguration();
	protected final EnvConfigProperties configProperties = new EnvConfigProperties();

	EnvConfigLoader() {
		final List<String> environments = configProperties.getEnvironments();
		final String configProfile = configProperties.getConfigProfile();
		loadKeepassConfigurations(environments);
		loadEnvConfigurations(environments);
		if (!configProfile.isEmpty()) {
			LOG.debug("Loading properties from profile {} under environments {}", configProfile, environments);
			environments.forEach(env -> loadFileConfigurations(new EnvConfigProfileFileList(configProperties.getConfigProfilePath(env, configProfile))));
		}
		LOG.debug("Loading properties from environment directories {}", environments);
		environments.forEach(env -> loadFileConfigurations(new EnvConfigFileList(configProperties.getConfigPath(env))));
	}

	private void loadKeepassConfigurations(final List<String> environments) {
		if (configProperties.isConfigKeePassEnabled()) {
			final String groupName = configProperties.getConfigKeePassFilename();
			final String masterKey = configProperties.getConfigKeePassMasterKey();
			LOG.debug("Loading properties from keepass {}", groupName);
			final KeePassEntries keepassEntries = new KeePassEntries(masterKey, groupName);
			environments.forEach(env -> this.configuration.addConfiguration(keepassEntries.getEntriesConfiguration(env)));
		}
	}

	private void loadEnvConfigurations(final List<String> envs) {
		final EnvironmentVariables envVars = new EnvironmentVariables();
		LOG.debug("Loading properties from system.properties");
		this.configuration.addConfiguration(envVars.getSystemConfiguration());
		final Configuration envOverrides = envVars.getEnvironmentConfiguration();
		if (envs.size() > MIN_ENVIRONMENTS) {
			envs.stream().filter(env -> !env.equals(EnvConfigUtils.CONFIG_ENV_DEFAULT)).forEach(env -> processEnvOverrides(envOverrides, env));
		}
		LOG.debug("Loading properties from system.env");
		this.configuration.addConfiguration(envOverrides);
	}

	private void processEnvOverrides(final Configuration envOverrides, final String env) {
		try {
			for (final File file : new EnvConfigFileList(configProperties.getConfigPath(env)).listFiles()) {
				final Configuration properties = new Configurations().properties(file);
				properties.getKeys().forEachRemaining(property -> {
					if (envOverrides.containsKey(property) && envOverrides.getProperty(property).equals(properties.getProperty(property))) {
						envOverrides.clearProperty(property);
					}
				});
			}
		} catch (ConfigurationException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not load configuration files. \n {}", e.getMessage());
			}
		}
	}

	private void loadFileConfigurations(final EnvConfigFileList fileList) {
		if (fileList.listFiles().isEmpty()) {
			LOG.debug("No property files found under {}", fileList.configPath);
		}
		try {
			for (final File file : fileList.listFiles()) {
				final Configuration config = new Configurations().properties(file);
				final Map<String, Object> propertiesMap = new HashMap<>();
				config.getKeys().forEachRemaining(key -> {
					final Object value = config.getProperty(key);
					propertiesMap.put(key, value);
					propertiesMap.put(EnvConfigUtils.getProcessedEnvKey(key), value);
				});
				LOG.debug("Loading properties from {}", file);
				this.configuration.addConfiguration(new MapConfiguration(propertiesMap));
			}
		} catch (ConfigurationException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not load configuration files. \n {}", e.getMessage());
			}
		}
	}

}
