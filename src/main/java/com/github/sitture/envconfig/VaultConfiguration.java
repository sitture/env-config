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
                final long retryInterval = i * 2L;
                logError(String.format("An exception occurred validating the vault token, will retry in %s seconds", retryInterval), vaultException);
                try {
                    TimeUnit.SECONDS.sleep(retryInterval);
                } catch (InterruptedException ex) {
                    logError("InterruptedException thrown whilst waiting to retry validating the vault token", ex);
                }
                if (i == validateTokenMaxRetries - 1) {
                    final String message = String.format("Reached CONFIG_VAULT_VALIDATE_MAX_RETRIES limit (%s) attempting to validate token", validateTokenMaxRetries);
                    logError(message, vaultException);

                    throw new EnvConfigException(message, vaultException);
                }
            }
        }
    }

    private static void logError(final String message, final Exception exception) {
        if (LOG.isErrorEnabled()) {
            LOG.error(message, exception);
        }
    }

    public Configuration getConfiguration(final String env) {
        try {
            final String secret = String.format("%s/%s", StringUtils.removeEnd(this.vaultProperties.getSecretPath(), "/"), env);
            final LogicalResponse response = this.vault.logical().read(secret);
            if (response.getRestResponse().getStatus() != 200 && EnvConfigUtils.CONFIG_ENV_DEFAULT.equals(env)) {
                throw new EnvConfigException(String.format("Could not find the vault secret: %s", secret));
            }
            return new MapConfiguration(response.getData());
        } catch (VaultException e) {
            throw new EnvConfigException("Could not read data from vault.", e);
        }
    }

}
