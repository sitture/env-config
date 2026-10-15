package com.github.sitture.envconfig;

record EnvConfigVaultProperties(String address,
                                String namespace,
                                String token,
                                String secretPath,
                                String defaultPath,
                                int validateTokenMaxRetries) {
}
