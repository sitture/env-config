package com.github.sitture.env.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class EnvConfigProfileTest {

	private static final String CONFIG_ENV_KEY = "config.env";
	private static final String CONFIG_ENV_PROFILE = "config.env.profile";
	private static final String DEFAULT_ENVIRONMENT = "default";

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setUp() {
		EnvConfig.reset();
		environmentVariables.clear(CONFIG_ENV_PROFILE);
	}

	@Test
	public void testCanGetPropertyFromProfile() {
		// given env is default and prof1.one exists in env properties
		setEnvironment(DEFAULT_ENVIRONMENT);
		// when an existing profile is set
		// and prof1.one also exists with a different value
		setProfile("prof1");
		// then value from profile property takes precedence
		Assert.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
	}

	@Test
	public void testCanGetFromProfileWhenProfileSetAsEnv() {
		// given env is default and prof1.one exists in env properties
		setEnvironment(DEFAULT_ENVIRONMENT);
		// when an existing profile is set
		// and prof1.one also exists with a different value
		environmentVariables.set("CONFIG_ENV_PROFILE", "prof1");
		// then value from profile property takes precedence
		Assert.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
	}

	@Test
	public void testCanGetFromProfileUnderDefault() {
		// given env is test and prof1.one exists in test/test.properties
		setEnvironment("test");
		// when an existing profile is set in default env only
		// and prof1.one also exists with a different value
		setProfile("prof1");
		// then value from profile property takes precedence
		Assert.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
	}

	@Test
	public void testCanGetOverrideFromProfileUnderEnv() {
		// given env is test and property.two doesn't exists in test/test.properties
		setEnvironment("test");
		// when an existing profile (prof2) exists in default and test envs
		// and prof2.one also exists with a different values
		setProfile("prof2");
		// then value from profile value under test env takes precedence
		Assert.assertEquals("test.prof2.value", EnvConfig.get("prof2.one"));
	}

	@Test
	public void testCanGetFromProfileUnderEnv() {
		// given env is test and prof1.one exists in test/test.properties
		setEnvironment("test");
		// when an existing profile only exists in test env
		// and prof1.one also exists with a different value
		setProfile("test-prof1");
		// then value from profile property takes precedence
		Assert.assertEquals("test.prof1.value", EnvConfig.get("test-prof1.one"));
	}

	@Test
	public void testCanGetWhenEnvVarAndProfileValuesDifferent() {
		// given env is test and prof1.one exists in default/default.properties
		setEnvironment(DEFAULT_ENVIRONMENT);
		// when an existing profile exists
		// and prof1.one also exists in profile
		environmentVariables.set("CONFIG_ENV_PROFILE", "prof1");
		// and prof1.one also set as environment variable
		environmentVariables.set("PROF1_ONE", "env.prof1.value");
		// then value from profile property takes precedence
		Assert.assertEquals("env.prof1.value", EnvConfig.get("prof1.one"));
	}

	@Test
	public void testCanGetFromProfileWhenMultipleEnvs() {
		// given env is test-env with no profiles
		setEnvironment("test,test-env");
		// and test env is set as base with property set in prof2 profile
		setProfile("prof2");
		// then value from profile takes precedence
		Assert.assertEquals("test.prof2.value", EnvConfig.get("prof2.one"));
	}

	@Test
	public void testCanGetFromDefaultProfileWhenMultipleEnvs() {
		// given env is test-env with no profiles
		setEnvironment("test,test-env");
		// and test env is set base with non-existing profile
		setProfile("prof1");
		// then value from profile takes precedence
		Assert.assertEquals("prof1.value", EnvConfig.get("prof1.one"));
	}

	private void setEnvironment(final String environment) {
		System.setProperty(CONFIG_ENV_KEY, environment);
	}

	private void setProfile(final String profile) {
		System.setProperty(CONFIG_ENV_PROFILE, profile);
	}

}
