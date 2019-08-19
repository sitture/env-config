package com.github.sitture.env.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class ConfigLoader {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
	protected static final String CONFIG_ENV_KEY = "config.env";
	protected static final String DEFAULT_ENVIRONMENT = "default";
	private static final String CONFIG_DIR = "config.dir";
	private static final String DEFAULT_ENV_DIRECTORY = "env";
	protected static CompositeConfiguration configuration;

	private String getProperty(final String key, final String defaultValue) {
		String value = System.getenv(key.replace(".", "_").toUpperCase());
		if (null != value) {
			setProperty(key, value);
			return value;
		}
		value = System.getProperty(key, defaultValue);
		setProperty(key, value);
		return value;
	}

	protected String getEnv() {
		return getProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT);
	}

	private void setProperty(final String key, final String value) {
		System.setProperty(key, value);
		LOG.debug(key + " set to '" + value + "'");
	}

	private String getConfigDir() {
		return getProperty(CONFIG_DIR, DEFAULT_ENV_DIRECTORY);
	}

	private String getBuildDir() {
		final String workingDirectory = System.getProperty("user.dir");
		final String buildDir = System.getProperty("project.build.directory", workingDirectory);
		return buildDir;
	}

	private String getConfigPath(final String env) {
		final String defaultConfig = getBuildDir() + File.separator + getConfigDir() + File.separator + env;
		return defaultConfig;
	}

	private List<File> getConfigFiles(final String configPath) {
		File configDir = new File(configPath);
		if (!configDir.exists() || !configDir.isDirectory()) {
			throw new ConfigException(
					"'" + configPath + "' does not exist or not a valid config directory!");
		}
		return getConfigProperties(configDir.listFiles(), configPath);
	}

	private List<File> getConfigProperties(final File[] configFiles, final String configPath) {
		if (configFiles.length == 0) {
			throw new ConfigException("No property files found under '" + configPath + "'");
		}
		return getFilteredPropertiesFiles(configFiles);
	}

	private List<File> getFilteredPropertiesFiles(File[] configFiles) {
		List<File> filteredFiles = new ArrayList<File>();
		for (File file : configFiles) {
			if (isValidPropertiesFile(file)) {
				filteredFiles.add(file);
			}
		}
		return filteredFiles;
	}

	private boolean isValidPropertiesFile(final File file) {
		return file.getName().endsWith(".properties");
	}

	protected void loadConfigurations() {
		configuration = new CompositeConfiguration();
		loadEnvConfigurations();
		loadKeePassConfigurations();
		final String env = getEnv();
		loadFileConfigurations(getConfigPath(env));
		if (!env.equals(DEFAULT_ENVIRONMENT)) {
			loadFileConfigurations(getConfigPath(DEFAULT_ENVIRONMENT));
		}
	}

	private void loadKeePassConfigurations() {
		final KeePassEntries keepassEntries = new KeePassEntries();
		configuration.addConfiguration(keepassEntries.getEntriesConfiguration("europa-e2e", getEnv()));
	}

	private void loadEnvConfigurations() {
		final EnvironmentVariables envVars = new EnvironmentVariables();
		configuration.addConfiguration(envVars.getEnvironmentConfiguration());
		configuration.addConfiguration(envVars.getSystemConfiguration());
	}

	private void loadFileConfigurations(final String configPath) {
		try {
			for (final File file : getConfigFiles(configPath)) {
				configuration.addConfiguration(new Configurations().properties(file));
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

}
