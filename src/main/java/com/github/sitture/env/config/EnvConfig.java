package com.github.sitture.env.config;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EnvConfig extends ConfigLoader {

	private static final Logger LOG = LoggerFactory.getLogger(EnvConfig.class);
	private static EnvConfig config;

	private EnvConfig() {
		loadConfigurations();
	}

	/**
	 * Creates an instance of Config class or returns already created.
	 *
	 * @return instance of Config
	 */
	public static EnvConfig getConfig() {
		synchronized(EnvConfig.class) {
			if (config == null) {
				config = new EnvConfig();
			}
		}
		return config;
	}

	protected static void reset() {
		synchronized(EnvConfig.class) {
			config = null;
		}
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
	 * Adds a property to the config.
	 *
	 * @param property
	 *            Name of the property to add value of
	 * @param value
	 *            Value to be assigned to the property
	 */
	private void addProperty(final String property, final Object value) {
		getConfiguration().addProperty(property, value);
	}

	/**
	 * Sets an existing property to given new value.
	 * @param property
	 *            Name of the property to set value of
	 * @param value
	 *            Value to be assigned to the existing property
	 */
	private void setProperty(final String property, final Object value) {
		getConfiguration().setProperty(property, value);
	}

	/**
	* Clears an existing property.
	*
	* @param property
	* 					Name of the property to clear
	*/
	private void clearProperty(final String property) {
		getConfiguration().clearProperty(property);
	}

	/**
	* Clears a property from the config.
	*
	* @param property
	* 					Name of the property to clear
	*/
	public static void clear(final String property) {
		getConfig().clearProperty(property);
	}

	/**
	 * Set a property into the config with given Object value.
	 * @param property
	 *            Key of the config entry
	 * @param value
	 *            value of the config entry
	 */
	public static void set(final String property, final Object value) {
		getConfig().setProperty(property, value);
	}

	/**
	 * Adds a property into the config with given Object value.
	 * @param property
	 *            Key of the config entry
	 * @param value
	 *            value of the config entry
	 */
	public static void add(final String property, final Object value) {
		getConfig().addProperty(property, value);
	}

	/**
	 * Get property from file.
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
	 * @return property value.
	 * @throws MissingVariableException if property does not exist.
	 */
	public static String getOrThrow(final String property) {
		final String value = get(property);
		if (null == value) {
			throw new MissingVariableException(
					"Missing required variable '" + property + "'");
		}
		return value;
	}

	/**
	 * Get property from file.
	 *
	 * @param property
	 *            - property name.
	 * @return property value.
	 */
	public static int getInteger(final String property) {
		return Integer.parseInt(get(property));
	}

	/**
	 * Returns a key/value from a named config, parsed as Boolean.
	 *
	 * @param property
	 *            the property
	 * @return a Boolean representing the value, false if the value cannot be
	 *         parsed as Boolean
	 */
	public static boolean getBool(final String property) {
		return null != get(property) && Boolean.parseBoolean(get(property));
	}

	/**
	 * Returns key/value from a named config, parsed as List<String>
	 *     with comma separated values.
	 *
	 * @param property
	 * @return a list of strings
	 */
	public static List<String> getList(final String property) {
		return getList(property, ",");
	}

	/**
	 * Returns key/value from a named config, parsed as List<String>
	 *     with a given delimiter.
	 *
	 * @param property
	 * @param delimiter
	 * @return a list of strings
	 */
	public static List<String> getList(final String property, final String delimiter) {
		final String value = get(property);
		return null == value
				? Collections.emptyList()
				: Stream.of(value.split(delimiter))
				.map(String::trim)
				.collect(Collectors.toList());
	}

	/**
	 * Gets environment property from Config.
	 *
	 * @return env property value.
	 */
	public static String getEnvironment() {
		return getConfig().getProperty(CONFIG_ENV_KEY);
	}

	private CompositeConfiguration getCompositeConfig() {
		return configuration;
	}

	/**
	 * Returns the composite configuration.
	 *
	 * @return the configuration object.
	 */
	private static CompositeConfiguration getConfiguration() {
		return getConfig().getCompositeConfig();
	}

	private Map<String, Object> asMap() {
		final Map<String, Object> propertiesMap = new TreeMap<>();
		final Iterator<String> keys = getConfiguration().getKeys();
		while (keys.hasNext()) {
			final String property = keys.next();
			propertiesMap.put(property, get(property));
		}
		return propertiesMap;
	}

	@Override
	public String toString() {
		return asMap().toString().replaceAll(", ", "\n");
	}

}
