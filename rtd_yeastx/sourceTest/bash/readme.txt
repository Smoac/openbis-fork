This directory contains resources which make the manual tests of dataset upload easier in the Eclipse environment.

Here is a list of the steps to perform the tests:
- restore the database from the 'yeastx-tests-openbis-database.sql' file and configure openbis to use it.
- change the dss configuration using the one from 'dss-config' directory
- run the 'jar' target of the rtd_yeatsx project, move the jar from targets/dist to datastore_server project 
   and add dependency to this library.
- start openbis and DSS in your eclipse environment
- copy the content of the 'incoming' to the incoming directory of your DSS and wait until DSS process them all
- run the 'assert-correct-results.sh' script. 
   It checks that the content of the DSS incoming folder, store folder and dropboxes is equal to the expected one.
