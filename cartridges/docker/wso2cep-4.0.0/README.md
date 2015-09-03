# WSO2 CEP 4.0.0 Dockerfile

WSO2 CEP 4.0.0 Dockerfile defines required resources for building a Docker image with WSO2 CEP 4.0.0.

## How to build

1. Copy following files to the packages folder:
```
wso2cep-4.0.0.zip
wso2cep-4.0.0-template-module-<version>.zip
```

2. Run build.sh file to build the docker image. (This will create template zip file and copy it to the docker image.)
```
sh build.sh clean
```

3. List docker images:
```
docker images
```