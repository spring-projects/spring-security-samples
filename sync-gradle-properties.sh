#!/bin/sh

find . -mindepth 2 -name build.gradle | xargs -I {} sh -c "dirname {} | xargs cp gradle.properties"
