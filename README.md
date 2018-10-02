# env-config

A simple utility to manage environment configs in Java-based projects by merging `*.properties` files and environment variables overrides.

All notable changes to this project are documented in [CHANGELOG.md](CHANGELOG.md).
The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Usage

Add the following dependency to use this package:

```bash
<dependency>
    <groupId>com.sitture</groupId>
    <artifactId>env-config</artifactId>
    <version>${version}</version>
</dependency>
```

```bash
com.sitture:env-config:${version}'
```

To start using this:

1. add a directory called `config` in project root.
2. create a `default` environment subdirectory under `config`
3. create a `default.properties` file in the `default` directoy. E.g. `config/default/default.properties`

> You can create as many environments as needed.

To get current environment:

```java
EnvConfig.getEnvironment();
```

To get a property set either in the properties file, system property or environment variable:

```java
EnvConfig.get("my.property");
EnvConfig.getInt("my.property");
EnvConfig.getBool("my.property");
// when a property is required to continue
EnvConfig.get("my.property", true);
// return a default value when a property isn't found
EnvConfig.get("my.property", "defaultValue");
```

### Default Configuration Examples

```java
// driver specific properties,
EnvConfig.driver().getEnvironment();
// browser specific
EnvConfig.browser().getName();
// appium specific
EnvConfig.appium().getPlatform();
```
