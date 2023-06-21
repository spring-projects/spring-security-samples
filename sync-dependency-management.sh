#!/bin/bash

export VERSION=$1

if [[ "VERSION" == "" ]]; then
  echo "Missing version. Usage: sync-dependency-management.sh <version>"
  exit 1
fi
find . -mindepth 2 -name build.gradle | xargs sed -i '' -E "s/(id 'io.spring.dependency-management' version ')[^']+'/\1$VERSION'/"
