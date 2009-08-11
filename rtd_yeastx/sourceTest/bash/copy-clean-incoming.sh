#!/bin/bash
# Tomasz Pylak, CISD
# This script copies the set of test datasets to the new folder, skipping the .svn directories

rm -fr incoming-clean
mkdir incoming-clean
cp -R incoming incoming-clean
find incoming-clean -name .svn -exec rm -fr {} \;
