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

FROM debian:7.7
MAINTAINER dev@wso2.org

ENV PCA_FILENAME=apache-stratos-python-cartridge-agent-4.1.0-SNAPSHOT
# ------------------
# Setup ssh server
# ------------------
WORKDIR /opt/
RUN apt-get update && apt-get install -y openssh-server git
RUN mkdir -p /var/run/sshd
RUN echo 'root:stratos' | chpasswd
RUN sed -i "s/PermitRootLogin without-password/#PermitRootLogin without-password/" /etc/ssh/sshd_config
EXPOSE 22

# ----------------------
# Install dependencies
# ----------------------
# stress tool is added for demo purposes
RUN apt-get install -y git python python-pip python-dev gcc zip stress
RUN pip install paho-mqtt \
    psutil \
    pexpect \
    pycrypto \
    gitpython \
    yapsy

# -------------------------
# Install cartridge agent 
# -------------------------
WORKDIR /opt/

COPY packages/${PCA_FILENAME}.zip /opt/${PCA_FILENAME}.zip
COPY scripts/start-agent.sh /usr/local/bin/start-agent.sh

RUN unzip -q /opt/${PCA_FILENAME}.zip -d /opt/${PCA_FILENAME}/ && \
    rm /opt/${PCA_FILENAME}.zip && \
    mkdir -p /opt/${PCA_FILENAME}/payload && \
    chmod +x /opt/${PCA_FILENAME}/extensions/bash/* && \
    mkdir -p /var/log/apache-stratos && \
    touch /var/log/apache-stratos/cartridge-agent-extensions.log && \
    chmod +x /usr/local/bin/start-agent.sh

