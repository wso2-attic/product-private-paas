# WSO2 products base image Dockerfile

WSO2 products base image Dockerfile defines required resources for building a Docker image with WSO2 product prerequisites.

## How to build

(1) Remove logic which set the root user password, this will disable root user login with credentials:
```
RUN mkdir -p /var/run/sshd
RUN echo 'root:wso2' | chpasswd
RUN sed -i "s/PermitRootLogin without-password/#PermitRootLogin without-password/" /etc/ssh/sshd_config
```

(2) Copy following files to the packages folder:

* [apache-stratos-cartridge-agent-4.1.3.zip ](http://www.apache.org/dyn/closer.cgi/stratos)
* [jdk-7u80-linux-x64.tar.gz](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)

(3)  Run build.sh file to build the docker image:
```
sh build.sh clean
```

(4) List docker images:
```
docker images
```
(5) If successfully built docker image similar to following should display
```
wso2/base-image        4.1.0               ac57800e96c2        2 minutes ago         677.6 MB
```
## Docker environment variables
```
PPAAS_VERSION - WSO2 Private PaaS Version
PCA_HOME - Apache Stratos Python Cartridge Agent Home
JAVA_HOME - JAVA HOME
CONFIGURATOR_HOME - WSO2 Private PaaS Configurator Home
```