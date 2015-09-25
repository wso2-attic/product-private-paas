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
ENV WSO2_SERVER_TYPE cep
ENV WSO2_SERVER_VERSION 4.0.0
ENV TEMPLATE_MODULE_NAME wso2${WSO2_SERVER_TYPE}-${WSO2_SERVER_VERSION}-template-module-${PPAAS_VERSION}
ENV LOGSTASH_TEMPLATE_MODULE logstash-1.5.4-template-module-${PPAAS_VERSION}

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY scripts/init.sh /usr/local/bin/init.sh
COPY packages/${TEMPLATE_MODULE_NAME}.zip /opt/
COPY packages/${LOGSTASH_TEMPLATE_MODULE}.zip /opt/
COPY packages/wso2${WSO2_SERVER_TYPE}-${WSO2_SERVER_VERSION}.zip /opt/
COPY packages/logstash-1.5.4.tar.gz /opt/
COPY plugins ${PCA_HOME}/plugins

# -------------------------------
# Unzip and move template modules
# -------------------------------
WORKDIR /opt

RUN tar xzvf /opt/logstash-1.5.4.tar.gz && \
    rm /opt/logstash-1.5.4.tar.gz && \
    unzip /opt/${TEMPLATE_MODULE_NAME}.zip -d ${CONFIGURATOR_HOME}/template-modules && \
    unzip /opt/${LOGSTASH_TEMPLATE_MODULE}.zip -d ${CONFIGURATOR_HOME}/template-modules && \
    rm -rf /opt/${TEMPLATE_MODULE_NAME}.zip \
    rm -rf /opt/${LOGSTASH_TEMPLATE_MODULE}.zip

# ----------------------
# Expose container ports
# ----------------------

EXPOSE 9443 7611 7711 8904

# -------------------------------
# Define entry point & start sshd
# -------------------------------

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log 2>&1 | /usr/sbin/sshd -D
