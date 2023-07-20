#!/bin/bash

source ./find-latest-minor-version.sh
source ./versions.properties

next_minor_version=$(find_next_minor_version "$springBootVersion" "https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter")

if [ -z "$next_minor_version" ]; then
  echo "No new minor Spring Boot version found"
  exit 0
fi

sed -i '' -e "s/^\(springBootVersion\s*=\s*\).*$/\1$next_minor_version/" versions.properties
bash ./sync-boot-version.sh "$next_minor_version"
