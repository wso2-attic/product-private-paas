# Zookeeper 3.4.6 Dockerfile

Zookeeper 3.4.6 Dockerfile defines required resources for building a Docker image with Zookeeper 3.4.6.

## How to build

1. Copy following files to the packages folder:
```
zookeeper-3.4.6.tar.gz
zookeeper-3.4.6-template-module-<version>.zip
```

2. Run build.sh file to build the docker image.(This will copy the plugins and template module to the docker image)
```
sh build.sh clean
```

3. List docker images:
```
docker images
```