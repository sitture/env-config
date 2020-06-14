package com.github.sitture.env.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConfigLoader {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
	protected static final String CONFIG_ENV_KEY = "config.env";
	private static final String DEFAULT_ENVIRONMENT = "default";
	private static final String CONFIG_DIR_KEY = "config.dir";
	private static final String DEFAULT_ENV_DIRECTORY = "config";
	private static final String CONFIG_KEEPASS_FILENAME_KEY = "config.keepass.filename";
	private static final String CONFIG_KEEPASS_ENABLED_KEY = "config.keepass.enabled";
	private static final String CONFIG_KEEPASS_MASTERKEY_KEY = "config.keepass.masterkey";
	protected static CompositeConfiguration configuration;

	private String getEnvByPropertyKey(final String key) {
		String value = System.getenv(key.replace(".", "_").toUpperCase());
		if (null == value) {
			value = System.getenv(key);
		}
		return value;
	}

	private String getProperty(final String key, final String defaultValue) {
		final String value = null != getEnvByPropertyKey(key) ? getEnvByPropertyKey(key) : System.getProperty(key, defaultValue);
		setProperty(key, value);
		return value;
	}

	private String getEnv() {
		return getProperty(CONFIG_ENV_KEY, DEFAULT_ENVIRONMENT);
	}

	private void setProperty(final String key, final String value) {
		System.setProperty(key, value);
		LOG.debug("{} set to '{}'", key, value);
	}

	private String getConfigDir() {
		return getProperty(CONFIG_DIR_KEY, DEFAULT_ENV_DIRECTORY);
	}

	private String getBuildDir() {
		final String workingDirectory = System.getProperty("user.dir");
		return System.getProperty("project.build.directory", workingDirectory);
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
		final String value = null != getEnvByPropertyKey(CONFIG_KEEPASS_MASTERKEY_KEY)
				? getEnvByPropertyKey(CONFIG_KEEPASS_MASTERKEY_KEY)
				: System.getProperty(CONFIG_KEEPASS_MASTERKEY_KEY);
		if (null == value) {
			throw new MissingVariableException(
					String.format("Missing required variable '%s'", CONFIG_KEEPASS_MASTERKEY_KEY));
		}
		setProperty(CONFIG_KEEPASS_MASTERKEY_KEY, value);
		return value;
	}

	private String getConfigPath(final String env) {
		return getBuildDir() + File.separator + getConfigDir() + File.separator + env;
	}

	private List<File> getConfigFiles(final String configPath) {
		final File configDir = new File(configPath);
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

	private List<File> getFilteredPropertiesFiles(final File[] configFiles) {
		final List<File> filteredFiles = new ArrayList<>();
		for (final File file : configFiles) {
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
		final String env = getEnv();
		final String groupName = getConfigKeePassFilename();
        if (isConfigKeePassEnabled()) {
			loadKeePassConfigurations(groupName, env);
			if (!env.equals(DEFAULT_ENVIRONMENT)) {
				loadKeePassConfigurations(groupName, DEFAULT_ENVIRONMENT);
			}
		}
		loadEnvConfigurations();
		loadFileConfigurations(getConfigPath(env));
		if (!env.equals(DEFAULT_ENVIRONMENT)) {
			loadFileConfigurations(getConfigPath(DEFAULT_ENVIRONMENT));
		}
	}

	private void loadKeePassConfigurations(final String groupName, final String env) {
		LOG.debug(String.format("Loading keePass entries for %s/%s.", groupName, env));
		final String masterKey = getConfigKeePassMasterKey();
		final KeePassEntries keepassEntries = new KeePassEntries(masterKey, groupName, env);
		configuration.addConfiguration(keepassEntries.getEntriesConfiguration());
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
			LOG.debug("Could not load configuration files. \n %s", e.getMessage());
		}
	}

}
