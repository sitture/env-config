package com.github.sitture.envconfig;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;

class EnvironmentVariables {

    private final Configuration systemConfiguration;
    private final Configuration environmentConfiguration;

    EnvironmentVariables() {
        systemConfiguration = new SystemConfiguration();
        environmentConfiguration = new MapConfiguration(getEnvMap());
    }

    private static Map<String, String> getEnvMap() {
        final Map<String, String> envMap = new HashMap<>();
        System.getenv().forEach((key, value) -> {
            envMap.put(key, value);
            envMap.put(EnvConfigUtils.getProcessedPropertyKey(key), value);
        });
        return envMap;
    }

    protected Configuration getSystemConfiguration() {
        return systemConfiguration;
    }

    protected Configuration getEnvironmentConfiguration() {
        return environmentConfiguration;
    }

}
