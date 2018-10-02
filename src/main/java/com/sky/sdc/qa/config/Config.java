package com.sky.sdc.qa.config;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Config extends ConfigLoader {

	private static final Logger LOG = LoggerFactory.getLogger(Config.class);
	protected static Config config;

	private Config() {
		loadConfigurations();
	}

	/**
	 * Creates an instance of Config class or returns already created.
	 *
	 * @return instance of Config
	 */
	public static synchronized Config getConfig() {
		if (config == null) {
			config = new Config();
		}
		return config;
	}

	public static DriverConfiguration driver() {
		getConfig();
		return ConfigFactory.create(DriverConfiguration.class, System.getProperties(), EnvironmentVariables.getEnvMap());
	}

	public static BrowserConfiguration browser() {
		getConfig();
		return ConfigFactory.create(BrowserConfiguration.class, System.getProperties(), EnvironmentVariables.getEnvMap());
	}

	public static AppiumConfiguration appium() {
		getConfig();
		return ConfigFactory.create(AppiumConfiguration.class, System.getProperties(), EnvironmentVariables.getEnvMap());
	}

	/**
	 * Returns value of the property 'property' of empty string if the property
	 * is not found.
	 *
	 * @param property
	 *            Name of the property to get value of
	 * @return value of the property of empty string
	 */
	private String getProperty(final String property) {
		final String value = getConfiguration().getString(property, null);
		if (null == value || value.isEmpty()) {
			LOG.debug("Property {} was not found in properties file", property);
		}
		return null != value ? value.trim() : value;
	}

	/**
	 * Get property from file.
	 *
	 * @param property
	 *            property name.
	 * @return property value.
	 */
	public static String get(final String property) {
		return getConfig().getProperty(property);
	}

	/**
	 * Get property from file.
	 *
	 * @param property
	 *            property name.
	 * @param defaultValue
	 *            default value if not set
	 * @return property value.
	 */
	public static String get(final String property, final String defaultValue) {
		final String value = get(property);
		return null != value ? value : defaultValue;
	}

	/**
	 * Get property from file.
	 *
	 * @param property
	 *            property name.
	 * @param required
	 *            boolean if its a required property
	 * @return property value.
	 */
	public static String get(final String property, final boolean required) {
		final String value = get(property);
		if (null == value && required) {
			throw new MissingVariableException("Missing required variable '" + property + "'");
		}
		return value;
	}

	/**
	 * Get property from file.
	 *
	 * @param property
	 *            property name.
	 * @return property value.
	 */
	public static int getInteger(final String property) {
		return Integer.parseInt(get(property));
	}

	 /**
     * returns a key/value from a named config, parsed as Boolean.
     *
     * @param key the key
     * @return a Boolean representing the value, false if the value cannot be
     * parsed as Boolean
     */
	public static boolean getBool(final String property) {
		if (null == get(property)) {
			return false;
		}
		return Boolean.parseBoolean(get(property));
	}

	/**
	 * Gets environment property from Config.
	 *
	 * @return env property value.
	 */
	public static String getEnvironment() {
		return getConfig().getProperty(ENV_KEY);
	}

	private CompositeConfiguration getCompositeConfig() {
		return configuration;
	}

	/**
	 * @return the configuration object.
	 */
	private static CompositeConfiguration getConfiguration() {
		return getConfig().getCompositeConfig();
	}

	private Map<String, Object> asMap() {
		Map<String, Object> propertiesMap = new TreeMap<String, Object>();
		Iterator<String> keys = getConfiguration().getKeys();
		while (keys.hasNext()) {
			String property = keys.next();
			propertiesMap.put(property, get(property));
		}
		return propertiesMap;
	}

	public String toString() {
		return asMap().toString().replaceAll(", ", "\n");
	}

}
