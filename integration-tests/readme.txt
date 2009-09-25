Author: Tomasz Pylak, 2007-09-26
		

Directories
-----------------
 templates - configuration of each component
 install - stores freshly built installation versions (*.zip)
 playground - temporary directory, here all tests take place

Launching
-----------------
Tests are run automatically in Continuous Integration process. 
You can also run them manually. To do that, simply launch run.sh from integration-tests project. 
Testing is fully automatic. Script returns non-zero value if error occurs. 
When testing is launched, following things happen:
- source code is fetched from the repository and is built
- all pieces of software are installed, configuration is copied from templates directory
- all pieces of software are launched
- Appropriate assertions are made to ensure the result is ok. 
Exact reason of an error can be checked by reading standard output or playground/all_err_log.txt.
Launching run.sh again will redo the tests without rebuilding or reinstalling anything. 
To reinstall everything without rebuilding binaries, delete playground directory.
If you want to starting tests from the scratch, launch run.sh with --force-rebuild option.

