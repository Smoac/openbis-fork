#!/bin/bash

./clean.sh

# ----- compile the plugin and copy it to DSS dir
	
cd ../../build
. ./antrun.sh jar
cp ../targets/dist/datastore_server-plugins.jar ../../datastore_server/source/
