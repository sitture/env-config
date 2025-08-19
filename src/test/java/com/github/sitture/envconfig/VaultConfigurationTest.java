package com.github.sitture.envconfig;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.valfirst.slf4jtest.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.github.jopenlibs.vault.VaultException;
import java.util.function.Predicate;
import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;


@SuppressWarnings("PMD.TooManyStaticImports")
@WireMockTest(httpPort = 8999)
class VaultConfigurationTest {

    @Test
    void testCanGetConfigurationMapWithData() {
        stubSelfLookupSuccess();
        stubReadSecretSuccess();
        final EnvConfigVaultProperties vaultProperties = getMockVaultProperties();
        final Configuration configuration = getVaultConfiguration(vaultProperties, "default");
        Assertions.assertEquals("value1", configuration.getString("key1"));
        Assertions.assertEquals("value2", configuration.getString("key2"));
    }

    @Test
    void testExceptionWhenSecretNotFound() {
        stubSelfLookupSuccess();
        stubFor(get("/v1/path/data/to/project/default").willReturn(notFound()));
        final EnvConfigVaultProperties vaultProperties = getMockVaultProperties();
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> getVaultConfiguration(vaultProperties, "default"));
        Assertions.assertEquals("Could not find the vault secret: path/to/project/default", exception.getMessage());
    }

    @Test
    void testExceptionNotThrownWhenEnvNotDefault() {
        stubSelfLookupSuccess();
        final EnvConfigVaultProperties vaultProperties = getMockVaultProperties();
        Assertions.assertDoesNotThrow(() -> getVaultConfiguration(vaultProperties, "test"));
    }

    @Test
    void testExceptionWhenCannotReadSecret() {
        stubSelfLookupSuccess();
        stubFor(get("/v1/path/data/to/project/default").willReturn(serverError()));
        final EnvConfigVaultProperties vaultProperties = getMockVaultProperties();
        final EnvConfigException exception = Assertions.assertThrows(EnvConfigException.class,
            () -> getVaultConfiguration(vaultProperties, "default"));
        Assertions.assertEquals("Could not read data from vault.", exception.getMessage());
    }

    @Test
    void testRetriesWhenSelfLookupFails() {
        stubSelfLookupFailure();
        final EnvConfigVaultProperties vaultProperties = getMockVaultProperties();
        final TestLogger testLogger = TestLoggerFactory.getTestLogger(VaultConfiguration.class);

        final EnvConfigException exception = Assertions.assertThrows(
            EnvConfigException.class, () -> getVaultConfiguration(vaultProperties, "default"));

        Assertions.assertEquals("Reached CONFIG_VAULT_VALIDATE_MAX_RETRIES limit (2) attempting to validate token", exception.getMessage());
        Assertions.assertEquals(412, ((VaultException) exception.getCause()).getHttpStatusCode());
        Assertions.assertEquals("Vault responded with HTTP status code: 412\nResponse body: ", exception.getCause().getMessage());

        final Predicate<LoggingEvent> errorWithVault412Throwable = event -> event.getLevel().equals(Level.ERROR)
            && event.getThrowable().isPresent()
            && "Vault responded with HTTP status code: 412\nResponse body: ".equals(event.getThrowable().get().getMessage());

        assertThat(testLogger).hasLogged(errorWithVault412Throwable.and(
            event -> "An exception occurred validating the vault token, will retry in 0 seconds".equals(event.getMessage())));
        assertThat(testLogger).hasLogged(errorWithVault412Throwable.and(
            event -> "An exception occurred validating the vault token, will retry in 2 seconds".equals(event.getMessage())));
        assertThat(testLogger).hasLogged(errorWithVault412Throwable.and(
            event -> "Reached CONFIG_VAULT_VALIDATE_MAX_RETRIES limit (2) attempting to validate token".equals(event.getMessage())));
    }

    private Configuration getVaultConfiguration(final EnvConfigVaultProperties vaultProperties, final String env) {
        return new VaultConfiguration(vaultProperties).getConfiguration(env, vaultProperties.getSecretPath());
    }

    private EnvConfigVaultProperties getMockVaultProperties() {
        return new EnvConfigVaultProperties("http://localhost:8999", "mock", "mock_token", "path/to/project/", null, 2);
    }

    private void stubReadSecretSuccess() {
        stubFor(get("/v1/path/data/to/project/default").willReturn(okJson("""
            {
              "data": {
                "data": {
                   "key1": "value1",
                   "key2": "value2"
                }
              }
            }
            """)));
    }

    private void stubSelfLookupFailure() {
        stubFor(get("/v1/auth/token/lookup-self").willReturn(aResponse().withStatus(412)));
    }

    private void stubSelfLookupSuccess() {
        stubFor(get("/v1/auth/token/lookup-self").willReturn(okJson("""
            {
              "data": {
                "policies": [
                  "default"
                ]
              }
            }
            """)));
    }
}
