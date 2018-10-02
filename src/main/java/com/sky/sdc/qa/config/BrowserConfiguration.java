package com.sky.sdc.qa.config;

import java.net.URL;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({ "file:${user.dir}/${env.dir}/${env}/browser.properties",
		"file:${user.dir}/${env.dir}/default/browser.properties",
		"file:${user.dir}/${env.dir}/${env}/${env}.properties", "file:${user.dir}/${env.dir}/${env}/default.properties",
		"file:${user.dir}/${env.dir}/default/default.properties" })
public interface BrowserConfiguration extends Config {

	@Key("browser.browsername")
	@DefaultValue("phantomjs")
	String getName();

	@Key("browser.location")
	@DefaultValue("${user.dir}/resources/drivers")
	String getLocation();

	@Key("browser.headless")
	@DefaultValue("false")
	boolean isHeadless();

	@Key("browser.remote")
	@DefaultValue("false")
	boolean isRemote();

	@Key("browser.hub.url")
	URL getHubUrl();

}
