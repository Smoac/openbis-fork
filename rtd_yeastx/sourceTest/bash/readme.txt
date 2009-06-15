This directory contains resources which make the manual tests of dataset upload easier in the Eclipse environment.

Here is a list of the steps to perform the tests:
- run 'prepare_tests.sh'. It will:
   -- restore the database from the 'yeastx-tests-openbis-database.sql' file 
   -- clean some DSS directories
   -- compile the plugin and copy it to DSS dir
- prepare the content of the 'incoming' directory of your DSS by coping the content of 'incoming.tar.gz' archive
- configure openbis by setting 'database.kind' to 'yeastx_test'
- change the dss configuration using the one from 'dss-config' directory
- add dependency of datastore_server project to the file:
	datastore_server/source/datastore_server-plugins.jar file
- start openbis and DSS in your eclipse environment. Wait until DSS processes all datasets
- run the 'assert-correct-results.sh' script. 
   It checks that the content of the DSS incoming folder, store folder and dropboxes is equal to the expected one.
