#!/usr/bin/env bash

find . -mindepth 2 -name "build.gradle*" | xargs dirname | xargs -I{} sh -c "cp './gradlew' {}; cp ./gradlew.bat {} ; cp -r gradle/wrapper {}/gradle/"
