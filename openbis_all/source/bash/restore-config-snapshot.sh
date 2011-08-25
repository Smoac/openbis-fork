#! /bin/bash
# 
if [ $# -ne 2 ]; then
    echo "Usage: restore-config-snapshot.sh <servers> <config snapshot folder>"
    exit 1
fi

SERVERS="$1"
SNAPSHOT="$2"
for file in `find "$SNAPSHOT"`; do
    if [ -f $file ]; then
        cp -pf ${file} $SERVERS/${file#$SNAPSHOT}
    fi
done
