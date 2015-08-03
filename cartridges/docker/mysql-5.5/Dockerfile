# ----------------------------------------------------------------------------
#
#  Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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
# ----------------------------------------------------------------------------

FROM wso2/base-image:4.1.0

ENV DEBIAN_FRONTEND noninteractive

ENV PPAAS_VERSION 4.1.0-SNAPSHOT
ENV PCA_NAME apache-stratos-python-cartridge-agent-${PPAAS_VERSION}
ENV PCA_HOME /opt/${PCA_NAME}


RUN apt-get update && \
    apt-get install mysql-server-5.5 -y && \
    apt-get clean && \
    rm -rf /var/cache/apt/*

COPY scripts/my.cnf /etc/mysql/conf.d/my.cnf
COPY plugins /${PCA_HOME}/plugins

RUN chmod 664 /etc/mysql/conf.d/my.cnf
COPY scripts/init.sh /usr/local/bin/init.sh
RUN chmod +x /usr/local/bin/init.sh

EXPOSE 3306
VOLUME ["/var/lib/mysql"]

ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log | /usr/sbin/sshd -D

