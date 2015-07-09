# WSO2 DAS 4.8.1 Dockerfile

WSO2 DAS 3.0.0-SNAPSHOT Dockerfile defines required resources for building a Docker image with WSO2 DAS 3.0.0.SNAPSHOT

## How to build

1. Copy following files to the packages folder:
```
jdk-7u60-linux-x64.tar
ppaas-configurator-<version>.zip
wso2das-3.0.0-SNAPSHOT.zip
wso2das-3.0.0-template-module-<version>.zip
mysql-connector-java-<version>-bin.jar
```

WSO2 DAS 3.0.0 version is not yet released, thus 3.0.0-SNAPSHOT is used for the moment

2. Run build.sh file to build the docker image:
```
sh build.sh
```

3. List docker images:
```
docker images
```

4. To enable only analytics profile set Environment variable PROFILE to analytics.
   To enable only receiver profile set Environment variable PROFILE to receiver.