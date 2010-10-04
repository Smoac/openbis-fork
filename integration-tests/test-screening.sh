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
		rm -fr $INSTALL
		fetch_distributions screening

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
	. ./run.sh admin password https://localhost:8443 > api-client-log.txt
	if [ $? -ne 0 ]; then
       report_error Running screening API has failed
    else
       echo [OK] Screening API runs without exceptions.
	fi
	assert_pattern_present api-client-log.txt 1 "Experiments: \[/DEMO/DEMO_PROJECT/DEMO_EXPERIMENT \[20100623121102843-1\]\]"
	assert_pattern_present api-client-log.txt 1 "Plates: \[/DEMO/PLATE1 \[20100624113752213-5\]"
	assert_pattern_present api-client-log.txt 1 "Image datasets: \[[0-9]*-9"
	assert_pattern_present api-client-log.txt 1 "Feature vector datasets: \[[0-9]*-8 (plate: /DEMO/PLATE2 \[20100624113756254-6\]"
	assert_pattern_present api-client-log.txt 1 "Feature codes: \[CELLNUMBER, FEATRUE1, FEATRUE10, FEATRUE11, FEATRUE12, FEATRUE13, FEATRUE14, FEATRUE15, FEATRUE16, FEATRUE2, FEATRUE3, FEATRUE4, FEATRUE5, FEATRUE6, FEATRUE7, FEATRUE8, FEATRUE9, FRET, HITRATE, RFU645, RFU730, STD1, STD10, STD11, STD12, STD13, STD14, STD15, STD16, STD2, STD3, STD4, STD5, STD6, STD7, STD8, STD9\]"
	assert_pattern_present api-client-log.txt 1 "Loaded feature datasets: 2"
	assert_pattern_present api-client-log.txt 1 "features labels: \[cellNumber, featrue1, featrue10, featrue11, featrue12, featrue13, featrue14, featrue15, featrue16, featrue2, featrue3, featrue4, featrue5, featrue6, featrue7, featrue8, featrue9, Hit Rate, std1, std10, std11, std12, std13, std14, std15, std16, std2, std3, std4, std5, std6, std7, std8, std9\]"
	assert_pattern_present api-client-log.txt 1 "Features of the first dataset: datasetCode: [0-9]*-8"
	assert_pattern_present api-client-log.txt 1 "wellPosition: \[1, 2\], values: \[48.0, 0.0051865"
	assert_pattern_present api-client-log.txt 1 "Image metadata: \[Dataset [0-9]*-[0-9]* (plate: /DEMO/PLATE3 \[20100624113759640-7\]) has \[\[DAPI, GFP\]\] channels, 9 tiles\. Images resolution: 720x468"
	for imgFile in `find . -name *.png`; do
	  assert_pattern_present $imgFile 1 PNG
	done
	numberOfImages=`find . -name *.png|wc|awk '{print $1}'`
	assert_equals "number of images" 36 $numberOfImages
	
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
