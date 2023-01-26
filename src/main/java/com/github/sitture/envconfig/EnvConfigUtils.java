package com.github.sitture.envconfig;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class EnvConfigUtils {

    public static final String CONFIG_PATH_DEFAULT = "config";
    public static final String CONFIG_ENV_DEFAULT = "default";
    public static final String CONFIG_DELIMITER_DEFAULT = ",";
    private static final String CONFIG_PREFIX = "env.config.";
    public static final String CONFIG_PATH_KEY = CONFIG_PREFIX + "path";
    public static final String CONFIG_ENV_KEY = CONFIG_PREFIX + "environment";
    public static final String CONFIG_PROFILE_KEY = CONFIG_PREFIX + "profile";
    public static final String CONFIG_PROFILES_PATH_KEY = CONFIG_PREFIX + "profiles.path";
    public static final String CONFIG_KEEPASS_ENABLED_KEY = CONFIG_PREFIX + "keepass.enabled";
    public static final String CONFIG_KEEPASS_FILENAME_KEY = CONFIG_PREFIX + "keepass.filename";
    public static final String CONFIG_KEEPASS_MASTERKEY_KEY = CONFIG_PREFIX + "keepass.masterkey";

    private EnvConfigUtils() {
    }

    static String getProcessedPropertyKey(final String envVar) {
        return envVar.replaceAll("_", ".").toLowerCase();
    }

    static String getProcessedEnvKey(final String property) {
        return property.replaceAll("\\.", "_").toUpperCase();
    }

    static List<String> getListOfValues(final String value, final String delimiter) {
        return null == value
                ? Collections.emptyList()
                : Stream.of(value.split(delimiter))
                .map(String::trim)
                .collect(Collectors.toList());
    }

}
