For development and bug fixing the tests have to run in a Web browser. 

For this an openBIS instance has to start up. To get such a test instance running with all core-plugins with 
the test suits the build file 'build.gradle' has to be temporarily modified (do not check in this change!): 
Replace line 

options.suites('source/java/tests.xml') 

by

options.suites('source/java/tests-dev.xml') 

Then start up everything by executing in 'js-test'

./gradlew test

This will start up openBIS AS and two DSSs. Also the Web browser will open (URL: http://localhost:20000/openbis/)
and user admin will be automatically logged in.

Next choose a test suite in menu 'Utilities'. A tab will be opened which shows all tests.

If a test fails you can click on the test and only the failed test will be shown.

You can change the test code (e.g. in servers/common/core-plugins/tests/1/as/webapps/openbis-test/html/openbis-test.js).
To see the changes you have to reload the frame (not the application) in the Web browser.



==== The instructions below are probably out dated after the move from ANT to gradle

To run js tests manually:
- run create-webapp-common and run-webapp-common ant targets and wait until AS, DSS1 and DSS2 start up
- login as admin to openBIS at http://localhost:20000/openbis/ using Firefox browser
- enter Utilities->openbis-test.js and Utilities->openbis-screening-test.js to run js tests

To run automatic js tests on a new server:
- run create-webapp-common and run-automated-tests-on-new-server ant targets

To run automatic js tests on an existing and already running server:
- run create-webapp-common and run-automated-tests-on-existing-server ant targets

To run a lab specific webapp:
- download database dumps from lascar:/links/groups/cisd/js-test/XXX directory to
  appropriate local directories i.e. pathinfo_test_js_XXX.js to servers/XXX/datastore_server/db
  and openbis_test_js_XXX.js to servers/XXX/openBIS-server/db
- download and extract a store dump from lascar:/links/groups/cisd/js-test/XXX directory to
  local directory servers/XXX/datastore_server/data/store
- run create-webapp-XXX and run-webapp-XXX ant targets
- openBIS is running at http://localhost:20000/openbis/
