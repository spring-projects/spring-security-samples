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
docker --version > /dev/null 2>&1
if [[ $? -ne 0 ]]; then
  echo docker not installed
  exit 1
fi
INSTALL_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
command -v cygpath > /dev/null && test ! -z "$MSYSTEM"
if [[ $? -eq 0 ]]; then
  INSTALL_DIR=$(cygpath -w "$INSTALL_DIR")
fi

CAS_VERSION=6.6.6
# Note that the default apereo/cas image doesn't contain the modules you will need
# You need to start with an cas overlay template and build your own image with the modules you want to use.
# See https://github.com/apereo/cas-overlay-template or https://github.com/apereo/cas-initializr
docker run -d -p 8090:8080 --name cas -v $INSTALL_DIR/services:/etc/cas/services --rm apereo/cas:$CAS_VERSION \
  --cas.standalone.configuration-directory=/etc/cas/config \
  --server.ssl.enabled=false \
  --server.port=8080 \
  --cas.service-registry.core.init-from-json=false \
  --cas.service-registry.json.location=file:/etc/cas/services

docker logs cas -f
