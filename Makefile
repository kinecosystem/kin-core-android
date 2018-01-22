# default target does nothing
.DEFAULT_GOAL: default
default: ;

# add truffle and testrpc to $PATH
export PATH := ./kin-sdk-core/truffle/node_modules/.bin:$(PATH)
export PATH := /usr/local/bin:$(PATH)

test:
	./gradlew :sample:assembleDebug
	./gradlew :kin-sdk-core:connectedAndroidTest
.PHONY: test

prepare-tests: truffle
	kin-sdk-core/truffle/scripts/prepare-tests.sh
.PHONY: test

truffle: testrpc truffle-clean
	kin-sdk-core/truffle/scripts/truffle.sh
.PHONY: truffle

truffle-clean:
	rm -f kin-sdk-core/truffle/token-contract-address
.PHONY: truffle-clean

testrpc: testrpc-run  # alias for testrpc-run
.PHONY: testrpc

testrpc-run: testrpc-kill
	kin-sdk-core/truffle/scripts/testrpc-run.sh
.PHONY: testrpc-run

testrpc-kill:
	kin-sdk-core/truffle/scripts/testrpc-kill.sh
.PHONY: testrpc-kill

clean: truffle-clean testrpc-kill
	rm -f kin-sdk-core/truffle/truffle.log
	rm -f kin-sdk-core/truffle/testrpc.log
