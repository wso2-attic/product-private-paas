# WSO2 AS 5.2.1 Dockerfile

WSO2 AS 5.2.1 Dockerfile defines required resources for building a Docker image with WSO2 AS 5.2.1.

## How to build

1. Copy following files to the packages folder:
```
jdk-7u60-linux-x64.tar
ppaas-configurator-<version>.zip
wso2as-5.2.1.zip
wso2as-5.2.1-template-module-<version>.zip
mysql-connector-java-<version>-bin.jar (Note: Change the MYSQL_CONNECTOR_VERSION value in Dockerfile accordingly)
```

2. Run build.sh file to build the docker image:
```
sh build.sh
```

3. List docker images:
```
docker images
```