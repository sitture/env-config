package com.github.sitture.envconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class EnvConfigUtils {

    private static final Logger LOG = LoggerFactory.getLogger(EnvConfigUtils.class);

    private EnvConfigUtils() {
    }

    public static String getConfigPath(final String env) {
        return Paths.get(getConfigDir().toString(), env).toString();
    }

    private static Path getConfigDir() {
        return Paths.get(getProperty(EnvConfigProperties.CONFIG_PATH_KEY, EnvConfigProperties.CONFIG_PATH_DEFAULT)).toAbsolutePath();
    }

    private static String getProperty(final String key, final String defaultValue) {
        final String value = Optional.ofNullable(getEnvByPropertyKey(key)).orElse(System.getProperty(key, defaultValue));
        if (null != value) {
            System.setProperty(key, value);
            LOG.debug("{} system property set to '{}'", key, value);
        }
        return value;
    }

    private static String getEnvByPropertyKey(final String key) {
        LOG.debug("Getting {} from environment variables", key);
        return Optional.ofNullable(System.getenv(key.replace(".", "_").toUpperCase()))
                .orElse(System.getenv(key));
    }

    public static String getConfigKeePassFilename() {
        return new File(getProperty(EnvConfigProperties.CONFIG_KEEPASS_FILENAME_KEY, getBuildDir())).getName();
    }

    public static String getBuildDir() {
        final String workingDirectory = System.getProperty("user.dir");
        return Paths.get(System.getProperty("project.build.directory", workingDirectory)).toString();
    }

    public static List<String> getEnvList() {
        final String value = String.format("%s%s%s",
                EnvConfigProperties.CONFIG_ENV_DEFAULT, EnvConfigProperties.CONFIG_DELIMITER_DEFAULT,
                getProperty(EnvConfigProperties.CONFIG_ENV_KEY, EnvConfigProperties.CONFIG_ENV_DEFAULT));
        return Stream.of(value.split(EnvConfigProperties.CONFIG_DELIMITER_DEFAULT))
                .sorted(Collections.reverseOrder())
                .distinct()
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static String getEnvProfile() {
        return getProperty(EnvConfigProperties.CONFIG_ENV_PROFILE_KEY, "");
    }

    public static boolean isConfigKeePassEnabled() {
        return Boolean.parseBoolean(getProperty(EnvConfigProperties.CONFIG_KEEPASS_ENABLED_KEY, "false"));
    }

    public static String getConfigKeePassMasterKey() {
        final String key = EnvConfigProperties.CONFIG_KEEPASS_MASTERKEY_KEY;
        return Optional.ofNullable(getProperty(key, null))
                .orElseThrow(() -> new EnvConfigException(String.format("Missing required variable '%s'", key)));
    }

    public static String getProcessedEnvKey(final String envVar) {
        return envVar.replaceAll("_", ".").toLowerCase();
    }

    public static List<String> getListOfValues(final String value, final String delimiter) {
        return null == value
                ? Collections.emptyList()
                : Stream.of(value.split(delimiter))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
