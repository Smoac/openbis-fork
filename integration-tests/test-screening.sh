#!/bin/sh
#
# The integration test scenario for screening workflow.
#

# --- include external sources ------------------------ 

source common.bash

WORK=$TARGETS/playground-screening
ERR_LOG=$WORK/all_err_log.txt

# --------------------

SVN_WEB_SRC_HCS=http://svncisd.ethz.ch/repos/cisd/screening/trunk
OPENBIS_DATABASE_HCS=openbis_screening_integration_tests
IMAGING_DB=imaging_integration_tests


DATA_TEMPLATE=$TEMPLATE/data-screening.zip
OPENBIS_SERVER_HCS=$WORK/openBIS-server-screening
DSS_DIR_NAME=datastore_server_screening

DSS_SERVER_HCS=$WORK/$DSS_DIR_NAME
DSS_INCOMING_PARENT_DIR=$DSS_SERVER_HCS/data
API_HCS=$WORK/screening_api

# --------------------

# Prepare template incoming data and some destination data structures
function prepare_data_first_phase {
		rm -fr $DSS_INCOMING_PARENT_DIR/incoming*
		mkdir $DSS_INCOMING_PARENT_DIR/incoming-analysis-genedata
		mkdir $DSS_INCOMING_PARENT_DIR/incoming-images-merged-channels
		mkdir $DSS_INCOMING_PARENT_DIR/incoming-images-split-channels
		unzip -q $DATA_TEMPLATE -d $DSS_INCOMING_PARENT_DIR -x incoming-analysis-genedata/* incoming-images*channels/*
		mkdir -p $DSS_INCOMING_PARENT_DIR/incoming-analysis-genedata
		mkdir -p $DSS_INCOMING_PARENT_DIR/incoming-analysis
    chmod -R 700 $DSS_INCOMING_PARENT_DIR/incoming*
}

function prepare_data_second_phase {
		unzip -q $DATA_TEMPLATE -d $DSS_INCOMING_PARENT_DIR incoming-analysis-genedata/*
		unzip -q $DATA_TEMPLATE -d $DSS_INCOMING_PARENT_DIR incoming-images*channels/*
    chmod -R 700 $DSS_INCOMING_PARENT_DIR/incoming*
}


function fetch_distributions {
	rm -fr $INSTALL
	mkdir -p $INSTALL
	fetch_latest_artifacts_from_cruise_control screening $INSTALL
}

function install_and_run_openbis_server_screening {
    local install_openbis=$1
    
		local openbis_server_dir=$OPENBIS_SERVER_HCS
		local openbis_server_name=`basename $openbis_server_dir`
		
		restore_database $OPENBIS_DATABASE_HCS $TEMPLATE/$openbis_server_name/test_database.sql
    if [ $install_openbis == "true" ]; then
        rm -fr $openbis_server_dir
    
        unzip -q -d $openbis_server_dir $INSTALL/openBIS*.zip
        mv $openbis_server_dir/openBIS-server/* $openbis_server_dir
				rmdir $openbis_server_dir/openBIS-server

				cat $TEMPLATE/$openbis_server_name/integration-tests-service.properties >> $openbis_server_dir/service.properties
				$openbis_server_dir/install.sh $PWD/$openbis_server_dir
				startup_openbis_server $openbis_server_dir
				wait_for_server
    else
        restart_openbis $openbis_server_dir
    fi
}

function install_dss_screening {
		local dss_dest=$DSS_SERVER_HCS
		local dss_template=$TEMPLATE/$DSS_DIR_NAME

		rm -fr $dss_dest
		unzip -q $INSTALL/datastore_server-screening*.zip -d $dss_dest
		mv $dss_dest/datastore_server/* $dss_dest
		rmdir $dss_dest/datastore_server

		# extend distribution configuration
		cat $dss_template/integration-tests-service.properties >> $dss_dest/etc/service.properties
		cat $dss_template/genedata-dropboxes-service.properties >> $dss_dest/etc/service.properties	
}

function install_screening_api {
		rm -fr $API_HCS
		mkdir -p $API_HCS
		# unzip only jar files
		unzip -q $INSTALL/screening-api*.zip -x *.zip -d $API_HCS
}

function install_screening {
		fetch_distributions

		echo Dropping imaging database: $IMAGING_DB
		psql_cmd=`run_psql`
		$psql_cmd -U postgres -c "drop database if exists $IMAGING_DB" 
		
		rm -fr $WORK
		mkdir -p $WORK
		install_dss_screening
		install_and_run_openbis_server_screening "true"
    install_screening_api
}

function test_screening_api {
	cd $API_HCS
	. ./run.sh admin password https://localhost:8443
	if [ $? -ne 0 ]; then
       report_error Running screening API has failed
    else
       echo [OK] Screening API runs without exceptions.
	fi
	# return to the original directory
	cd ../../..
}


function integration_tests_screening {
		install_screening

    prepare_data_first_phase
    switch_dss "on" datastore_server_screening
    sleep 30
    assertSpotSizes "24x16,24x16" 
    prepare_data_second_phase
  	sleep 15
	  
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-analysis
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-analysis-genedata
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-images-genedata
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-images-merged-channels
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-images-split-channels
    
    local datasets=`find $DSS_INCOMING_PARENT_DIR/store -name "original" | wc -l | tr -d " "`; 
    assert_equals "Wrong number of registered datasets" 5 $datasets
    
    assertSpotSizes "24x16,24x16,24x16" 
    assertFeatureVectorDef HITRATE "Hit Rate"
    assertFeatureVectorDef CELLNUMBER cellNumber
    # TODO: add a check if the results are correct
    test_screening_api
    
    switch_dss "off" datastore_server_screening
    shutdown_openbis_server $OPENBIS_SERVER_HCS
    exit_if_assertion_failed
}

function assertSpotSizes {
    answer=`psql -tA --field-separator='x' --record-separator=',' -U postgres -d imaging_integration_tests \
            -c "select spots_width,spots_height from containers order by spots_width"`
    
    assert_equals "spot sizes" "$1" $answer 
}

function assertFeatureVectorDef {
    local psql=`run_psql`
    local result=`$psql -t -U postgres -d $IMAGING_DB \
       -c "select label from feature_defs where code = '$1'"  \
       | awk '{gsub(/\|/,";");print}'`
    assert_equals "Feature code and label" " $2" "$result"
}

# can be called after integration tests are done just to check the API results
function quick_api_test {
	startup_openbis_server $OPENBIS_SERVER_HCS
	wait_for_server
	switch_dss "on" datastore_server_screening

	test_screening_api
  
	switch_dss "off" datastore_server_screening
	shutdown_openbis_server $OPENBIS_SERVER_HCS
	exit_if_assertion_failed
}

integration_tests_screening
quick_api_test
