package com.github.sitture.envconfig;

import org.apache.commons.configuration2.CompositeConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class EnvConfig extends EnvConfigLoader {

    private static EnvConfig config;

    private EnvConfig() {
        super();
    }

    static void reset() {
        synchronized (EnvConfig.class) {
            config = null;
        }
    }

    /**
     * Creates an instance of Config class or returns already created.
     *
     * @return instance of Config
     */
    static EnvConfig getConfig() {
        synchronized (EnvConfig.class) {
            if (config == null) {
                config = new EnvConfig();
            }
        }
        return config;
    }

    /**
     * Returns the composite configuration.
     *
     * @return the configuration object.
     */
    private static CompositeConfiguration getConfiguration() {
        return getConfig().configuration;
    }

    /**
     * Clears a property from the config.
     *
     * @param property Name of the property to clear
     */
    public static void clear(final String property) {
        getConfiguration().clearProperty(property);
    }

    /**
     * Set a property into the config with given Object value.
     *
     * @param property Key of the config entry
     * @param value    value of the config entry
     */
    public static void set(final String property, final Object value) {
        getConfiguration().setProperty(property, value);
    }

    /**
     * Adds a property into the config with given Object value.
     *
     * @param property Key of the config entry
     * @param value    value of the config entry
     */
    public static void add(final String property, final Object value) {
        getConfiguration().addProperty(property, value);
    }

    private static Optional<String> getProperty(final String property) {
        return Optional.ofNullable(getConfiguration().getString(property));
    }

    /**
     * Get property from file.
     *
     * @param property property name.
     * @return property value.
     * @throws EnvConfigException if property does not exist.
     */
    public static String getOrThrow(final String property) {
        return getProperty(property)
                .orElseThrow(() -> new EnvConfigException("Missing required variable '" + property + "'"));
    }

    /**
     * Get property from file.
     *
     * @param property property name.
     * @return property value.
     */
    public static String get(final String property) {
        return getProperty(property).map(String::trim).orElse(null);
    }

    /**
     * Get property from file.
     *
     * @param property     property name.
     * @param defaultValue default value if not set
     * @return property value.
     */
    public static String get(final String property, final String defaultValue) {
        return getProperty(property).orElse(defaultValue);
    }

    /**
     * Get property from file.
     *
     * @param property property name.
     * @return property value.
     */
    public static int getInt(final String property) {
        return getProperty(property).map(Integer::parseInt).orElse(-1);
    }

    /**
     * Returns a key/value from a named config, parsed as Boolean.
     *
     * @param property the property
     * @return a Boolean representing the value, false if the value not present or
     * cannot be parsed as Boolean
     */
    public static boolean getBool(final String property) {
        return getProperty(property).map(Boolean::parseBoolean).orElse(false);
    }

    /**
     * Returns key/value from a named config, parsed as List of Strings
     * with comma separated values.
     *
     * @param property the property
     * @return a list of strings
     */
    public static List<String> getList(final String property) {
        return getList(property, EnvConfigUtils.CONFIG_DELIMITER_DEFAULT);
    }

    /**
     * Returns key/value from a named config, parsed as List of Strings
     * with a given delimiter.
     *
     * @param property  the property
     * @param delimiter the delimiter
     * @return a list of strings
     */
    public static List<String> getList(final String property, final String delimiter) {
        return EnvConfigUtils.getListOfValues(get(property), delimiter);
    }

    /**
     * Gets environment property from Config.
     *
     * @return env property value.
     */
    public static String getEnvironment() {
        return getConfig().configProperties.getEnvironments().get(0);
    }

    public static Map<String, Object> asMap() {
        final Map<String, Object> propertiesMap = new TreeMap<>();
        getConfiguration().getKeys().forEachRemaining(key -> propertiesMap.put(key, get(key)));
        return propertiesMap;
    }

    @Override
    public String toString() {
        return asMap().toString().replaceAll(", ", "\n");
    }

}
