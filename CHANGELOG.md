# CHANGELOG

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

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
