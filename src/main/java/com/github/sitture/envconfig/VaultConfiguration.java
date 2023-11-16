package com.github.sitture.envconfig;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.LogicalResponse;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.lang3.StringUtils;

class VaultConfiguration {

    private final Vault vault;
    private final EnvConfigVaultProperties vaultProperties;

    VaultConfiguration(final EnvConfigVaultProperties vaultProperties)  {
        this.vaultProperties = vaultProperties;
        try {
            final VaultConfig config = new VaultConfig()
                    .address(vaultProperties.getAddress())
                    .nameSpace(vaultProperties.getNamespace())
                    .token(vaultProperties.getToken())
                    .build();
            this.vault = Vault.create(config);
            // attempt to lookupSelf to validate token
            this.vault.auth().lookupSelf();
        } catch (VaultException e) {
            throw new EnvConfigException("Could not connect to vault", e);
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
