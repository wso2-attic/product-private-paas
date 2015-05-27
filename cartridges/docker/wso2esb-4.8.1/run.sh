#!/bin/bash

# Start with configurator
docker run -d -P --name wso2esb wso2/wso2esb:4.8.1

# Start with python cartridge agent
# docker run -e START_CMD=PCA -d -P --name wso2esb-4.8.1 wso2/esb:4.8.1
