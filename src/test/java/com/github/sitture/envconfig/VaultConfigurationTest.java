package com.github.sitture.envconfig;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

@WireMockTest(httpPort = 8999)
class VaultConfigurationTest {

    @Test
    void testCanGetConfigurationMapWithData() {
        stubSelfLookupSuccess();
        final String secret = "foo/bar";
        stubGetSecretSuccess();
        final EnvConfigVaultProperties vaultProperties = new EnvConfigVaultProperties("http://localhost:8999", "mock", "mock_token", secret);
        final Configuration configuration = new VaultConfiguration(vaultProperties).getConfiguration(secret);
        Assertions.assertEquals("value1", configuration.getString("key1"));
        Assertions.assertEquals("value2", configuration.getString("key2"));
    }

    private void stubGetSecretSuccess() {
        stubFor(get("/v1/foo/data/bar").willReturn(okJson("{\n"
                + "  \"data\": {\n"
                + "    \"data\": {\n"
                + "       \"key1\": \"value1\",\n"
                + "       \"key2\": \"value2\"\n"
                + "    },\n"
                + "    \"metadata\": {\n"
                + "      \"created_time\": \"2022-02-08T16:45:00.066783936Z\",\n"
                + "      \"custom_metadata\": null,\n"
                + "      \"deletion_time\": \"\",\n"
                + "      \"destroyed\": false,\n"
                + "      \"version\": 1\n"
                + "    }\n"
                + "  },\n"
                + "  \"wrap_info\": null,\n"
                + "  \"warnings\": null,\n"
                + "  \"auth\": null\n"
                + "}\n")));
    }

    private void stubSelfLookupSuccess() {
        stubFor(get("/v1/auth/token/lookup-self").willReturn(okJson("{\n"
                + "  \"data\": {\n"
                + "    \"policies\": [\n"
                + "      \"default\"\n"
                + "    ]\n"
                + "  },\n"
                + "  \"auth\": null\n"
                + "}")));
    }
}
