#!/bin/bash

export BOOT_VERSION=$1

if [[ "$BOOT_VERSION" == "" ]]; then
  echo "Missing boot-version. Usage: sync-boot-version.sh <boot-version>"
  exit 1
fi
find . -mindepth 2 -name build.gradle | xargs sed -i '' -E "s/(id 'org.springframework.boot' version ')[^']+'/\1$BOOT_VERSION'/"
