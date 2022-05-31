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
	protected final CompositeConfiguration configuration;

	EnvConfigLoader() {
		configuration = new CompositeConfiguration();
		final List<String> envs = EnvConfigUtils.getEnvList();
		final String configProfile = EnvConfigUtils.getEnvProfile();
		if (EnvConfigUtils.isConfigKeePassEnabled()) {
			final String groupName = EnvConfigUtils.getConfigKeePassFilename();
			final String masterKey = EnvConfigUtils.getConfigKeePassMasterKey();
			final KeePassEntries keepassEntries = new KeePassEntries(masterKey, groupName);
			envs.forEach(env -> configuration.addConfiguration(keepassEntries.getEntriesConfiguration(env)));
		}
		loadEnvConfigurations(envs);
		if (!configProfile.isEmpty()) {
			envs.forEach(env -> loadFileConfigurations(new EnvConfigProfileFileList(env, configProfile)));
		}
		envs.forEach(env -> loadFileConfigurations(new EnvConfigFileList(env)));
	}

	private void loadEnvConfigurations(final List<String> envs) {
		final EnvironmentVariables envVars = new EnvironmentVariables();
		final Configuration envOverrides = envVars.getEnvironmentConfiguration();
		final EnvConfigFileList fileList = new EnvConfigFileList(EnvConfigProperties.CONFIG_ENV_DEFAULT);
		try {
			for (final File file : fileList.listFiles()) {
				final Configuration defaultConfig = new Configurations().properties(file);
				defaultConfig.getKeys().forEachRemaining(property -> {
					if (envs.size() > 2 && envOverrides.containsKey(property)
							&& envOverrides.getProperty(property).equals(defaultConfig.getProperty(property))) {
						envOverrides.clearProperty(property);
					}
				});
			}
		} catch (ConfigurationException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not load configuration files. \n {}", e.getMessage());
			}
		}
		configuration.addConfiguration(envOverrides);
		configuration.addConfiguration(envVars.getSystemConfiguration());
	}

	private void loadFileConfigurations(final EnvConfigFileList fileList) {
		try {
			for (final File file : fileList.listFiles()) {
				final Configuration config = new Configurations().properties(file);
				final Map<String, Object> propertiesMap = new HashMap<>();
				config.getKeys().forEachRemaining(key -> {
					final Object value = config.getProperty(key);
					propertiesMap.put(key, value);
					propertiesMap.put(EnvConfigUtils.getProcessedEnvKey(key), value);
				});
				configuration.addConfiguration(new MapConfiguration(propertiesMap));
			}
		} catch (ConfigurationException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not load configuration files. \n {}", e.getMessage());
			}
		}
	}

}
