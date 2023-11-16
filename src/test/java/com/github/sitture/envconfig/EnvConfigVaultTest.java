package com.github.sitture.envconfig;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

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
        // when keepass loading is enabled
        setVaultEnabled();
        // setup wiremock stubs for vault
        stubSelfLookupSuccess();
        stubGetSecretSuccess();
        // then property from vault group takes priority
        Assertions.assertEquals("VAULT_VALUE", EnvConfig.get(key));
        Assertions.assertEquals("VAULT_VALUE", EnvConfig.get(EnvConfigUtils.getProcessedEnvKey(key)));
    }

    private void setVaultEnabled() {
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_ENABLED_KEY, true);
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_ADDRESS_KEY, "http://localhost:8999");
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_NAMESPACE_KEY, "mock");
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_SECRET_PATH_KEY, "path/to/mock");
        systemProperties.set(EnvConfigUtils.CONFIG_VAULT_TOKEN_KEY, "mock");
    }

    private void setEnvironment(final String environment) {
        systemProperties.set(EnvConfigUtils.CONFIG_ENV_KEY, environment);
    }

    private void stubGetSecretSuccess() {
        stubFor(get("/v1/path/data/to/mock/default").willReturn(okJson("{\n"
                + "  \"data\": {\n"
                + "    \"data\": {\n"
                + "       \"property.eight\": \"VAULT_VALUE\",\n"
                + "       \"property.one\": \"VAULT_VALUE\",\n"
                + "       \"PROPERTY_ONE\": \"VAULT_VALUE\"\n"
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
