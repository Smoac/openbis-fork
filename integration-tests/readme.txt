Author: Tomasz Pylak, 2007-09-26

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
Simply run ant with default target in build directory to start tests. Target 'clean' cleans everything afterwards.  
Testing is fully automatic. Script returns non-zero value if error occurs. 
When testing is launched, following things happen:
- source code is fetched from the repository and is built
- all pieces of software are installed, configuration is copied from templates directory
- all pieces of software are launched
- Approriate assertions are made to ensure the result is ok. 
Exact reason of an error can be checked by reading standard output or playground/all_err_log.txt.
If you want to restart tests without building from source, delete playground directory and leave install directory untouched.
 
