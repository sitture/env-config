package com.github.sitture.env.config.utils;

import com.github.sitture.env.config.MissingVariableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropertyUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyUtils.class);

    private PropertyUtils() {

    }

    public static String getProcessedEnvKey(final String envVar) {
        return envVar.replaceAll("_", ".").toLowerCase();
    }

    public static String getProperty(final String key, final String defaultValue) {
        final String value = getEnvOrSystemProperty(key, defaultValue);
        setProperty(key, value);
        return value;
    }

    public static String getRequiredProperty(final String key) {
        final String value = getEnvOrSystemProperty(key, null);
        if (null == value) {
            throw new MissingVariableException(
                    String.format("Missing required variable '%s'", key));
        }
        setProperty(key, value);
        return value;
    }

    private static String getEnvOrSystemProperty(final String key, final String defaultValue) {
        return null != getEnvByPropertyKey(key) ? getEnvByPropertyKey(key) : System.getProperty(key, defaultValue);
    }

    private static void setProperty(final String key, final String value) {
        System.setProperty(key, value);
        LOG.debug("{} set to '{}'", key, value);
    }

    private static String getEnvByPropertyKey(final String key) {
        String value = System.getenv(key.replace(".", "_").toUpperCase());
        if (null == value) {
            value = System.getenv(key);
        }
        return value;
    }
}
