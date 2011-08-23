#! /bin/bash
# 
# This script does the following:
# - Install generic openBIS sprint servers based on latest builds on Hudson.
# - Restore store and databases from latest snapshot
# - Restart AS and DSS
# 
echo "home directory: $PWD"


fetch-ci-artifacts.sh openbis
fetch-ci-artifacts.sh rtd_yeastx
fetch-ci-artifacts.sh -p server datastore_server
#sprint_install.sh
#servers-startup-from-latest-snapshot.sh snapshots

