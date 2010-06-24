#! /bin/bash

# Delete (clear) an openBIS repository
# 2009, Bernd Rinn, CISD
# 2009, Sean Walsh, CISD - modified : 
# 1) to ask for user confirm 
# 2) removed automated restart of datastore server (see echos at end)
# 3) to take a SnapShot of the deleted openBIS 

echo "Do you really want to drop the database openbis_productive on this server?"
echo ""
echo "Server URL from datastore_server service.properties is :"
echo ""
grep server-url ~/sprint/datastore_server/etc/service.properties
echo ""
echo "Server DNS is : `uname -n`"
echo ""
echo "Answer y to continue, followed by [ENTER]:"
read ans

if [ "$ans" = "y" ]; then
  echo "Deleting openbis_productive" 
else
  echo "Answer is not y. Leaving database untouched."
  exit 0
fi

# Take snapshot just in case
pushd /tmp
echo "Creating SnapShot in /tmp with the TAG DELETED_OPENBIS"
~/bin/create_snapshot_openbis_repos.sh DELETED_OPENBIS
popd

# Shutdown servers
~/sprint/openBIS-server/jetty/bin/shutdown.sh
~/sprint/datastore_server/datastore_server.sh stop

# Remove the repos data
rm -fR  ~/sprint/datastore_server/data/store/*
psql -U postgres -c "drop database openbis_productive"
rm -fR ~/sprint/openBIS-server/jetty/indices/*

# Startup servers (will rebuild the database)
~/sprint/openBIS-server/jetty/bin/startup.sh

echo ""
echo "You need to login to openBIS now and to create the first user with system role"
echo "The start datastore_server with "
echo ""
echo "~/sprint/datastore_server/datastore_server.sh start"

