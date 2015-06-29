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

ENV DEBIAN_FRONTEND noninteractive
ENV PCA_FILENAME apache-stratos-python-cartridge-agent-4.1.0-SNAPSHOT

#JDK and configurator
ENV JDK_VERSION 1.7.0_60
ENV PPAAS_VERSION 4.1.0-SNAPSHOT
ENV JDK_TAR_FILENAME jdk-7u60-linux-x64.tar.gz
ENV JAVA_HOME /opt/jdk${JDK_VERSION}
ENV CONFIGURATOR_NAME ppaas-configurator-${PPAAS_VERSION}
ENV CONFIGURATOR_HOME /opt/${CONFIGURATOR_NAME}

WORKDIR /opt/

# ------------------
# Setup ssh server
# ------------------

RUN apt-get update && apt-get install -y \
		openssh-server \
		git \
		python \
		python-pip \ 
		python-dev gcc \
		zip \
		stress
# stress tool is added for demo purposes

RUN mkdir -p /var/run/sshd
RUN echo 'root:stratos' | chpasswd
RUN sed -i "s/PermitRootLogin without-password/#PermitRootLogin without-password/" /etc/ssh/sshd_config
EXPOSE 22

# ----------------------
# Install dependencies
# ----------------------

RUN pip install jinja2 \
    paho-mqtt \
    psutil \
    pexpect \
    pycrypto \
    gitpython \
    yapsy

# -------------------------
# Install cartridge agent 
# -------------------------
ADD  packages/${JDK_TAR_FILENAME} /opt/
COPY packages/${CONFIGURATOR_NAME}.zip /opt/
COPY scripts/start-agent.sh /usr/local/bin/start-agent.sh
COPY packages/${PCA_FILENAME}.zip /opt/${PCA_FILENAME}.zip

RUN unzip -q /opt/${PCA_FILENAME}.zip && \
    rm /opt/${PCA_FILENAME}.zip && \
    mkdir -p /opt/${PCA_FILENAME}/payload && \
    chmod +x /opt/${PCA_FILENAME}/extensions/bash/* && \
    unzip /opt/${CONFIGURATOR_NAME}.zip -d /opt/ && \
    rm /opt/${CONFIGURATOR_NAME}.zip && \
    mkdir -p /var/log/apache-stratos && \
    touch /var/log/apache-stratos/cartridge-agent-extensions.log && \
    chmod +x /usr/local/bin/start-agent.sh
