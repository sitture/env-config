package com.github.sitture.envconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class EnvConfigUtilsTest {

    @Test
    void testCanGetProcessedPropertyKey() {
        assertEquals("foo.key", EnvConfigUtils.getProcessedPropertyKey("FOO_KEY"));
    }

    @Test
    void testCanGetProcessedEnvVarKey() {
        assertEquals("FOO_KEY", EnvConfigUtils.getProcessedEnvKey("Foo.Key"));
    }

    @Test
    void testCanGetListOfValues() {
        assertEquals(List.of("foo", "bar", ""), EnvConfigUtils.getListOfValues("foo , bar, ", EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
        assertEquals(List.of("one"), EnvConfigUtils.getListOfValues("one", EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
        assertEquals(List.of(), EnvConfigUtils.getListOfValues(null, EnvConfigUtils.CONFIG_DELIMITER_DEFAULT));
    }

}
