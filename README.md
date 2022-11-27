# env-config

A simple utility to manage environment configs in Java-based projects by merging `*.properties` files with environment variables overrides.

![Build](https://github.com/sitture/env-config/workflows/Build/badge.svg) ![Github Publish](https://github.com/sitture/env-config/workflows/Github%20Publish/badge.svg) ![Maven Publish](https://github.com/sitture/env-config/workflows/Maven%20Publish/badge.svg) [![Maven Central](https://img.shields.io/maven-central/v/com.github.sitture/env-config.svg)](https://mvnrepository.com/search?q=com.github.sitture) [![Maintainability](https://api.codeclimate.com/v1/badges/338645e6d3c853fcb93e/maintainability)](https://codeclimate.com/github/sitture/env-config/maintainability) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?maxAge=2592000)](https://opensource.org/licenses/MIT) [![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](../../issues)

All notable changes to this project are documented in [CHANGELOG.md](CHANGELOG.md).
The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Setup

Add the following dependency to use this EnvConfig:

### Maven

```xml
<dependency>
    <groupId>com.github.sitture</groupId>
    <artifactId>env-config</artifactId>
    <version>${version}</version>
</dependency>
```

### Github Packages

If you would like to use github package instead of maven central, add the following repository to pom.xml.

```xml
<repositories>
  <repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/sitture/env-config</url>
  </repository>
</repositories>
```

### Gradle

```groovy
compile 'com.github.sitture:env-config:${version}'
```

## Configuration

| system property                | environment variable           | description                                                                                                                                               |
|--------------------------------|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `env.config.path`              | `ENV_CONFIG_PATH`              | The base directory where the configuration files are lived. **default:** `config` directory under the project.                                            |
| `env.config.environment`       | `ENV_CONFIG_ENVIRONMENT`       | The environment to activate. **default:** `default` directory under the base configuration directory.                                                     |
| `env.config.profiles.path`     | `ENV_CONFIG_PROFILES_PATH`     | The base directory where the profile based configuration files are lived. **default:** `${env.config.path}/${env.config.environment}/`                    |
| `env.config.profile`           | `ENV_CONFIG_PROFILE`           | The profile to activate from the active environment directory.                                                                                            |
| `env.config.keepass.enabled`   | `ENV_CONFIG_KEEPASS_ENABLED`   | Whether to load properties from a keepass file. **default:** `false`                                                                                      |
| `env.config.keepass.filename`  | `ENV_CONFIG_KEEPASS_FILENAME`  | The keepass filename to load from the resources folder (src/main/resources). **default:** the root project directory name. i.e. `project.build.directory` |
| `env.config.keepass.masterkey` | `ENV_CONFIG_KEEPASS_MASTERKEY` | The password to open the keepass file. This is required if `env.config.keepass.enabled=true`.                                                             |                                                                

## Configuration precedence

1. Java System properties - `System.getProperties()`
2. OS environment variables - `System.getenv()`
3. Environment profile properties - `config/${env.config.environment}/${env.config.profile}/*.properties`
4. Default profile properties - `config/default/${env.config.profile}/*.properties`
5. Environment specific properties - `config/${env.config.environment}/*.properties`
6. Default properties - `config/default/*.properties`

## Usage

To start using this:

### `config` directory

The default required directory for configuration files in `config` under project root. This can be overridden by `ENV_CONFIG_PATH` environment variable.

* create a directory called `config` in project root.

### `config` environments

The default environment is set to `default` and can be overridden by `ENV_CONFIG_ENVIRONMENT` environment variable.

1. create a `default` environment subdirectory under `config` directory.
2. create a `default.properties` file in the `default` directory. E.g. `config/default/default.properties`

```bash
# formatted as key=value
my.first.property=my_first_value
my.second.property=my_second_value
```

You can add multiple `.properties` files under environment directory. E.g. You may want to split the properties into:

```text
.
├── config
│   └── default
│       ├── default.properties
│       └── db.properties
```

>You can create as many environments as needed.

```text
.
├── config
│   └── default
│       ├── default.properties
│       └── db.properties
│   └── integration
│       └── integration.properties
```

### `config` profiles

You can also have config profiles within an environment directory by specifying the `ENV_CONFIG_PROFILE=profile1` variable E.g.

```text
.
├── config
│   └── default
│       └── profile1
│           └── profile1.properties
│       └── profile2
│           └── profile2.properties
│       └── default.properties
│   └── integration
│       └── profile1
│           └── profile1.properties
│       └── integration.properties
```

If `ENV_CONFIG_ENVIRONMENT=integration` and `ENV_CONFIG_PROFILE=profile1` suggests to load properties in the following order:

1. `integration/profile1/profile1.properties`
2. `default/profile1/profile1.properties`
3. `integration/integration.properties`
4. `default/default.properties`

### base environments

You can base an environment based on another by specifying multiple environment in `ENV_CONFIG_ENVIRONMENT` environment variable.

E.g. if you would like `env2` environment to inherit properties from `env2` environment:

```shell
ENV_CONFIG_ENVIRONMENT=env1,env2
```

The above will load environment properties from env2 on top of env1 and finally the default properties from default environment.

### `KeePass` Database Entries

If you have secret passwords which cannot be stored as plain text within project repository, you can store them into a password-protected [KeePass](https://keepass.info/) database file.

1. create a keepass database file, add to your resources folder. i.e. `src/main/resources` or `src/test/resources`.

#### Configurations

* `ENV_CONFIG_KEEPASS_ENABLED` - A flag to enable reading of the keePass file. Default is set to `false`.
* `ENV_CONFIG_KEEPASS_FILENAME` - This is the name of the DB file. Default is the name of project directory.
* `ENV_CONFIG_KEEPASS_MASTERKEY` - The key to access the DB file.

#### KeePass Groups

* The top level group should have the same name as the DB filename. e.g. if DB file is `keepass.kdbx` then top level group should be `keepass`.
* The sub-groups should match with the environment directory you have created above. For example, you should have `default` group for the default environment.
* The entries within the `default` group will be shared across all environments similar to the environment directories behaviour.

### Environment priority

The `EnvConfig` will go through properties set under your environment and then load properties from default environment ignoring the ones already set. You can keep the shared properties under your `default` environment without having to repeat them in every other environment.

### Current environment

You can get the current environment by:

```java
EnvConfig.getEnvironment();
```

### Get property `EnvConfig.get("...")`

To get a property set either in the properties file, system property or environment variable:

```java
EnvConfig.get("my.property");
EnvConfig.getInt("my.property");
EnvConfig.getBool("my.property");
EnvConfig.getList("my.property"); // will return a List<String> from a comma separated String.
```

### Get `required` property

```java
// when a property is required to continue
EnvConfig.getOrThrow("my.property");
```

If the property isn't set then a `EnvConfigException` is thrown.

### Get property with `defaultValue`

```java
// return a default value when a property isn't found
EnvConfig.get("my.property", "defaultValue");
```

__Note:__ All the environment variable names are set to properties naming convention.
E.g. `MY_ENV_VAR` can either be accessed by `EnvConfig.get("my.env.var");` or `EnvConfig.get("MY_ENV_VAR");`

### Property overrides

You can override any property set in the environment properties file by setting an system environment variable.

E.g. `my.env.property` can be overridden by `MY_ENV_PROPERTY` environment variable.

### Add property `EnvConfig.add("...")`

You can add key/value pairs to the EnvConfig to be accessed somewhere else in the project.

```java
EnvConfig.add("my.property", "my_value");
```

### Set property `EnvConfig.set("...")`

You can set/update an existing property in EnvConfig:

```java
EnvConfig.set("my.property", "my_value");
```

The `.set(...)` can be used for both existing and non-existing properties.

### Clear property `EnvConfig.clear("...")`

You can clear an existing property in EnvConfig:

```java
EnvConfig.clear("my.property")
```

### Get all `EnvConfig.asMap()`

You can get a full list of available properties with `EnvConfig.asMap()` which is a combination of properties from `config` directory, system properties and all environment variables.

## Issues & Contributions

Please [open an issue here](../../issues) on GitHub
if you have a problem, suggestion, or other comment.

Pull requests are welcome and encouraged! Any contributions should include new or updated unit tests as necessary to maintain thorough test coverage.

Read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.
