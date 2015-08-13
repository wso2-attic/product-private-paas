# WSO2 products base image Dockerfile

WSO2 products base image Dockerfile defines required resources for building a Docker image with WSO2 product prerequisites.

## How to build

(1) Copy following files to the packages folder:
```
apache-stratos-python-cartridge-agent-<PCA_VERSION>.zip
jdk-7u60-linux-x64.tar
ppaas-configurator-<PPAAS_VERSION>.zip
```

(2)  Run build.sh file to build the docker image:
```
sh build.sh clean
```

(3) List docker images:
```
docker images
```

## Docker environment variables
```
PPAAS_VERSION - WSO2 Private PaaS Version
PCA_HOME - Apache Stratos Python Cartridge Agent Home
JAVA_HOME - JAVA HOME
CONFIGURATOR_HOME - WSO2 Private PaaS Configurator Home
```