#!/bin/bash


usage() {
	echo "Usage: $0  <database name> <entity perm id>"
	exit 1
}

check_arguments() {
	if [ $# -ne 2 ]; then
		usage
	fi
}

check_arguments $@
DB_NAME=$1
PERM_ID=$2

psql -d $DB_NAME -A -t -c \
"SELECT content FROM events WHERE identifiers LIKE '$PERM_ID, %' OR identifiers LIKE '%, $PERM_ID' OR identifiers LIKE '%, $PERM_ID,%' OR identifiers = '$PERM_ID'" | 
python formatter.py $PERM_ID
