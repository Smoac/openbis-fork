#! /bin/bash

# Restore a snapshot of an openBIS repository
# 2009, Bernd Rinn, CISD

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
 
PRGDIR=`dirname "$PRG"`
source ${PRGDIR}/repos.conf
if [ -z "$BASE_DIR" -o -z "$DB_NAME" ]; then
  echo "No BASE_DIR or DB_NAME set, check repos.conf"
  exit 1
fi

FNAME="$1"
if [ "$FNAME" = "" ]; then
  echo "Syntax: ${0##*/} <SNAPSHOT_NAME>"
  exit 1
fi
if [ ! -f "$FNAME" ]; then
  echo "Snapshot file $FNAME does not exist"
  exit 1
fi

NAME=${FNAME%*.tar.bz2}
TMPROOT="`mktemp -d -p ~/tmp`"
TMPDIR="$TMPROOT/$NAME"

# Extract snapshot
bunzip2 -c "$FNAME" | gtar xf - -C "$TMPROOT"
if [ $? -ne 0 ]; then
  echo "Error untarring snapshot (nothing restored)"
  rm -fR "$TMPROOT"
  exit 1
fi

# Check snapshot
gtar tf "$TMPDIR/datastore.tar" > /dev/null
if [ $? -ne 0 ]; then
  echo "Error testing datastore (nothing restored)"
  rm -fR "$TMPROOT"
  exit 1
fi
gtar tf "$TMPDIR/lucene_indices.tar" > /dev/null
if [ $? -ne 0 ]; then
  echo "Error testing indices (nothing restored)"
  rm -fR "$TMPROOT"
  exit 1
fi
if [ ! -f "$TMPDIR/database.sql" ]; then
  echo "No database dump found (nothing restored)"
  rm -fR "$TMPROOT"
  exit 1
fi

# Create database and restore dump
psql -U postgres -c "create database openbis_restore owner openbis"
if [ $? -ne 0 ]; then
  echo "Cannot create database (nothing restored)"
  rm -fR "$TMPROOT"
  exit 1
fi
psql -U openbis -d openbis_restore -f "$TMPDIR/database.sql"
if [ $? -ne 0 ]; then
  echo "Cannot restore database dump (nothing restored)"
  rm -fR "$TMPROOT"
  psql -U postgres -c "drop database openbis_restore"
  exit 1
fi

# Shutdown servers
$BASE_DIR/openBIS-server/jetty/bin/shutdown.sh
$BASE_DIR/datastore_server/datastore_server.sh stop

# Remove the repos data
rm -fR $BASE_DIR/openBIS-server/jetty/indices/*
if [ $? -ne 0 ]; then
  echo "Error deleting lucene indices (inconsistent state)"
  rm -fR "$TMPROOT"
  psql -U postgres -c "drop database openbis_restore"
  exit 1
fi
rm -fR  $BASE_DIR/datastore_server/data/store/*
if [ $? -ne 0 ]; then
  echo "Error deleting data store (inconsistent state)"
  rm -fR "$TMPROOT"
  psql -U postgres -c "drop database openbis_restore"
  exit 1
fi
psql -U postgres -c "drop database $DB_NAME"
if [ $? -ne 0 ]; then
  echo "Error droping database (inconsistent state)"
  rm -fR "$TMPROOT"
  psql -U postgres -c "drop database openbis_restore"
  exit 1
fi

# Restore repos data
psql -U postgres -c "alter database openbis_restore rename to $DB_NAME"
if [ $? -ne 0 ]; then
  echo "Error renaming database (inconsistent state)"
  rm -fR "$TMPROOT"
  psql -U postgres -c "drop database openbis_restore"
  exit 1
fi
gtar xf "$TMPDIR/datastore.tar" -C $BASE_DIR/datastore_server/data
if [ $? -ne 0 ]; then
  echo "Error restoring data store (inconsistent state)"
  rm -fR "$TMPROOT"
  exit 1
fi
gtar xf "$TMPDIR/lucene_indices.tar" -C $BASE_DIR/openBIS-server/jetty
if [ $? -ne 0 ]; then
  echo "Error restoring lucene indices (inconsistent state)"
  rm -fR "$TMPROOT"
  exit 1
fi

# Startup servers
$BASE_DIR/openBIS-server/jetty/bin/startup.sh
$BASE_DIR/datastore_server/datastore_server.sh start

# Clean up
rm -fR "$TMPROOT"
