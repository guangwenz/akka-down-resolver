#!/bin/bash
set -e

git config --global user.email "zgwmike@hotmail.com"
git config --global user.name "Guangwen Zhou"
git config --global push.default simple

sbt ++$TRAVIS_SCALA_VERSION docs/publishMicrosite