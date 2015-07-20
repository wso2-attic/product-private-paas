# --------------------------------------------------------------
#
#  Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
# --------------------------------------------------------------

FROM wso2/base-image:4.1.0
MAINTAINER dev@wso2.org

ENV DEBIAN_FRONTEND noninteractive

ENV PPAAS_VERSION 4.1.0-SNAPSHOT
ENV WSO2_DAS_VERSION 3.0.0-SNAPSHOT
ENV MYSQL_CONNECTOR_VERSION 5.1.35
ENV DAS_TEMPLATE_MODULE_NAME wso2das-3.0.0-template-module-${PPAAS_VERSION}
ENV PCA_NAME apache-stratos-python-cartridge-agent-${PPAAS_VERSION}
ENV PCA_HOME /opt/${PCA_NAME}
ENV CARBON_HOME /opt/wso2das-${WSO2_DAS_VERSION}

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY scripts/init.sh /usr/local/bin/init.sh
COPY packages/${DAS_TEMPLATE_MODULE_NAME}.zip /opt/
COPY packages/wso2das-${WSO2_DAS_VERSION}.zip /opt/
COPY packages/mysql-connector-java-${MYSQL_CONNECTOR_VERSION}-bin.jar /opt/
COPY plugins /${PCA_HOME}/plugins

# -----------------------------
# Install prerequisites and DAS
# -----------------------------
WORKDIR /opt

RUN unzip /opt/wso2das-${WSO2_DAS_VERSION}.zip -d /opt/ && \
    rm /opt/wso2das-${WSO2_DAS_VERSION}.zip && \
    mv /opt/mysql-connector-java-${MYSQL_CONNECTOR_VERSION}-bin.jar /opt/wso2das-${WSO2_DAS_VERSION}/repository/components/lib && \
    unzip /opt/${DAS_TEMPLATE_MODULE_NAME}.zip -d ${CONFIGURATOR_HOME}/template-modules && \
    rm -rf /opt/${DAS_TEMPLATE_MODULE_NAME}.zip

RUN pip install PyMySQL

# ----------------------
# Expose container ports
# ----------------------

# DAS https port
EXPOSE 9443
# Thrift port
EXPOSE 7611
# Binary port
EXPOSE 9611

# -------------------------------
# Define entry point & start sshd
# -------------------------------

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log 2>&1 | /usr/sbin/sshd -D
