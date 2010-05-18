#! /bin/bash

TARGET_USER="$1"
SOURCE_USER="$2"


if [ -z "$TARGET_USER" -o -z "SOURCE_USER" ];
then
   echo "Syntax: $0 TARGET_USER SOURCE_USER"
   exit 1 
fi

psql -d openbis_productive -c "update persons set display_settings=(select display_settings from persons where user_id='${SOURCE_USER}') where user_id='${TARGET_USER}'"
