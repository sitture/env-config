# sdc-qa-config

This project adheres to [Semantic Versioning](https://semver.org). Given a version number __MAJOR.MINOR.PATCH__, increment the:

* __MAJOR__ version when you make incompatible API changes,
* __MINOR__ version when you add functionality in a backwards-compatible manner, and
* __PATCH__ version when you make backwards-compatible bug fixes.

## Usage

Add the following dependency to use this package:

```bash
<dependency>
    <groupId>com.sky.sdc.qa</groupId>
    <artifactId>sdc-qa-config</artifactId>
    <version>${version}</version>
</dependency>
```

```bash
com.sky.sdc.qa:sdc-qa-config:${version}'
```

To start using this:

1. add a directory called `config` in project root.
2. create a `default` environment subdirectory under `config`
3. create a `default.properties` file in the `default` directoy. E.g. `config/default/default.properties`

> You can create as many environments as needed.

To get current environment:

```java
Config.getEnvironment();
```

To get a property set either in the properties file, system property or environment variable:

```java
Config.get("my.property");
Config.getInt("my.property");
Config.getBool("my.property");
// when a property is required to continue
Config.get("my.property", true);
// return a default value when a property isn't found
Config.get("my.property", "defaultValue");
```

### Default Configuration Examples

```java
// driver specific properties,
Config.driver().getEnvironment();
// browser specific
Config.browser().getName();
// appium specific 
Config.appium().getPlatform();
```
