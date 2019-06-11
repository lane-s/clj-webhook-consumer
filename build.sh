#!/bin/bash
lein uberjar
docker build -t melodylane/clj-webhook-consumer .
