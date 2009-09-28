#!/bin/bash

function print_result {
	local result=$1
	if [ $result -ne 0 ]; then
		echo ERROR: Test has failed.
	fi
}

echo Testing Screening Workflow
./test-3v.sh $@
result_3v=$?
print_result $result_3v

echo Testing YeastX Workflow
./test-yeastx.sh $@
result_yeastx=$?
print_result $result_yeastx

if [ $result_3v -ne 0 -o $result_yeastx -ne 0 ]; then
	exit 1;
fi

