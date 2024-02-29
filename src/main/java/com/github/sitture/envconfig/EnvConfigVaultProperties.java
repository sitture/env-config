package com.github.sitture.envconfig;

class EnvConfigVaultProperties {

    private final String address;
    private final String namespace;
    private final String token;
    private final String secretPath;
    private final int validateTokenMaxRetries;

    EnvConfigVaultProperties(final String address, final String namespace, final String token, final String secretPath, final int validateTokenMaxRetries) {
        this.address = address;
        this.namespace = namespace;
        this.token = token;
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

    String getSecretPath() {
        return secretPath;
    }

    int getValidateTokenMaxRetries() {
        return validateTokenMaxRetries;
    }
}
