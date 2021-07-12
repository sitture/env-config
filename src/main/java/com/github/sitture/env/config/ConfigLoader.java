package com.github.sitture.env.config;

import com.github.sitture.env.config.filter.ConfigFileList;
import com.github.sitture.env.config.filter.ConfigProfileFileList;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.sitture.env.config.utils.BuildDirUtils.getBuildDir;
import static com.github.sitture.env.config.utils.PropertyUtils.getProperty;
import static com.github.sitture.env.config.utils.PropertyUtils.getRequiredProperty;

class ConfigLoader {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);

	protected static final String CONFIG_ENV_KEY = "config.env";
	protected static final String CONFIG_ENV_PROFILE_KEY = "config.env.profile";
	protected static final String DEFAULT_ENVIRONMENT = "default";
	protected static final String DEFAULT_DELIMITER = ",";
	private static final String CONFIG_KEEPASS_FILENAME_KEY = "config.keepass.filename";
	private static final String CONFIG_KEEPASS_ENABLED_KEY = "config.keepass.enabled";
	private static final String CONFIG_KEEPASS_MASTER_KEY_KEY = "config.keepass.masterkey";
	protected static CompositeConfiguration configuration;

	private List<String> getEnvList() {
		final String value = String.format("%s%s%s",
				DEFAULT_ENVIRONMENT, DEFAULT_DELIMITER,
				getProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT));
		return Stream.of(value.split(DEFAULT_DELIMITER))
				.sorted(Collections.reverseOrder())
				.distinct()
				.map(String::trim)
				.collect(Collectors.toList());
	}

	private String getEnvProfile() {
		return getProperty(CONFIG_ENV_PROFILE_KEY, "");
	}

	private boolean isConfigKeePassEnabled() {
		return Boolean.parseBoolean(getProperty(CONFIG_KEEPASS_ENABLED_KEY, "false"));
	}

	private String getConfigKeePassFilename() {
		final String[] buildDir = getBuildDir().split(File.separator);
		final String defaultFileName = buildDir[buildDir.length-1];
		return getProperty(CONFIG_KEEPASS_FILENAME_KEY, defaultFileName);
	}

	private String getConfigKeePassMasterKey() {
		return getRequiredProperty(CONFIG_KEEPASS_MASTER_KEY_KEY);
	}

	protected void loadConfigurations() {
		configuration = new CompositeConfiguration();
		final List<String> envs = getEnvList();
		final String configProfile = getEnvProfile();
		final String groupName = getConfigKeePassFilename();
		if (isConfigKeePassEnabled()) {
			envs.forEach(env -> loadKeePassConfigurations(groupName, env));
		}
		loadEnvConfigurations();
		if (!configProfile.isEmpty()) {
			envs.forEach(env -> loadFileConfigurations(new ConfigProfileFileList(env, configProfile)));
		}
		envs.forEach(env -> loadFileConfigurations(new ConfigFileList(env)));
	}

	private void loadKeePassConfigurations(final String groupName, final String env) {
		LOG.debug(String.format("Loading keePass entries for %s/%s.", groupName, env));
		final String masterKey = getConfigKeePassMasterKey();
		final KeePassEntries keepassEntries = new KeePassEntries(masterKey, groupName, env);
		configuration.addConfiguration(keepassEntries.getEntriesConfiguration());
	}

	private void loadEnvConfigurations() {
		final EnvironmentVariables envVars = new EnvironmentVariables();
		final Configuration envOverrides = envVars.getEnvironmentConfiguration();
		final ConfigFileList cfl = new ConfigFileList(DEFAULT_ENVIRONMENT);
		try {
			for (final File file : cfl.listFiles()) {
				final Configuration defaults = new Configurations().properties(file);
				final Iterator<String> keys = defaults.getKeys();
				while (keys.hasNext()) {
					final String property = keys.next();
					if (envOverrides.containsKey(property)
							&& envOverrides.getProperty(property).equals(defaults.getProperty(property))) {
						envOverrides.clearProperty(property);
					}
				}
			}
		} catch (ConfigurationException e) {
			LOG.debug("Could not load configuration files. \n {}", e.getMessage());
		}

		configuration.addConfiguration(envOverrides);
		configuration.addConfiguration(envVars.getSystemConfiguration());
	}

	private void loadFileConfigurations(final ConfigFileList configFileList) {
		try {
			for (final File file : configFileList.listFiles()) {
				configuration.addConfiguration(new Configurations().properties(file));
			}
		} catch (ConfigurationException e) {
			LOG.debug("Could not load configuration files. \n {}", e.getMessage());
		}
	}

}
