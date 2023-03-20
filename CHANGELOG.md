# Changelog

[TOC]

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [4.3.13] - 2023-03-21

### Changed

- Issue #201: Changed circles

## [4.3.12] - 2023-03-16

### Added

- Issue #199: Add more modules


## [4.3.11] - 2023-03-15

### Added

- Issue #197: Add site roundabouts

## [4.3.10] - 2022-12-12

### Added

- Issue #187: Generate connections

### Changed

- Issue #136: Weighted center in star builder
- Issue #186: Optimize simulation for speed
- Issue #188: Improve scroll and zoom

### Fixed

- Issue #189: Fix load and save frequence

## [4.3.9] - 2022-10-21

### Changed

- Issue #182: Modules upgrade

### Security

- Issue #183: Jackson library upgrade

## [4.3.8] - 2021-12-26

### Fixed
 
- Issue #153: Verify computation of simulation speed

## [4.3.7] - 2021-12-20

### Changed

- Issue #172: Predict transit time in congested traffic conditions
- Issue #173: First vehicle priority on cross
- Issue #174: Change the frequencies

## [4.3.6] - 2021-12-19

### Added

- Issue #162: Compute # vehicles hold on node

### Changed

- Issue #166: Set the frequency ratio to 2 in frequency panel

### Fixed

- Issue #164: Change frequency in map profile
- Issue #167: Verify first vehicle priority on cross

## [4.3.5] - 2021-11-27

### Changed

- Issue #159: set frequency to max 0.5 vehicles/sec
- Issue #160: change the curve modules

## [4.3.4] - 2021-11-26

### Added

- Issue #54: Modify edge with mouse
- Issue #156: Curve modules

### Fixed

- Issue #157: Verify modules' junctions

## [4.3.3] - 2021-11-25

### Added

- Issue #152: More modules and standardized the old ones

### Fixed

- Issue #127: Entry to a site destination must be prioritized (repoened)
- Issue #153: Verify computation of simulation speed
- Issue #154: Reset selection at creation, loading of a new map

## [4.3.2] - 2021-11-23

### Changed

- Issue #147: Improve snap to node precision
- Issue #148: Improve readability of status bar
- Issue #149: Transparent dropping 
- Issue #150: Increase to 8000 the max num of vehicles
  
## [4.3.1] - 2021-11-23

### Fixed

- Issue #127: Entry to a site destination must be prioritized
- Issue #128: Change to node edge should not change the map scale factor
- Issue #134: Hang on traffic stuck
- Issue #135: Module drop does not stick to existing nodes
- Issue #136: Reset scale and the sim speed when focus changed
- Issue #138: Wrong max vehecles when load
- Issue #140: Flicker of info pane
- Issue #141: Optimization creates double vehicles
- Issue #144: Error dropping edges from site to site in new random map
- Issue #145: The traffic color is wrong

### Added

- Issue #129: Split simulation thread from rendering thread
- Issue #130: Ring road module

## [4.3.0] - 2021-11-10

### Changed

- Issue #125: Functions for user interactions
- Issue #122: Immutable model, add ring road module

## [4.2.2] - 2021-10-04

### Fixed
 
- Issue #121: Selection on explorer panel
- Issue #123: Fit random map to size of map

## [4.2.1] - 2021-10-03

### Changed

- Issue #114: Update Pom
- Issue #116: Fix pom
- Issue #118: Reactive UI
 
## [4.2.0] - 2019-12-08

### Changed

- Issue #14: New round modules

## [4.1.0] - 2019-12-01

### Fixed

- Issue #8: Missing alert pane when save to access denied file
- Issue #9: wrong xml file suffix in file chooser
- Issue #12: Site defined twice both in sites and nodes during saving

### Changed

- Issue #10: Optimize modules

## [4.0.0] - 2019-11-26

### Changed

- Issue #1: move project folder to base path

### Added

- Issue #5: YAML format

### Removed

- XML file format

## [3.7.1] - 2013-10-14

### Added

- Maven

## [3.7.0] - 2013-06-26

### Added

- Base project
