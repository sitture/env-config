package com.github.sitture.env.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SystemStubsExtension.class)
class EnvConfigProfileTest {

	private static final String CONFIG_ENV_KEY = "config.env";
	private static final String CONFIG_ENV_PROFILE = "config.env.profile";
	private static final String DEFAULT_ENVIRONMENT = "default";

	@SystemStub
	EnvironmentVariables environmentVariables;

	@BeforeEach
	void setUp() {
		EnvConfig.reset();
	}

	@Test
	void testCanGetPropertyFromProfile() {
		// given env is default and prof1.one exists in env properties
		setEnvironment(DEFAULT_ENVIRONMENT);
		// when an existing profile is set
		// and prof1.one also exists with a different value
		setProfile("prof1");
		// then value from profile property takes precedence
		assertEquals("prof1.value", EnvConfig.get("prof1.one"));
	}

	@Test
	void testCanGetFromProfileWhenProfileSetAsEnv() {
		// given env is default and prof1.one exists in env properties
		setEnvironment(DEFAULT_ENVIRONMENT);
		// when an existing profile is set
		// and prof1.one also exists with a different value
		environmentVariables.set("CONFIG_ENV_PROFILE", "prof1");
		// then value from profile property takes precedence
		assertEquals("prof1.value", EnvConfig.get("prof1.one"));
	}

	@Test
	void testCanGetFromProfileUnderDefault() {
		// given env is test and prof1.one exists in test/test.properties
		setEnvironment("test");
		// when an existing profile is set in default env only
		// and prof1.one also exists with a different value
		setProfile("prof1");
		// then value from profile property takes precedence
		assertEquals("prof1.value", EnvConfig.get("prof1.one"));
	}

	@Test
	void testCanGetOverrideFromProfileUnderEnv() {
		// given env is test and property.two doesn't exists in test/test.properties
		setEnvironment("test");
		// when an existing profile (prof2) exists in default and test envs
		// and prof2.one also exists with a different values
		setProfile("prof2");
		// then value from profile value under test env takes precedence
		assertEquals("test.prof2.value", EnvConfig.get("prof2.one"));
	}

	@Test
	void testCanGetFromProfileUnderEnv() {
		// given env is test and prof1.one exists in test/test.properties
		setEnvironment("test");
		// when an existing profile only exists in test env
		// and prof1.one also exists with a different value
		setProfile("test-prof1");
		// then value from profile property takes precedence
		assertEquals("test.prof1.value", EnvConfig.get("test-prof1.one"));
	}

	@Test
	void testCanGetWhenEnvVarAndProfileValuesDifferent() {
		// given env is test and prof1.one exists in default/default.properties
		setEnvironment(DEFAULT_ENVIRONMENT);
		// when an existing profile exists
		// and prof1.one also exists in profile
		environmentVariables.set("CONFIG_ENV_PROFILE", "prof1");
		// and prof1.one also set as environment variable
		environmentVariables.set("PROF1_ONE", "env.prof1.value");
		// then value from profile property takes precedence
		assertEquals("env.prof1.value", EnvConfig.get("prof1.one"));
	}

	@Test
	void testCanGetFromProfileWhenMultipleEnvs() {
		// given env is test-env with no profiles
		setEnvironment("test,test-env");
		// and test env is set as base with property set in prof2 profile
		setProfile("prof2");
		// then value from profile takes precedence
		assertEquals("test.prof2.value", EnvConfig.get("prof2.one"));
	}

	@Test
	void testCanGetFromDefaultProfileWhenMultipleEnvs() {
		// given env is test-env with no profiles
		setEnvironment("test,test-env");
		// and test env is set base with non-existing profile
		setProfile("prof1");
		// then value from profile takes precedence
		assertEquals("prof1.value", EnvConfig.get("prof1.one"));
	}

	private void setEnvironment(final String environment) {
		System.setProperty(CONFIG_ENV_KEY, environment);
	}

	private void setProfile(final String profile) {
		System.setProperty(CONFIG_ENV_PROFILE, profile);
	}

}
