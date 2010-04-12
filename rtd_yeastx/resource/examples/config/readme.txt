This folder contains files needed to test yeatsx database locally.

To test yeastx you should:
- copy service.properties to 'etc' directory in datastore_server project
- copy 'targets/yeastx to the 'targets' directory in datastore_server project
- restore 'metabol_dbtest' database from the unizped metabol_dbtest.sql.zip file
- unpack 'yeastx-store.tgz' to the 'targets/yeastx' directory in datastore_server project
- use openbis_yeastx_db_test.sql as an openbis database