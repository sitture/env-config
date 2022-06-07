package com.github.sitture.envconfig;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvConfigUtilsTest {

    @Test
    void testCanGetProcessedEnvVarKey() {
        assertEquals("foo.key", EnvConfigUtils.getProcessedEnvKey("FOO_KEY"));
    }

    @Test
    void testCanGetListOfValues() {
        assertEquals(List.of("foo", "bar", ""), EnvConfigUtils.getListOfValues("foo , bar, ", EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
        assertEquals(List.of("one"), EnvConfigUtils.getListOfValues("one", EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
        assertEquals(List.of(), EnvConfigUtils.getListOfValues(null, EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
    }

}
