# Hadoop 2.7.0 Dockerfile

Hadoop 2.7.0 Dockerfile defines required resources for building a Docker image with Hadoop 2.7.0.

## How to build

1. Copy following files to the packages folder:
```
jdk-7u79-linux-x64.tar
ppaas-configurator-<version>.zip
hadoop-2.7.0.tar.gz
hadoop-2.7.0-template-module-<version>.zip
```

2. Run build.sh file to build the docker image:
```
sh build.sh
```

3. List docker images:
```
docker images
```
