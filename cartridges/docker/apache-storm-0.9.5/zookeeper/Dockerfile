FROM wso2/base-image:4.1.0

MAINTAINER dev@wso2.org


ENV DEBIAN_FRONTEND noninteractive
ENV JDK_VERSION jdk1.7.0_72
ENV JDK_TAR_FILENAME jdk-7u72-linux-x64.tar.gz
ENV JAVA_HOME /opt/jdk${JDK_VERSION}
ENV APACHE_ZOOKEEPER_VERSION 3.4.6

ENV PCA_NAME apache-stratos-python-cartridge-agent-${PPAAS_VERSION}
ENV PCA_HOME /opt/${PCA_NAME}

COPY scripts/init.sh /usr/local/bin/init.sh
COPY scripts/export-envs.sh /tmp/export-envs.sh
COPY packages/zookeeper-${APACHE_ZOOKEEPER_VERSION}.tar.gz /opt/

#RUN cp /etc/zookeeper/zoo_sample.cfg /etc/zookeeper/zoo.cfg

# workaround packaging issue
#RUN sed -i 's,/usr/share/java/log4j.jar,/usr/share/java/log4j-1.jar,' /usr/libexec/zkEnv.sh
#cp /opt/zookeeper-3.4.6/conf/zoo_sample.cfg /opt/zookeeper-3.4.6/conf/zoo.cfg

EXPOSE 2181 2888 3888

# -----------------------------
# Install prerequisites and AM
# -----------------------------

WORKDIR /opt

RUN tar xzvf /opt/zookeeper-3.4.6.tar.gz && \
    cp /opt/zookeeper-3.4.6/conf/zoo_sample.cfg /opt/zookeeper-3.4.6/conf/zoo.cfg && \
    chmod +x /tmp/export-envs.sh && \
    sleep 1 && \
    /tmp/export-envs.sh ${JAVA_HOME}

WORKDIR /opt/zookeeper-3.4.6

ENTRYPOINT ["bin/zkServer.sh"]
#CMD ["start-foreground"]

#ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log | /usr/sbin/sshd -D

