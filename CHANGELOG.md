# CHANGELOG

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## 1.13.1

### Updated

- Excludes org.linguafranca.pwdb:util dependency

## 1.13.0

### Fixes

- Updates to jorabin/KeePassJava2 2.2.3 to resolve https://github.com/jorabin/KeePassJava2/issues/70

## 1.12.1

### Fixes 

- Fixes an issue with resolving config properties

## 1.12.0

### Updates

- Excluding commons-collections dependency to resolve vulnerabilities

## 1.11.0

### Updates

- [openkeepass](https://github.com/cternes/openkeepass) is now deprecated, 
  - Updates to use jorabin/KeePassJava2 for reading keepass files

## 1.10.0

- locks maven-jar plugin for reproducible builds
- Updates to use pluginManagement

## 1.9.0

### Updates

- Adds ability to specify a default secret path when loading from vault.

## 1.8.0

### Updates

- Updates to latest version of pmd plugin

## 1.7.0

### Updated

- Updates the Vault configuration to retry token validation which is prone to initial fail due to Vaults eventual consistency approach

## 1.6.0

### Added

- Adds reproducible builds configuration based on https://maven.apache.org/guides/mini/guide-reproducible-builds.html
- Adds support for reading secrets from Vault

## 1.5.0

### Updated

- Bump commons-configuration2 from 2.8.0 to 2.9.0
- Bump slf4j.version from 2.0.6 to 2.0.7
- Bump checkstyle from 10.6.0 to 10.9.3
- Bump maven-surefire-plugin from 3.0.0-M8 to 3.0.0
- Bump maven-compiler-plugin from 3.10.1 to 3.11.0

## 1.4.0

### Updated

- Adds additional test coverage for config precedence
- Updates for following Gauge specific scenarios:
  - when env var value and default config file value is same and does not contain in env config file then it should ignore the env var
  - when env var value and env config file value is same and does not exist in default then value from env var takes priority

## 1.3.0

### Updated

- Ignore env vars if key=value pair in env/properties are same

## 1.2.4

### Fixed

- Fixes an issue getting value from keepass when key in env and default properties are same

## 1.2.3

### Fixed

- System environment should now take precedence over keepass

## 1.2.2

### Fixed

- Removes duplicate exception from EnvConfigLoader

## 1.2.1

### Fixed

- Fixes an issue with returning value when using parent-env

## 1.2.0

### Updated

- Bump slf4j-api from 1.7.36 to 2.0.0

### Fixed

- Fixes an issue when env var and parent/default env values are same
- Fixes an issue with flaky unit tests

## 1.1.1

### Added

- Adding missing used undeclared dependency

## 1.1.0

### Added

- Adds a new configuration item `env.config.profiles.path` to load profiles from another directory.
- Now throws an EnvConfigException on non-existing `env.config.path` and `env.config.profiles.path`.

### Fixed

- Fixes an issue with default environment loading more than once.
- Resolve env.config.path only once at the start.

## 1.0.0

### Updated

- Updates package name from `com.github.sitture.env.config` to `com.github.sitture.envconfig`
- Updates the version of checkstyle to 10.3
- Updates the configuration properties prefix from `config.*` to `env.config.*`
  - `config.dir` to `env.config.path`
  - `config.env` to `env.config.environment`
  - `config.env.profile` to `env.config.profile`
  - `config.keepass.enabled` to `env.config.keepass.enabled`
  - `config.keepass.filename` to `env.config.keepass.filename`
  - `config.keepass.masterkey` to `env.config.keepass.masterkey`

## 0.10.1

### Fixed

- Fixes an issue with env var override when there's no parent environment

## 0.10.0

### Updated

- Upgrade minimal required jdk version to 11
- Updates Unit tests to use Junit5 and system-stubs

## 0.9.1

### Updated

- Updates maven plugin dependency versions

## 0.9.0

### Updated

- Adds the ability to specify relative and absolute config dir paths

## 0.8.4

### Fixed

- Fixes an issue with keepass opening multiple times

## 0.8.3

### Fixed

- Changes the way that we get the filename to fix an issue in Windows

## 0.8.2

### Updated

- Updating to the latest version of checkstyle and pmd

## 0.8.1

### Fixed

- Fixes an issue with getting properties from parent environments with key format of my.property.

## 0.8.0

### Added

- Adds the ability to load in profiles using `CONFIG_ENV_PROFILE`

## 0.7.2

### Updated

- Adds ability to show the configuration asMap

## 0.7.1

### Fixed

- Fixes an issue with env vars priority

## 0.7.0

### Added

- Adds the ability to supply multiple environments. E.g. `config.env=env1,env2` where env2 takes precedence.

## 0.6.1

### Updated

- Bumps junit from 4.12 to 4.13.1

## 0.6.0

### Added

- Adds a method to get a list of delimiter/comma separated items from a key. `EnvConfig.getList(property)`

## 0.5.9

### Updated

- Updates javadoc for `getOrThrow(property)` method.

### Fixed

- Fixes an issue with trailing spaces in env name.

## 0.5.8

### Updated

- Updates method to `getOrThrow(property)` for getting a required property.

## 0.5.7

### Updated

- Migrating to use GitHub actions
- Publishing to both maven central and GitHub packages

## 0.5.6

### Updated

- Updates to the latest version of checkstyle/pmd.
- Updates the version of compiler plugin

## 0.5.5

### Fixed

- Updates the version of commons-configurations for security.

## 0.5.4

### Fixed

- Ignore leading and trailing spaces when gathering keys from keepass entries.

## 0.5.3

### Changed

- Now adds the raw environments as well as processed lowercase keys

E.g. TEST_VAR can either be retrieved with `EnvConfig.get("TEST_VAR")` or `EnvConfig.get("test.var")`

## 0.5.2

### Fixed

- Env vars now loaded after the keePass configurations.

### Changed

- KeePass title instead of username is now used as the key in the map.

## 0.5.1

### Changed

- Now adds the property from keepass configuration keys in original format as well as lower-cased.

E.g. For property `TEST_PROPERTY`, should now be able to get by `EnvConfig.get("TEST_PROPERTY")` or `EnvConfig.get("test.property")`.

- Keepass properties now take precedence over the environment variables.

## 0.5.0

### Added

- Ability to read [keepass](https://keepass.info) database file.

## 0.4.0

### Added

- Ability to clear a specified property with `EnvConfig.clear(...)`

## 0.3.0

### Added

- Adds the ability to set/update an existing property with `EnvConfig.set(...)`

## 0.2.0

### Added

- Adds the `EnvConfig.add(...)` method to add key/value pairs into the config

## 0.1.0

### Added

- Initial release of the package to manage environment configs
