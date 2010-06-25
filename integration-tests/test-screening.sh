#!/bin/sh
#
# The integration test scenario for screening workflow.
#

# --- include external sources ------------------------ 

source common.bash

# --------------------

#WORK=$TARGETS/playground-screening
DATA_TEMPLATE=$TEMPLATE/data-screening.zip
MY_DATA=$WORK/data-screening
IMAGING_DB=imaging_integration_tests
OPENBIS_SERVER_HCS=$WORK/openBIS-server-screening
DSS_DIR_NAME=datastore_server_screening
DSS_INCOMING_PARENT_DIR=$WORK/$DSS_DIR_NAME/data
SVN_WEB_SRC_HCS=http://svncisd.ethz.ch/repos/cisd/screening/trunk
OPENBIS_DATABASE_HCS=openbis_screening

# --------------------

# Prepare template incoming data and some destination data structures
function prepare_data_first_phase {
		rm -fr $DSS_INCOMING_PARENT_DIR/incoming*
		unzip $DATA_TEMPLATE -d $DSS_INCOMING_PARENT_DIR -x incoming-analysis-genedata/*
		mkdir -p $DSS_INCOMING_PARENT_DIR/incoming-analysis-genedata
    chmod -R 700 $DSS_INCOMING_PARENT_DIR/incoming*
}

function prepare_data_second_phase {
		unzip $DATA_TEMPLATE -d $DSS_INCOMING_PARENT_DIR incoming-analysis-genedata/*
    chmod -R 700 $DSS_INCOMING_PARENT_DIR/incoming*
}

function build_and_install_components {
	rm -fr $INSTALL
	mkdir -p $INSTALL
	fetch_latest_artifacts_from_cruise_control screening $INSTALL
}

function install_openbis_server_screening {
    local install_openbis=$1
    
		local openbis_server_dir=$OPENBIS_SERVER_HCS
		local openbis_server_name=`basename $openbis_server_dir`
		
		restore_database $OPENBIS_DATABASE_HCS $TEMPLATE/$openbis_server_name/test_database.sql
    if [ $install_openbis == "true" ]; then
        rm -fr $openbis_server_dir
    
        unzip -d $openbis_server_dir $INSTALL/openBIS*.zip
        mv $openbis_server_dir/openBIS-server/* $openbis_server_dir
				rmdir $openbis_server_dir/openBIS-server
		
				$openbis_server_dir/install.sh $PWD/$openbis_server_dir
				startup_openbis_server $openbis_server_dir
				wait_for_server
    else
        restart_openbis $openbis_server_dir
    fi
}

function install_dss_screening {
		local dss_dest=$WORK/$DSS_DIR_NAME
		local dss_template=$TEMPLATE/$DSS_DIR_NAME

		rm -fr $dss_dest
		unzip $INSTALL/datastore_server-screening*.zip -d $dss_dest
		mv $dss_dest/datastore_server/* $dss_dest
		rmdir $dss_dest/datastore_server

		# extend distribution configuration
		cat $dss_template/integration-tests-service.properties >> $dss_dest/etc/service.properties
		cat $dss_template/genedata-dropboxes-service.properties >> $dss_dest/etc/service.properties
}

function build_and_install_screening {
		mkdir -p $WORK
		install_dss_screening
		install_openbis_server_screening "true"
    
		echo Dropping imaging database: $IMAGING_DB
		psql_cmd=`run_psql`
		$psql_cmd -U postgres -c "drop database if exists $IMAGING_DB" 
}

function check_file_exists {
    local file=$1
    if [ -e $marker ]; then echo true; else echo false; fi
}

function assert_correct_incoming_content {
	local dir_name=$1
	local expected_file_count=$2
	local has_error_marker=$3
	
	local dir=$MY_DATA/incoming/$dir_name
	assert_dir_exists $dir
	if [ ! -e $dir ]; then
		return
	fi
	
	assert_files_number $dir $expected_file_count
	
	local marker=$dir/_delete_me_after_correcting_errors
	local error_log=$dir/error-log.txt
	if [ "$has_error_marker" = "true" ]; then
		assert_file_exists $marker
		assert_file_exists $error_log
	else
		assert_file_not_exists $marker
		assert_file_not_exists $error_log
	fi
}


function integration_tests_screening {
		build_and_install_screening

    prepare_data_first_phase
    switch_dss "on" datastore_server_screening
		sleep 60
		prepare_data_second_phase
  	sleep 20
	  
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-analysis
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-analysis-genedata
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-images-genedata
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-images-merged-channels
		assert_dir_empty  $DSS_INCOMING_PARENT_DIR/incoming-images-split-channels
    
    switch_dss "off" datastore_server_screening
    shutdown_openbis_server $OPENBIS_SERVER_HCS
    exit_if_assertion_failed
}

function build_and_test {
	build_and_install_components
	integration_tests_screening
}

build_and_test
