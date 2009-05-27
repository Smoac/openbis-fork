#! /bin/bash

# Delete (clear) an openBIS repository
# 2009, Bernd Rinn, CISD

# Shutdown servers
~/sprint/openBIS-server/apache-tomcat/bin/shutdown.sh
~/sprint/datastore_server/datastore_server.sh stop

# Remove the repos data
rm -fR  ~/sprint/datastore_server/data/store/*
psql -U postgres -c "drop database openbis_productive"
rm -fR ~/sprint/openBIS-server/apache-tomcat/indices/*

# Startup servers (will rebuild the database)
~/sprint/openBIS-server/apache-tomcat/bin/startup.sh
~/sprint/datastore_server/datastore_server.sh start

