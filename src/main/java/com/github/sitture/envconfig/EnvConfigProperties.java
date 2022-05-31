package com.github.sitture.envconfig;

final class EnvConfigProperties {

    public static final String CONFIG_PATH_DEFAULT = "config";
    public static final String CONFIG_ENV_DEFAULT = "default";
    public static final String CONFIG_DELIMITER_DEFAULT = ",";
    private static final String CONFIG_PREFIX = "env.config.";
    public static final String CONFIG_PATH_KEY = CONFIG_PREFIX + "path";
    public static final String CONFIG_ENV_KEY = CONFIG_PREFIX + "environment";
    public static final String CONFIG_ENV_PROFILE_KEY = CONFIG_PREFIX + "profile";
    public static final String CONFIG_KEEPASS_ENABLED_KEY = CONFIG_PREFIX + "keepass.enabled";
    public static final String CONFIG_KEEPASS_FILENAME_KEY = CONFIG_PREFIX + "keepass.filename";
    public static final String CONFIG_KEEPASS_MASTERKEY_KEY = CONFIG_PREFIX + "keepass.masterkey";

    private EnvConfigProperties() {
    }

}
