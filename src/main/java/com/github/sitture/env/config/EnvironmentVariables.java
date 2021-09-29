package com.github.sitture.env.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;

import java.util.HashMap;
import java.util.Map;

import static com.github.sitture.env.config.utils.PropertyUtils.getProcessedEnvKey;

class EnvironmentVariables {

	private final Configuration systemConfiguration;
	private final Configuration environmentConfiguration;

	EnvironmentVariables() {
		systemConfiguration = new SystemConfiguration();
		environmentConfiguration = new MapConfiguration(getEnvMap());
	}

	protected Configuration getSystemConfiguration() {
		return systemConfiguration;
	}

	protected Configuration getEnvironmentConfiguration() {
		return environmentConfiguration;
	}

	private static Map<String, String> getEnvMap() {
		final Map<String, String> envMap = new HashMap<>();
		System.getenv().entrySet().forEach(entry -> {
			envMap.put(entry.getKey(), entry.getValue());
			envMap.put(getProcessedEnvKey(entry.getKey()), entry.getValue());
		});
		return envMap;
	}

}
