package com.github.sitture.envconfig;

import java.util.Map;
import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigKeepassConfigurationTest {

    private static final String CONFIG_KEEPASS_PASSWORD = "envconfig";

    @SystemStub
    private final SystemProperties systemProperties = new SystemProperties();

    @Test
    void testKeepassConfigurationBuildsConfigurationForConfiguredEnvironments() {
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), CONFIG_KEEPASS_PASSWORD);
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_FILENAME.getProperty(), "env-config.kdbx");
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test");

        final Map<String, Configuration> configurationMap = new EnvConfigKeepassConfiguration(new EnvConfigProperties()).getConfiguration();

        Assertions.assertEquals(2, configurationMap.size());
        Assertions.assertEquals(4, configurationMap.get("default").size());
        Assertions.assertEquals("KEEPASS_VALUE", configurationMap.get("default").getString("PROPERTY_KEEPASS"));
        Assertions.assertEquals("KEEPASS_VALUE", configurationMap.get("default").getString("property.keepass"));
        Assertions.assertEquals("ANOTHER_PROPERTY", configurationMap.get("test").getString("another.property"));
    }
}

