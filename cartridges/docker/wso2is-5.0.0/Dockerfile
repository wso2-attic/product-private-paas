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
ENV CONFIGURATOR_NAME ppaas-configurator-${PPAAS_VERSION}
ENV CONFIGURATOR_HOME /opt/${CONFIGURATOR_NAME}
ENV WSO2_IS_VERSION 5.0.0
ENV IS_TEMPLATE_MODULE_NAME wso2is-${WSO2_IS_VERSION}-template-module-${PPAAS_VERSION}
ENV CARBON_HOME /opt/wso2is-${WSO2_IS_VERSION}
ENV PCA_NAME apache-stratos-python-cartridge-agent-${PPAAS_VERSION}
ENV PCA_HOME /opt/${PCA_NAME}

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
ADD packages/${JDK_TAR_FILENAME} /opt/
COPY scripts/init.sh /usr/local/bin/init.sh
COPY scripts/export-envs.sh /tmp/export-envs.sh
COPY packages/${CONFIGURATOR_NAME}.zip /opt/
COPY packages/${IS_TEMPLATE_MODULE_NAME}.zip /opt/
COPY packages/wso2is-${WSO2_IS_VERSION}.zip /opt/
COPY plugins /${PCA_HOME}/plugins

# -----------------------------
# Install prerequisites and IS
# -----------------------------
WORKDIR /opt

RUN unzip /opt/wso2is-${WSO2_IS_VERSION}.zip -d /opt/ && \
    rm /opt/wso2is-${WSO2_IS_VERSION}.zip && \
    unzip /opt/${CONFIGURATOR_NAME}.zip -d /opt/ && \
    rm /opt/${CONFIGURATOR_NAME}.zip && \
    unzip /opt/${IS_TEMPLATE_MODULE_NAME}.zip -d /opt/${CONFIGURATOR_NAME}/template-modules/ && \
    rm /opt/${IS_TEMPLATE_MODULE_NAME}.zip && \
    pip install jinja2 && \
    chmod +x /tmp/export-envs.sh && \
    sleep 1 && \
    /tmp/export-envs.sh ${JAVA_HOME} ${CARBON_HOME}

# ----------------------
# Expose container ports
# ----------------------

# IS https port
EXPOSE 9443
# -------------------------------
# Define entry point & start sshd
# -------------------------------

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log | /usr/sbin/sshd -D