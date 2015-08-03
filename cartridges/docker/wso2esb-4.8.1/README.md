# WSO2 ESB 4.8.1 Dockerfile

WSO2 ESB 4.8.1 Dockerfile defines required resources for building a Docker image with WSO2 ESB 4.8.1.

## How to build

1. Copy following files to the packages folder:
```
wso2esb-4.8.1.zip
wso2esb-4.8.1-template-module-<PPAAS_VERSION>.zip
```

2. Run build.sh file to build the docker image:
```
sh build.sh
```

3. List docker images:
```
docker images
```