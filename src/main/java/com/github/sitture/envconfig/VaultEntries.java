package com.github.sitture.envconfig;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;

class VaultEntries {

    private final Vault vault;

    VaultEntries(final String address, final String namespace, final String token)  {
        try {
            final VaultConfig config = new VaultConfig()
                    .address(address)
                    .nameSpace(namespace)
                    .token(token)
                    .build();
            vault = Vault.create(config);
        } catch (VaultException e) {
            throw new EnvConfigException("Could not connect to vault", e);
        }
    }

    public Configuration getEntriesConfiguration(final String secret) {
        try {
            // attempt to lookupSelf to validate token
            this.vault.auth().lookupSelf();
            return new MapConfiguration(this.vault.logical().read(secret).getData());
        } catch (VaultException e) {
            throw new EnvConfigException("Could not read data from vault.", e);
        }
    }

}
