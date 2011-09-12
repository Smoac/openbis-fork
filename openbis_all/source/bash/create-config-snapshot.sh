#! /bin/bash
# 
# Usage: create-config-snapshot.sh <servers> <snapshot repository> <config file list 1> ... <config file list n> 
# 
# where
# <servers> is the path to the directory containing the server folders 'openBIS-server' 
#    and 'datastore_server',
# <snapshot repository> is the path to the directory which will store the snapshot,
# <config file list i> is a list of relative paths to files/folders relative to <servers> 
#    which should be copied into the snapshot. Each line contains a path. 
#    Empty lines and lines starting with '#' are ignored
# 
# 
set -o nounset
set -o errexit

if [ $# -lt 3 ]; then
    echo "Usage: create-config-snapshot.sh <servers> <snapshot repository> <config file list 1> ... <config file list n>"
    exit 1
fi

##################################################
#
# Gathering parameters
#
SERVERS="$1"
REPOSITORY="$2"
shift 2

TIMESTAMP=`date +%Y-%m-%d_%H%M%S`
SNAPSHOT_FOLDER_NAME="openbis-config-snapshot-$TIMESTAMP"
SNAPSHOT="$REPOSITORY/$SNAPSHOT_FOLDER_NAME"

##################################################
#
# Creating config snapshot
#
echo "==== Creating config snapshot $SNAPSHOT"
mkdir -p "$SNAPSHOT"
for config_file in $*; do
    echo "Save config files/folders listed in $config_file"
    if [ ! -f "$config_file" ]; then
        echo "Unknown config file: $config_file. Config snapshot creation aborted."
        exit 1
    fi
    while read line ; do
        if [ -z "$line" ]; then
            continue
        fi
        if [ "${line:0:1}" == "#" ]; then
            continue
        fi
        file="$SERVERS/$line"
        folder="$SNAPSHOT/${line%/*}"
        if [ -e "$file" ]; then
            mkdir -p "$folder"
            cp -pfr "$file" "$folder"   
            if [ $? -ne 0 ]; then
                echo "Error while copying $file -> $folder. Config snapshot creation aborted."
                exit 1
            fi
        fi
    done < $config_file
done
echo "==== Config snapshot successfully created"