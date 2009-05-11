#! /bin/bash

# Create a snapshot of an openBIS repository
# 2009, Bernd Rinn, CISD

DB_NAME="lims_productive"
BASE_DIR="$HOME/sprint"

TAG="$1"
if [ "$TAG" = "" ]; then
  echo "Syntax: ${0##*/} <TAG>"
  exit 1
fi

DATE=`date +%Y-%m-%d_%H:%M:%S`
NAME="openbis_snapshot_${TAG}_${DATE}"
TMPROOT="`mktemp -d -p ~/tmp`"
TMPDIR="$TMPROOT/$NAME"
mkdir "$TMPDIR"

# Shutdown servers
$BASE_DIR/openBIS-server/apache-tomcat/bin/shutdown.sh
$BASE_DIR/datastore_server/datastore_server.sh stop

# Prepare snaphot
gtar cf "$TMPDIR/datastore.tar" -C $BASE_DIR/datastore_server/data store
if [ $? -ne 0 ]; then
  echo "Error tarring data store! (No snapshot created)"
  rm -fR "$TMPROOT"
  exit 1
fi
pg_dump -U postgres -O -d $DB_NAME > "$TMPDIR/database.sql"
if [ $? -ne 0 ]; then
  echo "Error dumping database! (No snapshot created)"
  rm -fR "$TMPROOT"
  exit 1
fi
gtar cf "$TMPDIR/lucene_indices.tar" -C $BASE_DIR/openBIS-server/apache-tomcat indices
if [ $? -ne 0 ]; then
  echo "Error tarring lucene indices! (No snapshot created)"
  rm -fR "$TMPROOT"
  exit 1
fi

# Startup servers
$BASE_DIR/openBIS-server/apache-tomcat/bin/startup.sh
$BASE_DIR/datastore_server/datastore_server.sh start

# Package snapshot
gtar cf - -C "$TMPROOT" "$NAME" | bzip2 -c > "$NAME.tar.bz2"
if [ $? -ne 0 ]; then
  echo "Error tarring snapshot! (No snapshot created)"
  rm -fR "$TMPROOT"
  rm -f "$NAME.tar.bz2"
  exit 1
fi

# Clean up
rm -fR "$TMPROOT"
