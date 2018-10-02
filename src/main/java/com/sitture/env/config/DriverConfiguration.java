package com.sitture.env.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({ "file:${user.dir}/${env.dir}/${env}/${env}.properties",
		"file:${user.dir}/${env.dir}/${env}/default.properties",
		"file:${user.dir}/${env.dir}/default/default.properties" })
public interface DriverConfiguration extends Config {

	@Key("driver.environment")
	@DefaultValue("web")
	String getEnvironment();

	@Key("driver.timeout")
	@DefaultValue("15")
	int getTimeout();

}
