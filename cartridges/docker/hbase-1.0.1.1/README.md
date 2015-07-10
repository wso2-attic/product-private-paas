# HBase 1.0.1.1 Dockerfile

HBase 1.0.1.1 Dockerfile defines required resources for building a Docker image with HBase 1.0.1.1.

## How to build

1. Copy following files to the packages folder:
```
ppaas-configurator-<version>.zip
hbase-1.0.1.1.zip
hbase-1.0.1.1-template-module-<version>.zip
```

2. Run build.sh file to build the docker image:
```
sh build.sh
```

3. List docker images:
```
docker images
```