# WSO2 products base image Dockerfile

WSO2 products base image Dockerfile defines required resources for building a Docker image with WSO2 product prerequisites.

## How to build

(1) Copy following files to the packages folder:
```
apache-stratos-python-cartridge-agent-4.1.0-SNAPSHOT.zip
jdk-7u60-linux-x64.tar
ppaas-configurator-<version>.zip
```

(2)  Run build.sh file to build the docker image:
```
sh build.sh
```

(3) List docker images:
```
docker images
```