#!/bin/bash

export TAG=$(git rev-parse HEAD)

envsubst < kubernetes/deployment.yaml | kubectl apply -f -