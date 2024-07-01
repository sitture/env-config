package com.github.sitture.envconfig;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.LogicalResponse;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


class VaultConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(VaultConfiguration.class);
    private final Vault vault;
    private final EnvConfigVaultProperties vaultProperties;

    VaultConfiguration(final EnvConfigVaultProperties vaultProperties) {
        this.vaultProperties = vaultProperties;
        try {
            final VaultConfig config = new VaultConfig()
                    .address(vaultProperties.getAddress())
                    .nameSpace(vaultProperties.getNamespace())
                    .token(vaultProperties.getToken())
                    .build();
            this.vault = Vault.create(config);
            validateToken();
        } catch (VaultException vaultException) {
            throw new EnvConfigException("Could not connect to vault", vaultException);
        }
    }

    private void validateToken() throws VaultException {
        final int validateTokenMaxRetries = this.vaultProperties.getValidateTokenMaxRetries();
        for (int i = 0; i < validateTokenMaxRetries; i++) {
            try {
                this.vault.auth().lookupSelf();
                break;
            } catch (VaultException vaultException) {
                retryUntilMaxMaxRetries(vaultException, i, validateTokenMaxRetries);
            }
        }
    }

    private static void retryUntilMaxMaxRetries(final VaultException vaultException, final int attempt, final int validateTokenMaxRetries) {
        final long retryInterval = attempt * 2L;
        logError(String.format("An exception occurred validating the vault token, will retry in %s seconds", retryInterval), vaultException);
        try {
            TimeUnit.SECONDS.sleep(retryInterval);
        } catch (InterruptedException ex) {
            logError("InterruptedException thrown whilst waiting to retry validating the vault token", ex);
        }
        if (attempt == validateTokenMaxRetries - 1) {
            final String message = String.format("Reached CONFIG_VAULT_VALIDATE_MAX_RETRIES limit (%s) attempting to validate token", validateTokenMaxRetries);
            logError(message, vaultException);

            throw new EnvConfigException(message, vaultException);
        }
    }

    private static void logError(final String message, final Exception exception) {
        if (LOG.isErrorEnabled()) {
            LOG.error(message, exception);
        }
    }


    public Configuration getConfiguration(final String env) {
        final List<String> paths = getSecretPaths();
        final Map<String, Object> combinedData = new HashMap<>();
        String secretNotFound = null;
        final boolean defaultEnv = EnvConfigUtils.CONFIG_ENV_DEFAULT.equals(env);

        for (final String path : paths) {
            final String secret = formatSecretPath(path, env);
            try {
                final LogicalResponse response = this.vault.logical().read(secret);
                if (response != null && response.getRestResponse().getStatus() == 200) {
                    combinedData.putAll(response.getData());
                } else if (defaultEnv) {
                    secretNotFound = secret;
                }
            } catch (VaultException e) {
                throw new EnvConfigException("Could not read data from vault.", e);
            }
        }

        handleDefaultEnvCase(combinedData, defaultEnv, secretNotFound);

        return new MapConfiguration(combinedData);
    }

    private void handleDefaultEnvCase(final Map<String, Object> combinedData, final boolean defaultEnv, final String secretNotFound) {
        if (combinedData.isEmpty() && defaultEnv) {
            throw new EnvConfigException(String.format("Could not find the vault secret: %s", secretNotFound));
        }
    }

    private List<String> getSecretPaths() {
        return Arrays.asList(this.vaultProperties.getSecretPath().split("\\s*,\\s*"));
    }

    private String formatSecretPath(final String path, final String env) {
        return String.format("%s/%s", StringUtils.removeEnd(path, "/"), env);
    }
}