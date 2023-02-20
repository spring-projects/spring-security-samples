#!/bin/bash
#
# Copyright 2023 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
jq -h > /dev/null 2>&1
if [[ $? -ne 0 ]]; then
  echo jq not installed
  exit 1
fi
curl -h > /dev/null 2>&1
if [[ $? -ne 0 ]]; then
  echo curl not installed
  exit 1
fi

INSTALL_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
command -v cygpath > /dev/null && test ! -z "$MSYSTEM"
if [[ $? -eq 0 ]]; then
  INSTALL_DIR=$(cygpath -w "$INSTALL_DIR")
fi
# The supported version of CAS in the cas initializr changes over time so query the current value (for 6.6.x)
# Get valid combinations with this: curl -s https://casinit.herokuapp.com/actuator/info/ | jq '."supported-versions"[] | select(.branch == "6.6")'
CAS_VERSION=$(curl -s https://casinit.herokuapp.com/actuator/info/ | jq -r '."supported-versions"'[2].version)
BOOT_VERSION=$(curl -s https://casinit.herokuapp.com/actuator/info/ | jq -r '."supported-versions"'[2].bootVersion)
set -e
SERVER_DIR=${INSTALL_DIR}/cas-server
mkdir -p $SERVER_DIR
cd $SERVER_DIR
curl https://casinit.herokuapp.com/starter.tgz -d "dependencies=support-json-service-registry&casVersion=${CAS_VERSION}&bootVersion=${BOOT_VERSION}" | tar  -xzvf -
echo Building cas server
./gradlew build
mkdir -p ./etc/cas/config

echo Service Directory is ${INSTALL_DIR}/services
DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5555

java $DEBUG -jar build/libs/cas.war \
  --cas.standalone.configuration-directory=./etc/cas/config \
  --server.ssl.enabled=false \
  --server.port=8090 \
  --cas.service-registry.core.init-from-json=false \
  --cas.service-registry.json.location=file:${INSTALL_DIR}/services
