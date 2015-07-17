# --------------------------------------------------------------
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# --------------------------------------------------------------

FROM wso2/base-image:4.1.0

MAINTAINER dev@wso2.org

ENV DEBIAN_FRONTEND noninteractive
ENV PPAAS_VERSION 4.1.0-SNAPSHOT
ENV JDK_VERSION 1.7.0_60
ENV JDK_TAR_FILENAME jdk-7u60-linux-x64.tar.gz
ENV JAVA_HOME /opt/jdk${JDK_VERSION}
ENV APACHE_STORM_VERSION 0.9.5
ENV CARBON_HOME /opt/apache-storm-0.9.5
ENV STORM_TEMPLATE_MODULE_NAME apache-storm-0.9.5-template-module-${PPAAS_VERSION}
ENV PCA_NAME apache-stratos-python-cartridge-agent-${PPAAS_VERSION}
ENV PCA_HOME /opt/${PCA_NAME}

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY scripts/init.sh /usr/local/bin/init.sh
COPY scripts/export-envs.sh /tmp/export-envs.sh
COPY packages/apache-storm-${APACHE_STORM_VERSION}.tar.gz /opt/
COPY packages/${STORM_TEMPLATE_MODULE_NAME}.zip /opt/
COPY plugins /${PCA_HOME}/plugins


# -----------------------------
# Install prerequisites and AM
# -----------------------------

WORKDIR /opt

RUN tar xzvf /opt/apache-storm-${APACHE_STORM_VERSION}.tar.gz && \
        unzip /opt/${STORM_TEMPLATE_MODULE_NAME}.zip -d /opt/ && \
        mkdir -p /opt/${CONFIGURATOR_NAME}/template-modules/apache-storm-0.9.5 && \
        mv /opt/${STORM_TEMPLATE_MODULE_NAME}/* /opt/${CONFIGURATOR_NAME}/template-modules/apache-storm-0.9.5/ && \
        rm -rf /opt/${STORM_TEMPLATE_MODULE_NAME}* && \
        chmod +x /tmp/export-envs.sh && \
        chmod +x /usr/local/bin/init.sh && \
        sleep 1 && \
        /tmp/export-envs.sh ${JAVA_HOME} ${CARBON_HOME}

EXPOSE 8080 3772 6627 6700

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log | /usr/sbin/sshd -D
