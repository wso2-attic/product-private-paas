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

RUN ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
RUN cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
ADD ssh_config /root/.ssh/config

ENV DEBIAN_FRONTEND noninteractive

ENV HBASE_VERSION 1.0.1.1
ENV HBASE_TEMPLATE_MODULE_NAME apache-hbase-${HBASE_VERSION}-template-module-${PPAAS_VERSION}
ENV HBASE_HOME /opt/hbase-${HBASE_VERSION}

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
ADD packages/hbase-${HBASE_VERSION}-bin.tar.gz /opt/
COPY scripts/init.sh /usr/local/bin/init.sh
COPY scripts/add-host.sh /tmp/add-host.sh
COPY packages/${HBASE_TEMPLATE_MODULE_NAME}.zip /opt/
COPY plugins /${PCA_HOME}/plugins

# -----------------------------
# Install prerequisites and ESB
# -----------------------------
WORKDIR /opt

RUN unzip /opt/${HBASE_TEMPLATE_MODULE_NAME}.zip -d ${CONFIGURATOR_HOME}/template-modules && \
    rm -rf /opt/${HBASE_TEMPLATE_MODULE_NAME}.zip

# ----------------------
# Expose container ports
# ----------------------

# HBase master
EXPOSE 16000
# HBase master web UI
EXPOSE 16010
# HBase regionserver
EXPOSE 16020
# HBase regionserver web UI
EXPOSE 16030

# -------------------------------
# Define entry point & start sshd
# -------------------------------

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log | /usr/sbin/sshd -D
