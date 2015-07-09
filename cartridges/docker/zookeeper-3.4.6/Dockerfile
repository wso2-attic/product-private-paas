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
ENV CARBON_HOME /opt/zookeeper-3.4.6
ENV PCA_NAME apache-stratos-python-cartridge-agent-${PPAAS_VERSION}
ENV PCA_HOME /opt/${PCA_NAME}
ENV APACHE_ZOOKEEPER_VERSION 3.4.6

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY scripts/init.sh /usr/local/bin/init.sh
COPY scripts/export-envs.sh /tmp/export-envs.sh
#COPY plugins /${PCA_HOME}/plugins
COPY packages/zookeeper-3.4.6.tar.gz /opt/


# -----------------------------
# Install prerequisites and Zookeeper
# -----------------------------

WORKDIR /opt

RUN tar xzvf /opt/zookeeper-3.4.6.tar.gz && \
    cp /opt/zookeeper-3.4.6/conf/zoo_sample.cfg /opt/zookeeper-3.4.6/conf/zoo.cfg && \
    chmod +x /tmp/export-envs.sh && \
    chmod +x /usr/local/bin/init.sh && \
    sleep 1 && \
    /tmp/export-envs.sh ${JAVA_HOME} ${CARBON_HOME}

EXPOSE 2181 2888 3888

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log | /usr/sbin/sshd -D

