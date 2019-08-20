package com.github.sitture.env.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;

class EnvironmentVariables {

	private static Configuration systemConfiguration;
	private static Configuration environmentConfiguration;

	EnvironmentVariables() {
		systemConfiguration = new SystemConfiguration();
		environmentConfiguration = new MapConfiguration(getEnvMap());
	}

	Configuration getSystemConfiguration() {
		return systemConfiguration;
	}

	Configuration getEnvironmentConfiguration() {
		return environmentConfiguration;
	}

	private static Map<String, String> getEnvMap() {
		Map<String, String> envMap = new HashMap<String, String>();
		for (final Map.Entry<String, String> envVar : System.getenv().entrySet()) {
			envMap.put(getProcessedEnvKey(envVar.getKey()), envVar.getValue());
		}
		return envMap;
	}

	private static String getProcessedEnvKey(final String envVar) {
		return envVar.replaceAll("_", ".").toLowerCase();
	}

}
