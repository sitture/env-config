package com.github.sitture.envconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnvConfigKeyTest {

    @Test
    void testCanGetConfigKey() {
        assertEquals("env.config.environment", EnvConfigKey.CONFIG_ENV.getProperty());
        assertEquals("ENV_CONFIG_ENVIRONMENT", EnvConfigKey.CONFIG_ENV.getEnvProperty());
    }

}
