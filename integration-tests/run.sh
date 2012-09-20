#!/bin/bash

# all tests to be executed
TEST_FILE_PATTERN=./test-*.sh
EXIT_WITH_ERROR=false

function print_result {
  local testFile=$1
	local result=$2
	if [ $result -ne 0 ]; then
		echo ERROR: Test "$testFile" has failed.
		EXIT_WITH_ERROR=true
	else
		echo OK: Test "$testFile" succeeded.
	fi
}

for testScript in $TEST_FILE_PATTERN; do
  echo =================================================================
  echo ==================== Executing test $testScript 
  ./$testScript $@
  result=$?
  print_result $testScript $result
done

if [ true == "$EXIT_WITH_ERROR" ]; then
	exit 1;
fi

