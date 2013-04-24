To run js tests manually:
- run create-and-run-webapp ant target and wait until AS, DSS1 and DSS2 start up
- login as admin to openBIS at http://localhost:20000/openbis/ using Firefox browser
- enter Utilities->openbis-test.js and Utilities->openbis-screening-test.js to run js tests

To run automatic js tests on a new server:
- run create-webapp and run-tests-js-on-new-server ant targets

To run automatic js tests on an existing and already running server:
- run create-webapp and run-tests-js-on-existing-server ant targets