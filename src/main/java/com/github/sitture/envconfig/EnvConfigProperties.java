package com.github.sitture.envconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class EnvConfigProperties {

    private static final Logger LOG = LoggerFactory.getLogger(EnvConfigProperties.class);
    private final List<String> environments;
    private final Path configDir;
    private final Path configProfilesPath;

    EnvConfigProperties() {
        configDir = getConfigPath();
        configProfilesPath = getConfigProfilePath();
        environments = getEnvList();
    }

    String getBuildDir() {
        return Paths.get(Optional.ofNullable(System.getProperty("project.build.directory"))
                .orElse(System.getProperty("user.dir"))).toString();
    }

    List<String> getEnvironments() {
        return environments;
    }

    String getConfigProfile() {
        return getConfigurationProperty(EnvConfigUtils.CONFIG_PROFILE_KEY, "");
    }

    private List<String> getEnvList() {
        final List<String> environments = new ArrayList<>();
        environments.add(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        environments.addAll(EnvConfigUtils.getListOfValues(getConfigurationProperty(EnvConfigUtils.CONFIG_ENV_KEY, EnvConfigUtils.CONFIG_ENV_DEFAULT), EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
        Collections.reverse(environments);
        return environments.stream().distinct().collect(Collectors.toList());
    }

    Path getConfigPath(final String env) {
        return Paths.get(this.configDir.toString(), env);
    }

    private Path getConfigPath() {
        return getPath(Paths.get(getConfigurationProperty(EnvConfigUtils.CONFIG_PATH_KEY, EnvConfigUtils.CONFIG_PATH_DEFAULT)).toAbsolutePath());
    }

    Path getConfigProfilePath(final String env, final String configProfile) {
        return Paths.get(this.configProfilesPath.toString(), env, configProfile);
    }

    private Path getConfigProfilePath() {
        return getPath(Paths.get(getConfigurationProperty(EnvConfigUtils.CONFIG_PROFILES_PATH_KEY, this.configDir.toString())).toAbsolutePath());
    }

    private Path getPath(final Path configPath) {
        final File configDir = configPath.toFile();
        if (!configDir.exists() || !configDir.isDirectory()) {
            throw new EnvConfigException(
                    "'" + configPath + "' does not exist or not a valid config directory!");
        }
        return configPath;
    }

    private String getConfigurationProperty(final String key, final String defaultValue) {
        return Optional.ofNullable(System.getProperty(key))
                .orElse(Optional.ofNullable(getEnvByPropertyKey(key))
                        .orElse(defaultValue));
    }

    private String getEnvByPropertyKey(final String key) {
        LOG.debug("Getting {} from system.env", key);
        return Optional.ofNullable(System.getenv(EnvConfigUtils.getProcessedEnvKey(key)))
                .orElse(System.getenv(key));
    }

    String getConfigKeePassFilename() {
        return new File(getConfigurationProperty(EnvConfigUtils.CONFIG_KEEPASS_FILENAME_KEY, getBuildDir())).getName();
    }

    boolean isConfigKeePassEnabled() {
        return Boolean.parseBoolean(getConfigurationProperty(EnvConfigUtils.CONFIG_KEEPASS_ENABLED_KEY, "false"));
    }

    String getConfigKeePassMasterKey() {
        final String key = EnvConfigUtils.CONFIG_KEEPASS_MASTERKEY_KEY;
        return Optional.ofNullable(getConfigurationProperty(key, null))
                .orElseThrow(() -> new EnvConfigException(String.format("Missing required variable '%s'", key)));
    }
}
