#!/bin/bash

export VERSION=$1

if [[ "VERSION" == "" ]]; then
  echo "Missing version. Usage: sync-nebula-integtest.sh <version>"
  exit 1
fi
find . -mindepth 2 -name build.gradle | xargs sed -i '' -E "s/(id 'nebula.integtest' version ')[^']+'/\1$VERSION'/"
