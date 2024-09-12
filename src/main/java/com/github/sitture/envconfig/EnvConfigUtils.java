package com.github.sitture.envconfig;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class EnvConfigUtils {

    public static final String CONFIG_PATH_DEFAULT = "config";
    public static final String CONFIG_ENV_DEFAULT = "default";
    public static final String CONFIG_DELIMITER_DEFAULT = ",";

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
