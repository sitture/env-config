package com.sitture.env.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class EnvConfigTest {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Before
	public void setup() {
		environmentVariables.clear("CONFIG_ENV", "CONFIG_DIR");
	}

	@Test
	public void testCanGetDefaultEnvironment() {
		System.setProperty("config.env", "default");
		Assert.assertEquals("default", EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetEnvironment() {
		System.setProperty("config.env", "test");
		Assert.assertEquals("test", EnvConfig.getEnvironment());
	}

	@Test
	public void testCanGetParsedInt() {
		System.setProperty("property", "123");
		Assert.assertEquals(123, EnvConfig.getInteger("property"));
	}

	@Test
	public void testCanGetParsedBoolean() {
		System.setProperty("property", "true");
		Assert.assertTrue(EnvConfig.getBool("property"));
		System.setProperty("property", "false");
		Assert.assertFalse(EnvConfig.getBool("property"));
		System.setProperty("property", "falssse");
		Assert.assertFalse(EnvConfig.getBool("property"));
	}

	@Test
	public void testCanGetDefaultForNonExistingProperty() {
		Assert.assertEquals("test", EnvConfig.get("non.existing", "test"));
	}

	@Test
	public void testCanGetEnvVarUsingPropertyKey() {
		Assert.assertEquals(System.getenv("PATH"), EnvConfig.get("path"));
	}

	@Test
	public void testNullForNonExistingProperty() {
		Assert.assertNull(EnvConfig.get("non.existing"));
	}

	@Test(expected = MissingVariableException.class)
	public void testMissingVariableExceptionThrown() {
		EnvConfig.get("non.existing", true);
	}

	@Test
	public void testCanAddANewProperty() {
		EnvConfig.add("1", 1);
		Assert.assertEquals(1, EnvConfig.getInteger("1"));
		Assert.assertEquals("1", EnvConfig.get("1"));
		Assert.assertFalse(EnvConfig.getBool("1"));
		EnvConfig.add("1", "1");
		Assert.assertEquals(1, EnvConfig.getInteger("1"));
		Assert.assertEquals("1", EnvConfig.get("1"));
		EnvConfig.add("flag", true);
		Assert.assertTrue(EnvConfig.getBool("flag"));
	}

	@Test
	public void testCanSetProperty() {
		System.setProperty("property", "value");
		Assert.assertEquals("value", EnvConfig.get("property"));
		// re-assign to an existing property
		EnvConfig.set("property", "updatedValue");
		Assert.assertEquals("updatedValue", EnvConfig.get("property"));
		// set a new property
		EnvConfig.set("property2", "value");
		Assert.assertEquals("value", EnvConfig.get("property2"));
	}

}
