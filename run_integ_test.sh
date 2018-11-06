#!/usr/bin/env bash
if [ -z "$1" ]
then
    echo "Please specify AVD name to test on."
    exit 1
fi

git clone https://github.com/kinecosystem/blockchain-ops.git
cd blockchain-ops
git checkout c26afdc168ad378363c63fb44de5a93b3cf24ddf
cd image
./init.sh
cd ../..
~/Library/Android/sdk/tools/emulator -avd $1 &

#wait for emulator
set +e

bootanim=""
failcounter=0
timeout_in_sec=360

until [[ "$bootanim" =~ "stopped" ]]; do
  bootanim=`adb -e shell getprop init.svc.bootanim 2>&1 &`
  if [[ "$bootanim" =~ "device not found" || "$bootanim" =~ "device offline"
    || "$bootanim" =~ "running" ]]; then
    let "failcounter += 1"
    echo "Waiting for emulator to start"
    if [[ $failcounter -gt timeout_in_sec ]]; then
      echo "Timeout ($timeout_in_sec seconds) reached; failed to start emulator"
      exit 1
    fi
  fi
  sleep 1
done

echo "Emulator is ready"
#---------------------
./gradlew jacocoTestReport
if [ $? -eq 0 ]; then
    open kin-sdk/build/reports/jacoco/jacocoTestReport/html/index.html
else
    echo Build failed!
fi

