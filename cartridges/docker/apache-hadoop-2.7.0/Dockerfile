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
USER root

RUN ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
RUN cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
ADD ssh_config /root/.ssh/config

ENV DEBIAN_FRONTEND noninteractive
ENV PPAAS_VERSION 4.1.0-SNAPSHOT
ENV JDK_VERSION 1.7.0_72
ENV JAVA_HOME /opt/jdk${JDK_VERSION}
ENV CONFIGURATOR_NAME ppaas-configurator-${PPAAS_VERSION}
ENV HADOOP_VERSION 2.7.0
ENV HADOOP_TEMPLATE_MODULE_NAME apache-hadoop-${HADOOP_VERSION}-template-module-${PPAAS_VERSION}
ENV HADOOP_HOME /opt/hadoop-${HADOOP_VERSION}
ENV PCA_NAME apache-stratos-python-cartridge-agent-4.1.0
ENV PCA_HOME /opt/${PCA_NAME}
ENV CONFIG_PARAM_HADOOP_MASTER localhost

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------

COPY scripts/init.sh /usr/local/bin/init.sh
COPY scripts/export-envs.sh /tmp/export-envs.sh
COPY packages/${HADOOP_TEMPLATE_MODULE_NAME}.zip /opt/
ADD packages/hadoop-${HADOOP_VERSION}.tar.gz /opt/
COPY plugins /${PCA_HOME}/plugins

# --------------------------------
# Install prerequisites and HADOOP
# --------------------------------
WORKDIR /opt

RUN mkdir -p /app/hadoop/tmp
RUN chmod 750 /app/hadoop/tmp
RUN mkdir -p /opt/hdfs/namenode
RUN chmod 750 /opt/hdfs/namenode
RUN mkdir -p /opt/hdfs/datanode
RUN chmod 750 /opt/hdfs/datanode

RUN unzip /opt/${HADOOP_TEMPLATE_MODULE_NAME}.zip -d /opt/${CONFIGURATOR_NAME}/template-modules/ && \
    rm /opt/${HADOOP_TEMPLATE_MODULE_NAME}.zip && \
    chmod +x /tmp/export-envs.sh && \
    sleep 1 && \
    /tmp/export-envs.sh ${JAVA_HOME} ${HADOOP_HOME}

# ----------------------
# Expose container ports
# ----------------------

# HADOOP htts port
EXPOSE 50070
EXPOSE 54310
EXPOSE 54311
# -------------------------------
# Define entry point & start sshd
# -------------------------------

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log | /usr/sbin/sshd -D
