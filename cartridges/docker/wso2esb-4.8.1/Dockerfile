# ------------------------------------------------------------------------
#
# Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

FROM wso2/base-image:4.1.0
MAINTAINER dev@wso2.org

ENV DEBIAN_FRONTEND noninteractive
ENV WSO2_SERVER_TYPE esb
ENV WSO2_SERVER_VERSION 4.8.1
ENV TEMPLATE_MODULE_NAME wso2${WSO2_SERVER_TYPE}-${WSO2_SERVER_VERSION}-template-module-${PPAAS_VERSION}

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY scripts/init.sh /usr/local/bin/init.sh
COPY packages/${TEMPLATE_MODULE_NAME}.zip /opt/
COPY packages/wso2${WSO2_SERVER_TYPE}-${WSO2_SERVER_VERSION}.zip /opt/
COPY plugins ${PCA_HOME}/plugins

# -------------------------------
# Unzip and move template modules
# -------------------------------
WORKDIR /opt

RUN unzip /opt/${TEMPLATE_MODULE_NAME}.zip -d ${CONFIGURATOR_HOME}/template-modules && \
    rm -rf /opt/${TEMPLATE_MODULE_NAME}.zip

# ----------------------
# Expose container ports
# ----------------------

# ESB mgt-http/mgt-https/pt-http/pt-https ports
EXPOSE 9763 9443 8280 8243

# -------------------------------
# Define entry point & start sshd
# -------------------------------

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log 2>&1 | /usr/sbin/sshd -D
