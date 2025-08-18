package com.github.sitture.envconfig;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
@WireMockTest(httpPort = 8999)
class EnvConfigVaultTest {

    private static final String PROPERTY_VAULT = "property.vault";
    private static final String SYS_PROPERTY_VALUE = "sys.property.value";
    private static final String SYS_ENV_VALUE = "sys.env.value";

    @SystemStub
    private final SystemProperties systemProperties = new SystemProperties();

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeEach
    void setUp() {
        EnvConfig.reset();
    }

    @Test
    void testSystemVariablesTakesPriorityOverVault() {
        final String key = PROPERTY_VAULT;
        // when vault loading is enabled
        // and property exists in keepass
        setVaultEnabled();
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        stubGetSecretSuccess();
        // and property is set as system property
        systemProperties.set(PROPERTY_VAULT, SYS_PROPERTY_VALUE);
        // and property is set as environment variable
        environmentVariables.set(EnvConfigUtils.getProcessedEnvKey(key), SYS_ENV_VALUE);
        // and property is set in environment file
        setEnvironment();
        // then value from system property and environment variable takes priority
        Assertions.assertEquals(SYS_PROPERTY_VALUE, EnvConfig.get(key));
        Assertions.assertEquals(SYS_ENV_VALUE, EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testVaultTakesPriorityOverFiles() {
        final String key = "property.one";
        // given property exists in default config files
        // when vault loading is enabled
        setVaultEnabled();
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        stubGetSecretSuccess();
        // then property from vault group takes priority
        Assertions.assertEquals("VAULT_PROJECT_DEFAULT", EnvConfig.get(key));
        Assertions.assertEquals("VAULT_PROJECT_DEFAULT", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testProjectVaultTakesPriorityOverVaultDefaultPath() {
        final String key = "property.one";
        // given property exists in default config files
        // when vault loading is enabled
        setVaultEnabled();
        // and default secret path is set
        systemProperties.set(EnvConfigKey.CONFIG_VAULT_DEFAULT_PATH.getProperty(), "path/to/common");
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        stubGetSecretSuccess();
        stubGetCommonSecretSuccess();
        // and property exists in both secret path and default path
        // then property from secret path takes priority
        Assertions.assertEquals("VAULT_PROJECT_DEFAULT", EnvConfig.get(key));
        Assertions.assertEquals("VAULT_PROJECT_DEFAULT", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testVaultDefaultPathTakesPriorityOverFiles() {
        final String key = "property.two";
        // given property exists in default config files
        // when vault loading is enabled
        setVaultEnabled();
        // and default secret path is set
        systemProperties.set(EnvConfigKey.CONFIG_VAULT_DEFAULT_PATH.getProperty(), "path/to/common");
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        stubGetSecretSuccess();
        stubGetCommonSecretSuccess();
        // and property exists only in default path
        // then property from default path takes priority
        Assertions.assertEquals("VAULT_COMMON_DEFAULT", EnvConfig.get(key));
        Assertions.assertEquals("VAULT_COMMON_DEFAULT", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testVaultPrecedenceWhenBothPathAndDefaultPathAreSet() {
        setVaultEnabled();
        // Given default secret path is set
        systemProperties.set(EnvConfigKey.CONFIG_VAULT_DEFAULT_PATH.getProperty(), "path/to/common");
        // And environment is set to test
        setEnvironment();
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        // project/default
        stubFor(get("/v1/path/data/to/project/default").willReturn(okJson("""
            {
              "data": {
                "data": {
                   "property.one": "VAULT_PROJECT_DEFAULT",
                   "property.two": "VAULT_PROJECT_DEFAULT",
                   "property.three": "VAULT_PROJECT_DEFAULT"
                }
              }
            }
            """)));
        // project/test
        stubFor(get("/v1/path/data/to/project/test").willReturn(okJson("""
            {
              "data": {
                "data": {
                   "property.one": "VAULT_PROJECT_TEST",
                   "property.four": "VAULT_PROJECT_TEST"
                }
              }
            }
            """)));
        // common/default
        stubFor(get("/v1/path/data/to/common/default").willReturn(okJson("""
            {
              "data": {
                "data": {
                   "property.one": "VAULT_COMMON_DEFAULT",
                   "property.two": "VAULT_COMMON_DEFAULT",
                   "property.three": "VAULT_COMMON_DEFAULT",
                   "property.five": "VAULT_COMMON_DEFAULT"
                }
              }
            }
            """)));
        // common/test
        stubFor(get("/v1/path/data/to/common/test").willReturn(okJson("""
            {
              "data": {
                "data": {
                   "property.one": "VAULT_COMMON_TEST",
                   "property.three": "VAULT_COMMON_TEST",
                   "property.four": "VAULT_COMMON_TEST"
                }
              }
            }
            """)));
        // when property in project/default, project/test, common/default, common/test
        // then value from project/test should take priority
        Assertions.assertEquals("VAULT_PROJECT_TEST", EnvConfig.get("property.one"));
        // when property in project/default and common/default
        // then value from project/default should take priority
        Assertions.assertEquals("VAULT_PROJECT_DEFAULT", EnvConfig.get("property.two"));
        // when property in project/default, common/default, common/test
        // then value from common/test should take priority
        Assertions.assertEquals("VAULT_COMMON_TEST", EnvConfig.get("property.three"));
        // when property in project/test, common/test
        // then value from common/test should take priority
        Assertions.assertEquals("VAULT_PROJECT_TEST", EnvConfig.get("property.four"));
        // when property in common/default only
        // then value from common/default should take priority
        Assertions.assertEquals("VAULT_COMMON_DEFAULT", EnvConfig.get("property.five"));
    }

    private void setVaultEnabled() {
        systemProperties.set(EnvConfigKey.CONFIG_VAULT_ENABLED.getProperty(), true);
        systemProperties.set(EnvConfigKey.CONFIG_VAULT_ADDRESS.getProperty(), "http://localhost:8999");
        systemProperties.set(EnvConfigKey.CONFIG_VAULT_NAMESPACE.getProperty(), "mock");
        systemProperties.set(EnvConfigKey.CONFIG_VAULT_SECRET_PATH.getProperty(), "path/to/project");
        systemProperties.set(EnvConfigKey.CONFIG_VAULT_TOKEN.getProperty(), "mock");
    }

    private void setEnvironment() {
        systemProperties.set(EnvConfigKey.CONFIG_ENV.getProperty(), "test");
    }

    private void stubGetSecretSuccess() {
        stubFor(get("/v1/path/data/to/project/default").willReturn(okJson("""
            {
              "data": {
                "data": {
                   "property.eight": "VAULT_PROJECT_DEFAULT",
                   "property.one": "VAULT_PROJECT_DEFAULT",
                   "PROPERTY_ONE": "VAULT_PROJECT_DEFAULT"
                }
              }
            }
            """)));
    }

    private void stubGetCommonSecretSuccess() {
        stubFor(get("/v1/path/data/to/common/default").willReturn(okJson("""
            {
              "data": {
                "data": {
                   "property.one": "VAULT_COMMON_DEFAULT",
                   "PROPERTY_ONE": "VAULT_COMMON_DEFAULT",
                   "property.two": "VAULT_COMMON_DEFAULT",
                   "PROPERTY_TWO": "VAULT_COMMON_DEFAULT"
                }
              }
            }
            """)));
    }

    private void stubSelfLookupSuccess() {
        stubFor(get("/v1/auth/token/lookup-self").willReturn(okJson("""
            {
              "data": {
                "policies": [
                  "default"
                ]
              }
            }\
            """)));
    }

}
