#!/usr/bin/env bash

find -name "build.gradle*" | sed -E 's#\.?/#:#g' | sed 's#:build.gradle.*$##' | xargs -I{} echo "include \"{}\"" | sort