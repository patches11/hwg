#!/bin/bash

COMMIT=$(git rev-parse HEAD)

FULL_TAG = $COMMIT-SNAPSHOT

envsubst kubernetes/deployment.yaml | kubectl apply -f -