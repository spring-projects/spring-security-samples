#!/bin/bash

export BOM_VERSION=$1

if [[ "$BOM_VERSION" == "" ]]; then
  echo "Missing framework-bom-version. Usage: sync-framework-bom.sh <bom-version>"
  exit 1
fi
find . -mindepth 2 -name build.gradle | xargs sed -i '' -E "s/(\"org.springframework:spring-framework-bom:)[^:\"]+/\1$BOM_VERSION/"

