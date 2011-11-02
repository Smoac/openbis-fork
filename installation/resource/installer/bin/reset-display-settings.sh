#!/bin/bash
# Use with care!
# Overrides all user display settings with the settings of the template user.

echo Remove 'exit' after configuring existing template user name($TEMPLATE_USER), database username ($DB_USERNAME) and openbis database ($OPENBIS_DB)
exit 1
TEMPLATE_USER=openbis-user-name
DB_USER_NAME=$(whoami)
OPENBIS_DB=openbis_screening

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

psql -U $DB_USER_NAME -d $OPENBIS_DB -c "update persons set display_settings = (select display_settings from persons where user_id = '$TEMPLATE_USER') where user_id != '$TEMPLATE_USER'"
