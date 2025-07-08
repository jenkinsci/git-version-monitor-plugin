# Git version node monitor plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/git-version-monitor-plugin/main)](https://ci.jenkins.io/job/plugins/job/git-version-monitor-plugin-plugin/)
[![Coverage](https://ci.jenkins.io/job/Plugins/job/git-version-monitorgit-version-monitor-plugin/job/main/badge/icon?status=${instructionCoverage}&subject=coverage&color=${colorInstructionCoverage})](https://ci.jenkins.io/job/Plugins/job/git-version-monitor-plugin/job/main)
[![LOC](https://ci.jenkins.io/job/Plugins/job/git-version-monitor-plugin/job/main/badge/icon?job=test&status=${lineOfCode}&subject=line%20of%20code&color=blue)](https://ci.jenkins.io/job/Plugins/job/git-version-monitor-plugin/job/main)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/git-version-monitor-plugin.svg)](https://plugins.jenkins.io/git-version-monitor-plugin/)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/git-version-monitor-plugin.svg?label=changelog)](https://github.com/jenkinsci/git-version-monitor-plugin/releases/latest)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/git-version-monitor-plugin.svg)](https://github.com/jenkinsci/git-version-monitor-plugin/graphs/contributors)


## Introduction

A Jenkins plugin that add a new node monitor column for the Git version of the node.

Only the `git` binary present on the PATH is used to determine the Git version.

If you are using automatic tool installers with [Git plugin](https://plugins.jenkins.io/git/) the git version used might not be the one used by the node during checkout operations.

Future version of this plugin could support version comparison and disconnect nodes with outdated (or missing) Git versions.

![](docs/nodes.png)

## How to use

Just install the plugin, noo configuration is needed

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)
