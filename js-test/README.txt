To run js tests manually:
- run create-webapp-common and run-webapp-common ant targets and wait until AS, DSS1 and DSS2 start up
- login as admin to openBIS at http://localhost:20000/openbis/ using Firefox browser
- enter Utilities->openbis-test.js and Utilities->openbis-screening-test.js to run js tests

To run automatic js tests on a new server:
- run create-webapp-common and run-automated-tests-on-new-server ant targets

To run automatic js tests on an existing and already running server:
- run create-webapp-common and run-automated-tests-on-existing-server ant targets

To run a lab specific webapp:
- download database dumps from lascar:/links/groups/cisd/js-test/ directory to
  appropriate local directories i.e. pathinfo_test_js_XXX.js to servers/XXX/datastore_server/db
  and openbis_test_js_XXX.js to servers/XXX/openBIS-server/db
- run create-webapp-XXX and run-webapp-XXX ant targets
