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

ENV WSO2_AM_VERSION 1.8.0
ENV AM_TEMPLATE_MODULE_NAME wso2am-${WSO2_AM_VERSION}-template-module-${PPAAS_VERSION}
ENV CARBON_HOME /opt/wso2am-${WSO2_AM_VERSION}

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY scripts/init.sh /usr/local/bin/init.sh
COPY packages/${AM_TEMPLATE_MODULE_NAME}.zip /opt/
COPY packages/wso2am-${WSO2_AM_VERSION}.zip /opt/
COPY plugins ${PCA_HOME}/plugins

# -----------------------------
# Install prerequisites and AM
# -----------------------------
WORKDIR /opt

RUN unzip /opt/wso2am-${WSO2_AM_VERSION}.zip -d /opt/ && \
    rm /opt/wso2am-${WSO2_AM_VERSION}.zip && \
    unzip /opt/${AM_TEMPLATE_MODULE_NAME}.zip -d ${CONFIGURATOR_HOME}/template-modules && \
    rm -rf /opt/${AM_TEMPLATE_MODULE_NAME}.zip

# ----------------------
# Expose container ports
# ----------------------

# AM https port
EXPOSE 9443
# AM http pass-through transport port
EXPOSE 8280
# AM https pass-through transport port
EXPOSE 8243

# -------------------------------
# Define entry point & start sshd
# -------------------------------

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log 2>&1 | /usr/sbin/sshd -D