#!/bin/bash
# Used during manual tests of yeastx workflow to compile a new DSS plugin and copy it to datastore_server directory 

# ----- compile the plugin and copy it to DSS dir	
cd ../../build
. ./antrun.sh jar
cp ../targets/dist/datastore_server-plugins.jar ../../datastore_server/source/
