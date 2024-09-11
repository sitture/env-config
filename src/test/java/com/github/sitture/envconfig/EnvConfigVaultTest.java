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
        setEnvironment("test");
        // then value from system property and environment variable takes priority
        Assertions.assertEquals(SYS_PROPERTY_VALUE, EnvConfig.get(key));
        Assertions.assertEquals(SYS_ENV_VALUE, EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testVaultTakesPriorityOverFiles() {
        final String key = "property.one";
        // given property exists in default config files
        // when vault loading is enabled
        // and default
        setVaultEnabled();
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        stubGetSecretSuccess();
        // then property from vault group takes priority
        Assertions.assertEquals("VAULT_VALUE", EnvConfig.get(key));
        Assertions.assertEquals("VAULT_VALUE", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testProjectVaultTakesPriorityOverVaultDefaultSecret() {
        final String key = "property.one";
        // given property exists in default config files
        // when vault loading is enabled
        setVaultEnabled();
        // and default secret path is set
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_DEFAULT_PATH_KEY, "path/to/common");
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        stubGetSecretSuccess();
        stubGetCommonSecretSuccess();
        // and property exists in both secret path and default path
        // then property from secret path takes priority
        Assertions.assertEquals("VAULT_VALUE", EnvConfig.get(key));
        Assertions.assertEquals("VAULT_VALUE", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testVaultDefaultSecretTakesPriorityOverFiles() {
        final String key = "property.two";
        // given property exists in default config files
        // when vault loading is enabled
        setVaultEnabled();
        // and default secret path is set
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_DEFAULT_PATH_KEY, "path/to/common");
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        stubGetSecretSuccess();
        stubGetCommonSecretSuccess();
        // and property exists only in default path
        // then property from default path takes priority
        Assertions.assertEquals("VAULT_DEFAULT_VALUE", EnvConfig.get(key));
        Assertions.assertEquals("VAULT_DEFAULT_VALUE", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    @Test
    void testVaultDefaultSecretTakesPriorityOverFiles2() {
        setVaultEnabled();
        // Given default secret path is set
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_DEFAULT_PATH_KEY, "path/to/common");
        // And environment is set to test
        setEnvironment("test");
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        // project/default
        stubFor(get("/v1/path/data/to/project/default").willReturn(okJson("{\n"
            + "  \"data\": {\n"
            + "    \"data\": {\n"
            + "       \"property.one\": \"VAULT_PROJECT_DEFAULT\",\n"
            + "       \"property.two\": \"VAULT_PROJECT_DEFAULT\",\n"
            + "       \"property.three\": \"VAULT_PROJECT_DEFAULT\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n")));
        // project/test
        stubFor(get("/v1/path/data/to/project/test").willReturn(okJson("{\n"
            + "  \"data\": {\n"
            + "    \"data\": {\n"
            + "       \"property.one\": \"VAULT_PROJECT_TEST\",\n"
            + "       \"property.four\": \"VAULT_PROJECT_TEST\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n")));
        // common/default
        stubFor(get("/v1/path/data/to/common/default").willReturn(okJson("{\n"
            + "  \"data\": {\n"
            + "    \"data\": {\n"
            + "       \"property.one\": \"VAULT_COMMON_DEFAULT\",\n"
            + "       \"property.two\": \"VAULT_COMMON_DEFAULT\",\n"
            + "       \"property.three\": \"VAULT_COMMON_DEFAULT\",\n"
            + "       \"property.five\": \"VAULT_COMMON_DEFAULT\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n")));
        // common/test
        stubFor(get("/v1/path/data/to/common/test").willReturn(okJson("{\n"
            + "  \"data\": {\n"
            + "    \"data\": {\n"
            + "       \"property.one\": \"VAULT_COMMON_TEST\",\n"
            + "       \"property.three\": \"VAULT_COMMON_TEST\",\n"
            + "       \"property.four\": \"VAULT_COMMON_TEST\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n")));
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
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_ENABLED_KEY, true);
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_ADDRESS_KEY, "http://localhost:8999");
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_NAMESPACE_KEY, "mock");
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_SECRET_PATH_KEY, "path/to/project");
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_TOKEN_KEY, "mock");
    }

    private void setEnvironment(final String environment) {
        systemProperties.set(EnvConfigUtils.CONFIG_ENV_KEY, environment);
    }

    private void stubGetSecretSuccess() {
        stubFor(get("/v1/path/data/to/project/default").willReturn(okJson("{\n"
            + "  \"data\": {\n"
            + "    \"data\": {\n"
            + "       \"property.eight\": \"VAULT_VALUE\",\n"
            + "       \"property.one\": \"VAULT_VALUE\",\n"
            + "       \"PROPERTY_ONE\": \"VAULT_VALUE\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n")));
    }

    private void stubGetCommonSecretSuccess() {
        stubFor(get("/v1/path/data/to/common/default").willReturn(okJson("{\n"
            + "  \"data\": {\n"
            + "    \"data\": {\n"
            + "       \"property.one\": \"VAULT_DEFAULT_VALUE\",\n"
            + "       \"PROPERTY_ONE\": \"VAULT_DEFAULT_VALUE\",\n"
            + "       \"property.two\": \"VAULT_DEFAULT_VALUE\",\n"
            + "       \"PROPERTY_TWO\": \"VAULT_DEFAULT_VALUE\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n")));
    }

    private void stubSelfLookupSuccess() {
        stubFor(get("/v1/auth/token/lookup-self").willReturn(okJson("{\n"
            + "  \"data\": {\n"
            + "    \"policies\": [\n"
            + "      \"default\"\n"
            + "    ]\n"
            + "  }\n"
            + "}")));
    }

}
