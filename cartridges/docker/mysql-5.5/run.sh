#!/usr/bin/env bash
docker run --name some-mysql -e MYSQL_ROOT_PASSWORD=123 -d -P mysql:5.5
