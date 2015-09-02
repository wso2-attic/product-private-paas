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

RUN ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
RUN cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
ADD ssh_config /root/.ssh/config

ENV HADOOP_VERSION 2.6.0
ENV HADOOP_TEMPLATE_MODULE_NAME apache-hadoop-${HADOOP_VERSION}-template-module-${PPAAS_VERSION}
ENV HADOOP_HOME /opt/hadoop-${HADOOP_VERSION}

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------

COPY scripts/init.sh /usr/local/bin/init.sh
COPY scripts/export-envs.sh /tmp/export-envs.sh
COPY scripts/add-host.sh /tmp/add-host.sh
COPY packages/${HADOOP_TEMPLATE_MODULE_NAME}.zip /opt/
ADD packages/hadoop-${HADOOP_VERSION}.tar.gz /opt/
COPY plugins /${PCA_HOME}/plugins

# --------------------------------
# Install prerequisites and HADOOP
# --------------------------------
WORKDIR /opt

RUN mkdir -p /app/hadoop/tmp
RUN chown -R root /app/hadoop/tmp
RUN chown -R root ${HADOOP_HOME}

RUN unzip /opt/${HADOOP_TEMPLATE_MODULE_NAME}.zip -d ${CONFIGURATOR_HOME}/template-modules/ && \
    rm /opt/${HADOOP_TEMPLATE_MODULE_NAME}.zip

# ----------------------
# Expose container ports
# ----------------------

# HADOOP htts port
EXPOSE 50070 54310 54311 50075 50010 50020 50090

# -------------------------------
# Define entry point & start sshd
# -------------------------------

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log | /usr/sbin/sshd -D