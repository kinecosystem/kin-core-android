#!/usr/bin/env bash

#use `am instrument` to filter out @LargeTest annotated tests in CI, these tests access the network (test net) and are flaky
instrumentationResult=$(adb shell 'am instrument -w -e notAnnotation android.support.test.filters.LargeTest  kin.core.test/android.support.test.runner.AndroidJUnitRunner')
printf "$instrumentationResult\n"

if [[ $instrumentationResult = *"FAILURES!!!"* ]]; then
  exit 1
fi