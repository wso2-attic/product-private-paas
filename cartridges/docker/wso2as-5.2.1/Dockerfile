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

ENV WSO2_AS_VERSION 5.2.1
ENV AS_TEMPLATE_MODULE_NAME wso2as-${WSO2_AS_VERSION}-template-module-${PPAAS_VERSION}
ENV CARBON_HOME /opt/wso2as-${WSO2_AS_VERSION}

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY scripts/init.sh /usr/local/bin/init.sh
COPY packages/${AS_TEMPLATE_MODULE_NAME}.zip /opt/
COPY packages/wso2as-${WSO2_AS_VERSION}.zip /opt/
COPY plugins ${PCA_HOME}/plugins

# -----------------------------
# Install prerequisites and AS
# -----------------------------
WORKDIR /opt

RUN unzip /opt/wso2as-${WSO2_AS_VERSION}.zip -d /opt/ && \
    rm /opt/wso2as-${WSO2_AS_VERSION}.zip && \
	unzip /opt/${AS_TEMPLATE_MODULE_NAME}.zip -d ${CONFIGURATOR_HOME}/template-modules && \
    rm -rf /opt/${AS_TEMPLATE_MODULE_NAME}.zip

# ----------------------
# Expose container ports
# ----------------------

# AS https port
EXPOSE 9443

# -------------------------------
# Define entry point & start sshd
# -------------------------------

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log 2>&1 | /usr/sbin/sshd -D