#!/usr/bin/env bash

cd kin-sdk-core/truffle

./node_modules/.bin/truffle deploy --reset > ./truffle.log 2>&1

cat ./truffle.log | grep "Token contract deployed at" | tail -n 1 | awk '{print $5}' > ./token-contract-address
