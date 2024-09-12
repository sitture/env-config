package com.github.sitture.envconfig;

import java.util.Optional;

class EnvConfigVaultProperties {

    private final String address;
    private final String namespace;
    private final String token;
    private final String defaultPath;
    private final String secretPath;
    private final int validateTokenMaxRetries;

    EnvConfigVaultProperties(final String address, final String namespace, final String token, final String secretPath, final String defaultPath, final int validateTokenMaxRetries) {
        this.address = address;
        this.namespace = namespace;
        this.token = token;
        this.defaultPath = defaultPath;
        this.secretPath = secretPath;
        this.validateTokenMaxRetries = validateTokenMaxRetries;
    }

    String getAddress() {
        return address;
    }

    String getNamespace() {
        return namespace;
    }

    String getToken() {
        return token;
    }

    Optional<String> getDefaultPath() {
        return Optional.ofNullable(defaultPath);
    }

    String getSecretPath() {
        return secretPath;
    }

    int getValidateTokenMaxRetries() {
        return validateTokenMaxRetries;
    }
}
