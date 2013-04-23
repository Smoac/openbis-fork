#!/bin/bash

SCRIPT_PATH="`dirname \"$0\"`"
SCRIPT_PATH="`( cd \"$SCRIPT_PATH\" && pwd )`"

pg_dump -U postgres -O openbis_test_js > $SCRIPT_PATH/../../servers/openBIS-server/db/openbis_test_js.sql

pg_dump -U postgres -O pathinfo_test_js > $SCRIPT_PATH/../../servers/datastore_server/db/pathinfo_test_js.sql
pg_dump -U postgres -O imaging_test_js > $SCRIPT_PATH/../../servers/datastore_server/db/imaging_test_js.sql

pg_dump -U postgres -O pathinfo_test_js2 > $SCRIPT_PATH/../../servers/datastore_server2/db/pathinfo_test_js2.sql
pg_dump -U postgres -O imaging_test_js2 > $SCRIPT_PATH/../../servers/datastore_server2/db/imaging_test_js2.sql
