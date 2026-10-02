package com.github.sitture.envconfig;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.LogicalResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EnvConfigVaultConfiguration extends AbstractEnvConfigConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EnvConfigVaultConfiguration.class);
    private final Vault vault;
    private final EnvConfigVaultProperties vaultProperties;

    EnvConfigVaultConfiguration(final EnvConfigProperties configProperties) {
        this(configProperties.getVaultProperties(), configProperties.getEnvironments());
    }

    EnvConfigVaultConfiguration(final EnvConfigVaultProperties vaultProperties) {
        this(vaultProperties, List.of(EnvConfigUtils.CONFIG_ENV_DEFAULT));
    }

    private EnvConfigVaultConfiguration(final EnvConfigVaultProperties vaultProperties,
                                        final List<String> environments) {
        super(environments);
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

    @SuppressWarnings("PMD.DoNotUseThreads")
    private static void retryUntilMaxMaxRetries(final VaultException vaultException, final int attempt, final int validateTokenMaxRetries) {
        final long retryInterval = attempt * 2L;
        logError("An exception occurred validating the vault token, will retry in %s seconds".formatted(retryInterval), vaultException);
        try {
            TimeUnit.SECONDS.sleep(retryInterval);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            final String message = "InterruptedException thrown whilst waiting to retry validating the vault token";
            logError(message, ex);
            throw new EnvConfigException(message, ex);
        }
        if (attempt == validateTokenMaxRetries - 1) {
            final String message = "Reached CONFIG_VAULT_VALIDATE_MAX_RETRIES limit (%s) attempting to validate token".formatted(validateTokenMaxRetries);
            logError(message, vaultException);

            throw new EnvConfigException(message, vaultException);
        }
    }

    private static void logError(final String message, final Exception exception) {
        if (LOG.isErrorEnabled()) {
            LOG.error(message, exception);
        }
    }

    @Override
    protected Configuration getConfigurationForEnvironment(final String env) {
        final CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(getConfigurationForPath(env, this.vaultProperties.getSecretPath()));
        this.vaultProperties.getDefaultPath()
            .ifPresent(path -> configuration.addConfiguration(getConfigurationForPath(env, path)));
        return configuration;
    }

    private Configuration getConfigurationForPath(final String env, final String path) {
        final String secret = "%s/%s".formatted(Strings.CS.removeEnd(path, "/"), env);
        final LogicalResponse response = readSecret(secret);
        validateSecretResponse(env, secret, response);
        return new MapConfiguration(getResponseData(response));
    }

    private LogicalResponse readSecret(final String secret) {
        try {
            LOG.debug("Loading config from secret {}", secret);
            return this.vault.logical().read(secret);
        } catch (VaultException e) {
            throw new EnvConfigException("Could not read data from vault.", e);
        }
    }

    private static void validateSecretResponse(final String env, final String secret, final LogicalResponse response) {
        final boolean missingDefaultSecret = EnvConfigUtils.CONFIG_ENV_DEFAULT.equals(env)
            && (response == null || response.getRestResponse().getStatus() != 200);
        if (missingDefaultSecret) {
            throw new EnvConfigException("Could not find the vault secret: %s".formatted(secret));
        }
    }

    private static Map<String, String> getResponseData(final LogicalResponse response) {
        return response == null || response.getData() == null ? Map.of() : response.getData();
    }

}
