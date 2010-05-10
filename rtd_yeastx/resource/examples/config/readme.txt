This folder contains files needed to test yeatsx database locally.

To test yeastx you should:
- copy 'targets/yeastx to the 'targets' directory in rtd_yeastx project
- restore 'metabol_dev' database from metabol_dev.sql file
- unpack 'yeastx-store.tgz' to the 'targets/yeastx' directory in datastore_server project
- use openbis_yeastx_db_test.sql as an openbis database 
-- restore the database
-- change service.properties in openbis project to use the restored database
- launch openBIS web client and 'yeastx Datastore Server'.

 If you need more test data, see generate-test-data/generate-datasets.sh script.