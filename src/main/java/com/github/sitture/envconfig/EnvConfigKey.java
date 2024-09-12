package com.github.sitture.envconfig;

enum EnvConfigKey {

    CONFIG_PATH("env.config.path"),
    CONFIG_ENV("env.config.environment"),
    CONFIG_PROFILE("env.config.profile"),
    CONFIG_PROFILES_PATH("env.config.profiles.path"),
    CONFIG_KEEPASS_ENABLED("env.config.keepass.enabled"),
    CONFIG_KEEPASS_FILENAME("env.config.keepass.filename"),
    CONFIG_KEEPASS_MASTERKEY("env.config.keepass.masterkey"),
    CONFIG_VAULT_ENABLED("env.config.vault.enabled"),
    CONFIG_VAULT_ADDRESS("env.config.vault.address"),
    CONFIG_VAULT_NAMESPACE("env.config.vault.namespace"),
    CONFIG_VAULT_DEFAULT_PATH("env.config.vault.default.secret.path"),
    CONFIG_VAULT_SECRET_PATH("env.config.vault.secret.path"),
    CONFIG_VAULT_TOKEN("env.config.vault.token"),
    CONFIG_VAULT_VALIDATE_MAX_RETRIES("env.config.vault.validate.token.max.retries");

    private final String property;

    EnvConfigKey(final String property) {
        this.property = property;
    }

    public String getProperty() {
        return this.property;
    }

    public String getEnvProperty() {
        return EnvConfigUtils.getProcessedEnvKey(this.property);
    }

}
