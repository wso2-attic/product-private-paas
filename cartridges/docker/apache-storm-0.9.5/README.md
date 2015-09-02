# Apache Storm 0.9.5 Dockerfile

Apache Storm 0.9.5 Dockerfile defines required resources for building a Docker image with Apache Storm 0.9.5.

## How to build

1. Copy following files to the packages folder:
```
apache-storm-0.9.5.tar.gz
apache-storm-0.9.5-template-module-<version>.zip
```

2. Run build.sh file to build the docker image.(This will copy the plugins and template module to the docker image)
```
sh build.sh clean
```

3. List docker images:
```
docker images
```