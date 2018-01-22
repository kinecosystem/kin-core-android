#!/usr/bin/env bash

cd kin-sdk-core/truffle

if [ -f './testrpc.pid' ]; then
    echo "killing testrpc on port $(cat ./testrpc.pid)"
    # Don't fail if the process is already killed
    kill -SIGINT $(cat ./testrpc.pid) || true
    rm -f ./testrpc.pid
else
    echo "./testrpc.pid not found, doing nothing"
fi
