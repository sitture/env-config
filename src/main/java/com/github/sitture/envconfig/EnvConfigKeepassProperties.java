package com.github.sitture.envconfig;

class EnvConfigKeepassProperties {
    private final String filename;
    private final String masterKey;

    EnvConfigKeepassProperties(final String filename, final String masterKey) {
        this.filename = filename;
        this.masterKey = masterKey;
    }

    String getFilename() {
        return filename;
    }

    String getMasterKey() {
        return masterKey;
    }
}
