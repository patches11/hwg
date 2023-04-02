#!/bin/bash

COMMIT=$(git rev-parse HEAD)

sbt docker

docker push 192.168.86.185:31836/hwg:$COMMIT