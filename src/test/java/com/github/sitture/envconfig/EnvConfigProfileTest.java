package com.github.sitture.envconfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigProfileTest {

    private static final String PROFILE_ONE = "prof1";

    @TempDir
    Path tempDir;

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @SystemStub
    private final SystemProperties systemProperties = new SystemProperties();

    @BeforeEach
    void setUp() {
        EnvConfig.reset();
    }

    @Test
    void testCanGetPropertyFromProfile() {
        // given env is default and prof1.one exists in env properties
        setEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        // when an existing profile is set
        // and prof1.one also exists with a different value
        setProfile(PROFILE_ONE);
        // then value from profile property takes precedence
        Assertions.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testCanGetFromProfileWhenDifferentProfilePath() {
        // given env is default and prof1.one exists in env properties
        setEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        // when profiles path is different to config.path
        systemProperties.set(EnvConfigKey.CONFIG_PROFILES_PATH.getProperty(), "config/sample-profiles");
        // when an existing profile is set
        // and prof1.one also exists with a different value
        setProfile(PROFILE_ONE);
        // then value from profile property takes precedence
        Assertions.assertEquals("profiles.prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testCanPreferParentEnvironmentProfileOverEnvironmentFile() throws IOException {
        final Path configPath = Files.createDirectories(this.tempDir.resolve("config"));
        final Path profilesPath = Files.createDirectories(this.tempDir.resolve("profiles"));
        final Path defaultConfigDir = Files.createDirectories(configPath.resolve(EnvConfigUtils.CONFIG_ENV_DEFAULT));
        final Path testConfigDir = Files.createDirectories(configPath.resolve("test"));
        Files.writeString(defaultConfigDir.resolve("default.properties"), "base.one=default\n");
        Files.writeString(testConfigDir.resolve("test.properties"), "shared.one=current.env\n");
        final Path defaultProfileDir = Files.createDirectories(profilesPath.resolve(EnvConfigUtils.CONFIG_ENV_DEFAULT).resolve(PROFILE_ONE));
        Files.writeString(defaultProfileDir.resolve(PROFILE_ONE + ".properties"), "shared.one=parent.profile\n");

        systemProperties.set(EnvConfigKey.CONFIG_PATH.getProperty(), configPath.toString());
        systemProperties.set(EnvConfigKey.CONFIG_PROFILES_PATH.getProperty(), profilesPath.toString());
        setEnvironment("test");
        setProfile(PROFILE_ONE);

        Assertions.assertEquals("parent.profile", EnvConfig.get("shared.one"));
    }

    @Test
    void testDoesNotLoadProfilesWhenProfileIsNotSet() {
        // given env is default and profiles path contains only profile subdirectories
        setEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        systemProperties.set(EnvConfigKey.CONFIG_PROFILES_PATH.getProperty(), "config/sample-profiles");

        // then loading skips the empty profile layer and env values still resolve
        Assertions.assertNull(EnvConfig.get("prof1.two"));
    }

    @Test
    void testEnvironmentProfileTakesPriorityOverDefaultProfile() {
        // given default and environment profile values for the same key
        setEnvironment("test");
        setProfile("prof2");
        // then the environment profile wins
        Assertions.assertEquals("test.profile.prof2", EnvConfig.get("property.precedence"));
    }

    @Test
    void testDefaultProfileTakesPriorityOverEnvironmentSpecific() {
        // given default profile and environment-specific values for the same key
        setEnvironment("test");
        setProfile(PROFILE_ONE);
        // then the default profile wins
        Assertions.assertEquals("default.profile", EnvConfig.get("property.precedence"));
    }

    @Test
    void testCanGetFromProfileWhenProfileSetAsEnv() {
        // given env is default and prof1.one exists in env properties
        setEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        // when an existing profile is set
        // and prof1.one also exists with a different value
        environmentVariables.set("ENV_CONFIG_PROFILE", PROFILE_ONE);
        // then value from profile property takes precedence
        Assertions.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testCanGetFromProfileUnderDefault() {
        // given env is test and prof1.one exists in test/test.properties
        setEnvironment("test");
        // when an existing profile is set in default env only
        // and prof1.one also exists with a different value
        setProfile(PROFILE_ONE);
        // then value from profile property takes precedence
        Assertions.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
    }

    @Test
    void testCanGetFromProfileUnderDefaultProfileWhenDifferentProfilePath() {
        // given env is test and prof1.one exists in test/test.properties
        setEnvironment("test");
        // when profiles path is different to config.path
        systemProperties.set(EnvConfigKey.CONFIG_PROFILES_PATH.getProperty(), "config/sample-profiles");
        // when an existing profile is set in default env only
        // and prof1.one also exists with a different value
        setProfile(PROFILE_ONE);
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
    void testEnvironmentSpecificTakesPriorityOverDefault() {
        // given default and environment-specific values for the same key
        setEnvironment("test");
        // then the environment-specific value wins
        Assertions.assertEquals("test", EnvConfig.get("property.precedence"));
    }

    @Test
    void testKeepassTakesPriorityOverEnvironmentProfile() {
        // given keepass and profile entries for the same key
        setEnvironment("test");
        setProfile("prof2");
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_ENABLED.getProperty(), true);
        systemProperties.set(EnvConfigKey.CONFIG_KEEPASS_MASTERKEY.getProperty(), "envconfig");
        // then keepass wins
        Assertions.assertEquals("KEEPASS_VALUE", EnvConfig.get("property.keepass"));
    }

    @Test
    void testCanGetWhenEnvVarAndProfileValuesDifferent() {
        // given env is test and prof1.one exists in default/default.properties
        setEnvironment(EnvConfigUtils.CONFIG_ENV_DEFAULT);
        // when an existing profile exists
        // and prof1.one also exists in profile
        environmentVariables.set("CONFIG_ENV_PROFILE", PROFILE_ONE);
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
        setProfile(PROFILE_ONE);
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
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), environment);
    }

    private void setProfile(final String profile) {
        systemProperties.set(EnvConfigKey.CONFIG_PROFILE.getProperty(), profile);
    }

}
