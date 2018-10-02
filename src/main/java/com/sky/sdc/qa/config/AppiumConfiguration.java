package com.sky.sdc.qa.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;

@LoadPolicy(LoadType.MERGE)
@Sources({ "file:${user.dir}/${env.dir}/${env}/device.properties",
		"file:${user.dir}/${env.dir}/${env}/${env}.properties", "file:${user.dir}/${env.dir}/${env}/default.properties",
		"file:${user.dir}/${env.dir}/default/default.properties" })
public interface AppiumConfiguration extends Config {

	@Key("platform")
	String getPlatform();

	@Key("platform.version")
	String getPlatformVersion();

	@Key("device.name")
	String getDeviceName();

	@Key("device.id")
	String getDeviceID();

	@Key("automation.name")
	String getAutomationName();

	@Key("application.path")
	String getApplicationPath();

	@Key("application.package")
	String getApplicationPackage();

	@Key("organisation.id")
	@DefaultValue("GJ24C8864F")
	String getOrganisationID();

	@Key("signing.id")
	@DefaultValue("iPhone Developer")
	String getSigningID();

	@Key("full.reset")
	@DefaultValue("false")
	boolean isFullReset();

	@Key("no.reset")
	@DefaultValue("true")
	boolean isNoReset();

	@Key("chromedriver.executable")
	@DefaultValue("./chromedriver")
	String getChromedriverExecutable();

	@Key("simulator")
	@DefaultValue("false")
	boolean isSimulator();

	@Key("language")
	@DefaultValue("en")
	String getLanguage();

	@Key("locale")
	@DefaultValue("en_GB")
	String getLocale();

	@Key("avd.args")
	String getAVDArgs();

}
