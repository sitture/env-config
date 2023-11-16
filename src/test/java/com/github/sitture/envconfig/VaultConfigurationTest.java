package com.github.sitture.envconfig;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

@WireMockTest(httpPort = 8999)
class VaultConfigurationTest {

    @Test
    void testCanGetConfigurationMapWithData() {
        stubSelfLookupSuccess();
        final String secret = "path/to/project/";
        stubGetSecretSuccess();
        final EnvConfigVaultProperties vaultProperties = new EnvConfigVaultProperties("http://localhost:8999", "mock", "mock_token", secret);
        final Configuration configuration = new VaultConfiguration(vaultProperties).getConfiguration("default");
        Assertions.assertEquals("value1", configuration.getString("key1"));
        Assertions.assertEquals("value2", configuration.getString("key2"));
    }

    @Test
    void testExceptionWhenSecretNotFound() {
        stubSelfLookupSuccess();
        final String secret = "path/to/project/";
        stubFor(get("/v1/path/data/to/project/default").willReturn(notFound()));
        final EnvConfigVaultProperties vaultProperties = new EnvConfigVaultProperties("http://localhost:8999", "mock", "mock_token", secret);
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
                () -> new VaultConfiguration(vaultProperties).getConfiguration("default"));
        Assertions.assertEquals("Could not find the vault secret: path/to/project/default", exception.getMessage());
    }

    private void stubGetSecretSuccess() {
        stubFor(get("/v1/path/data/to/project/default").willReturn(okJson("{\n"
                + "  \"data\": {\n"
                + "    \"data\": {\n"
                + "       \"key1\": \"value1\",\n"
                + "       \"key2\": \"value2\"\n"
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
