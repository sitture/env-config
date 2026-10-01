package com.github.sitture.envconfig;

import java.util.Map;
import org.apache.commons.configuration2.Configuration;

public interface EnvConfigConfiguration {

    Map<String, Configuration> getConfiguration();

}
