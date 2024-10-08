package com.github.sitture.envconfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigProfileTest {

    @SystemStub
    EnvironmentVariables environmentVariables;

    @BeforeEach
    void setUp() {
        EnvConfig.reset();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(EnvConfigKey.CONFIG_PROFILE.getProperty());
        System.clearProperty(EnvConfigKey.CONFIG_PROFILES_PATH.getProperty());
    }

    @Test
    void testCanGetPropertyFromProfile() {
        // given env is default and prof1.one exists in env properties
        setEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        // when an existing profile is set
        // and prof1.one also exists with a different value
        setProfile("prof1");
        // then value from profile property takes precedence
        Assertions.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testCanGetFromProfileWhenDifferentProfilePath() {
        // given env is default and prof1.one exists in env properties
        setEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        // when profiles path is different to config.path
        setProfilePath("config/sample-profiles");
        // when an existing profile is set
        // and prof1.one also exists with a different value
        setProfile("prof1");
        // then value from profile property takes precedence
        Assertions.assertEquals("profiles.prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testCanGetFromProfileWhenProfileSetAsEnv() {
        // given env is default and prof1.one exists in env properties
        setEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        // when an existing profile is set
        // and prof1.one also exists with a different value
        environmentVariables.set("ENV_CONFIG_PROFILE", "prof1");
        // then value from profile property takes precedence
        Assertions.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testCanGetFromProfileUnderDefault() {
        // given env is test and prof1.one exists in test/test.properties
        setEnvironment("test");
        // when an existing profile is set in default env only
        // and prof1.one also exists with a different value
        setProfile("prof1");
        // then value from profile property takes precedence
        Assertions.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testCanGetFromProfileUnderDefaultProfileWhenDifferentProfilePath() {
        // given env is test and prof1.one exists in test/test.properties
        setEnvironment("test");
        // when profiles path is different to config.path
        setProfilePath("config/sample-profiles");
        // when an existing profile is set in default env only
        // and prof1.one also exists with a different value
        setProfile("prof1");
        // then value from profile property takes precedence
        Assertions.assertEquals("profiles.prof1.value", EnvConfig.get("prof1.two"));
    }

    @Test
    void testCanGetOverrideFromProfileUnderEnv() {
        // given env is test and property.two doesn't exists in test/test.properties
        setEnvironment("test");
        // when an existing profile (prof2) exists in default and test envs
        // and prof2.one also exists with a different values
        setProfile("prof2");
        // then value from profile value under test env takes precedence
        Assertions.assertEquals("test.prof2.value", EnvConfig.get("prof2.one"));
    }

    @Test
    void testCanGetFromProfileUnderEnv() {
        // given env is test and prof1.one exists in test/test.properties
        setEnvironment("test");
        // when an existing profile only exists in test env
        // and prof1.one also exists with a different value
        setProfile("test-prof1");
        // then value from profile property takes precedence
        Assertions.assertEquals("test.prof1.value", EnvConfig.get("test-prof1.one"));
    }

    @Test
    void testCanGetWhenEnvVarAndProfileValuesDifferent() {
        // given env is test and prof1.one exists in default/default.properties
        setEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        // when an existing profile exists
        // and prof1.one also exists in profile
        environmentVariables.set("CONFIG_ENV_PROFILE", "prof1");
        // and prof1.one also set as environment variable
        environmentVariables.set("PROF1_ONE", "env.prof1.value");
        // then value from profile property takes precedence
        Assertions.assertEquals("env.prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testCanGetFromProfileWhenMultipleEnvs() {
        // given env is test-env with no profiles
        setEnvironment("test,test-env");
        // and test env is set as base with property set in prof2 profile
        setProfile("prof2");
        // then value from profile takes precedence
        Assertions.assertEquals("test.prof2.value", EnvConfig.get("prof2.one"));
    }

    @Test
    void testCanGetFromDefaultProfileWhenMultipleEnvs() {
        // given env is test-env with no profiles
        setEnvironment("test,test-env");
        // and test env is set base with non-existing profile
        setProfile("prof1");
        // then value from profile takes precedence
        Assertions.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testThrowsExceptionWhenNoPropertiesInProfile() {
        // given env is default and empty-profile exists in env properties
        setEnvironment("default");
        // when an and empty-profile directory does not contain any valid properties files
        setProfile("empty-profile");
        // then an exception is thrown
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> EnvConfig.getOrThrow("non.existing"));
        Assertions.assertTrue(exception.getMessage().startsWith("No property files found under"), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().endsWith("/env-config/config/default/empty-profile'"));
    }

    private void setEnvironment(final String environment) {
        System.setProperty(EnvConfigKey.CONFIG_ENV.getProperty(), environment);
    }

    private void setProfile(final String profile) {
        System.setProperty(EnvConfigKey.CONFIG_PROFILE.getProperty(), profile);
    }

    private void setProfilePath(final String path) {
        System.setProperty(EnvConfigKey.CONFIG_PROFILES_PATH.getProperty(), path);
    }

}
