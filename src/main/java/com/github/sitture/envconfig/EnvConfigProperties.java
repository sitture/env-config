package com.github.sitture.envconfig;

import static com.github.sitture.envconfig.EnvConfigUtils.getConfigProperty;
import static com.github.sitture.envconfig.EnvConfigUtils.getRequiredConfigProperty;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class EnvConfigProperties {

    private final List<String> environments;
    private final Path configDir;
    private final Path configProfilesPath;

    EnvConfigProperties() {
        configDir = getConfigPath();
        configProfilesPath = getConfigProfilePath();
        environments = getEnvList();
    }

    String getBuildDir() {
        return Path.of(Optional.ofNullable(System.getProperty("project.build.directory"))
            .orElse(System.getProperty("user.dir"))).toString();
    }

    List<String> getEnvironments() {
        return environments;
    }

    String getCurrentEnvironment() {
        return environments.get(0);
    }

    String getConfigProfile() {
        return getConfigProperty(EnvConfigKey.CONFIG_PROFILE, "");
    }

    private List<String> getEnvList() {
        final List<String> environmentsList = new ArrayList<>();
        environmentsList.add(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        environmentsList.addAll(EnvConfigUtils.getListOfValues(getConfigProperty(EnvConfigKey.CONFIG_ENV, EnvConfigUtils.CONFIG_ENV_DEFAULT).toLowerCase(), EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
        Collections.reverse(environmentsList);
        return environmentsList.stream().distinct().toList();
    }

    Path getConfigPath(final String env) {
        return Path.of(this.configDir.toString(), env);
    }

    private Path getConfigPath() {
        return getPath(Path.of(getConfigProperty(EnvConfigKey.CONFIG_PATH, EnvConfigUtils.CONFIG_PATH_DEFAULT)).toAbsolutePath());
    }

    Path getConfigProfilePath(final String env, final String configProfile) {
        return Path.of(this.configProfilesPath.toString(), env, configProfile);
    }

    Path getConfigProfilePath(final String env) {
        return getConfigProfilePath(env, getConfigProfile());
    }

    private Path getConfigProfilePath() {
        return getPath(Path.of(getConfigProperty(EnvConfigKey.CONFIG_PROFILES_PATH, this.configDir.toString())).toAbsolutePath());
    }

    private Path getPath(final Path configPath) {
        final File configDirFile = configPath.toFile();
        if (!configDirFile.exists() || !configDirFile.isDirectory()) {
            throw new EnvConfigException(
                "'" + configPath + "' does not exist or not a valid config directory!");
        }
        return configPath;
    }

    boolean isConfigKeepassEnabled() {
        return Boolean.parseBoolean(getConfigProperty(EnvConfigKey.CONFIG_KEEPASS_ENABLED, "false"));
    }

    EnvConfigKeepassProperties getKeepassProperties() {
        return new EnvConfigKeepassProperties(new File(getConfigProperty(EnvConfigKey.CONFIG_KEEPASS_FILENAME, getBuildDir())).getName(),
            getRequiredConfigProperty(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY));
    }

    boolean isConfigVaultEnabled() {
        return Boolean.parseBoolean(getConfigProperty(EnvConfigKey.CONFIG_VAULT_ENABLED, "false"));
    }

    EnvConfigVaultProperties getVaultProperties() {
        return new EnvConfigVaultProperties(getRequiredConfigProperty(EnvConfigKey.CONFIG_VAULT_ADDRESS),
            getRequiredConfigProperty(EnvConfigKey.CONFIG_VAULT_NAMESPACE),
            getRequiredConfigProperty(EnvConfigKey.CONFIG_VAULT_TOKEN),
            getRequiredConfigProperty(EnvConfigKey.CONFIG_VAULT_SECRET_PATH),
            getConfigProperty(EnvConfigKey.CONFIG_VAULT_DEFAULT_PATH, null),
            Integer.parseInt(getConfigProperty(EnvConfigKey.CONFIG_VAULT_VALIDATE_MAX_RETRIES, "5")));
    }
}
