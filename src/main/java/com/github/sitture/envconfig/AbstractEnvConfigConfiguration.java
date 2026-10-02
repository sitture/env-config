package com.github.sitture.envconfig;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.configuration2.Configuration;

abstract class AbstractEnvConfigConfiguration implements EnvConfigConfiguration {

    private final List<String> environments;

    protected AbstractEnvConfigConfiguration(final List<String> environments) {
        this.environments = environments;
    }

    @Override
    public Map<String, Configuration> getConfiguration() {
        return this.environments.stream()
            .collect(Collectors.toMap(Function.identity(), this::getConfigurationForEnvironment));
    }

    protected abstract Configuration getConfigurationForEnvironment(String env);
}

