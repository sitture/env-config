## 1.3.2
### Changed
- Changes property `browser.name` to `browser.browsername`.

## 1.3.1
### Changed
- Now reads browser properties from `default/browser.properties` if `${env}/browser.properties` does not exist.

## 1.3.0
### Added
- Adds `browser.remote` and `browser.hub.url` properties to browser configuration.

## 1.2.0
### Added
- Adds `browser.headless` property to browser configuration.
### Changed
- Updates the repository details to artifactory.

## 1.1.0
### Added
- Allow setting of `ENV_DIR` environment variable to point at your env directory.

## 1.0.2
### Fixed
- Pass in processed env vars into the Configuration instead.
- Replaces camel cased configuration to property convention

## 1.0.1
### Fixed
- Config.getBool now returns false when property does not exist.

## 1.0.0
- Initial release of the package to manage config environments