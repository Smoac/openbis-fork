Author: Tomasz Pylak, 2007-09-26
		Basil Neff, 2008-06-03 (section "integration test in branches with the datamover" created)
		
The integration test scenario 
    assumption: postgres is running on the local machine
-------------------
- lims server is launched
- lims client registers some cell plates 
- one etl server and one datamover is launched, one pair for raw data and one for image analysis data
- some data are generated for each cell plate
- 'raw' datamover moves the data, creating additional copy
- 'raw' etl server registers raw data
- dummy script does the image analysis and moves the data for 'analysis' datamover
- 'analysis' datamover moves the data
- 'analysis' etl server registers analysis data

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

Integration test in branches with the datamover
---------------------------------------------
Due to the fact, that the datamover is not part of our branch, we need a possibility
to test the integration test with a existing datamover distribution.
 
For this create the directory 'install' in the target directory and copy a distibution of the CISD datamover in it
which matches the pattern 'datamover-*.zip'. 

If you checked out the whole branch, you can run the integration test script with the followin parameter:
	./run.sh --etl --lims --local-source --reinstall-all