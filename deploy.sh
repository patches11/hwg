#!/bin/bash

COMMIT=$(git rev-parse HEAD)

FULL_TAG = $COMMIT

envsubst kubernetes/deployment.yaml | kubectl apply -f -