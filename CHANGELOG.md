# CHANGELOG

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

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

- Updating to latest version of checkstyle and pmd

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

- Migrating to use github actions
- Publishing to both maven central and github packages

## 0.5.6

### Updated

- Updates to latest version of checkstyle/pmd.
- Updates the version of compiler plugin

## 0.5.5

### Fixed

- Updates the version of commons-configurations for security.

## 0.5.4

### Fixed

- Ignore leading and trailing spaces when gathering keys from keepass entries.

## 0.5.3

### Changed

- Now adds the raw environments as well as processed lowercased keys

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
