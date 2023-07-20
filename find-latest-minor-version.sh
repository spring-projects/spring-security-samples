#!/bin/bash

increment_version() {
    local version="$1"
    local last_digit=$(echo "$version" | rev | cut -d '.' -f 1 | rev)
    local incremented_digit=$((last_digit + 1))
    echo "${version%.*}.$incremented_digit"
}

find_next_minor_version() {
  local current_version=$1
  local maven_url=$2
  local next_version=$(increment_version "$current_version")
  local url="$maven_url/$next_version/"
  local response=$(curl --write-out "%{http_code}\n" --silent --output /dev/null "$url")

  if [ "$response" -eq 200 ]; then
    echo "$next_version"
  fi
}
