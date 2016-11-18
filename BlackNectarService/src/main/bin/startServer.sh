#!/usr/bin/env bash

jar="blacknectar-service.jar"

nohup java -jar $jar > application.log &
