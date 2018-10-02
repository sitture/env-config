# env-config

A simple utility to manage environment configs in Java-based projects by merging `*.properties` files and environment variables overrides.

All notable changes to this project are documented in [CHANGELOG.md](CHANGELOG.md).
The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Setup

Add the following dependency to use this EnvConfig:

### Maven
```xml
<dependency>
    <groupId>com.sitture</groupId>
    <artifactId>env-config</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

```groovy
compile 'com.sitture:env-config:${version}'
```

## Usage

To start using this:

### `config` directory

The default required directory for configuration files in `config` under project root. This can be overridden by `CONFIG_DIR` environment variable.

* create a directory called `config` in project root.

### `config` environments

The default environment is set to `default` and can be overridden by `CONFIG_ENV` environment variable.

1. create a `default` environment subdirectory under `config` directory.
2. create a `default.properties` file in the `default` directory. E.g. `config/default/default.properties`

```bash
# formatted as key=value
my.first.property=my_first_value
my.second.property=my_second_value
```

You can add multiple `.properties` files under environment directory. E.g. You may want to split the property files into:

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
// when a property is required to continue
EnvConfig.get("my.property", true);
// return a default value when a property isn't found
EnvConfig.get("my.property", "defaultValue");
```

>__Note:__ All the environment variable names are set to properties naming convention. E.g. `MY_ENV_VAR` can be accessed by `EnvConfig.get("my.env.var");`.

### Property overrides

You can override any property set in the environment properties file by setting an system environment variable.

E.g. `my.env.property` can be overridden by `MY_ENV_PROPERTY` environment variable.

## Issues & Contributions

Please [open an issue here](../../issues) on GitHub if you have a problem, suggestion, or other comment.

Pull requests are welcome and encouraged! Any contributions should include new or updated unit tests as necessary to maintain thorough test coverage.

Read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.