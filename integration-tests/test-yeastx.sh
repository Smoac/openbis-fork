#!/bin/sh
#
# The integration test scenario for yeastx workflow.
#

# --- include external sources ------------------------ 

source common.bash

# --------------------

MY_DATA=$WORK/data-yeastx
TEMPLATE_INCOMING_CONTENT=$MY_DATA/expected-output/template_incoming_content_yeastx.txt
METABOL_DB=metabol_dev

# --------------------

function build_and_install_yeastx {
    local use_local_source=$1

    local install_dss=true
    local install_dmv=false
    local install_openbis=true
    local reinstall_all=false
# TODO: UNCOMMENT it !!!!!!!!!!!!!!!!
#    build_and_install $install_dss $install_dmv $install_openbis $use_local_source $reinstall_all
		build_and_install false $install_dmv false $use_local_source $reinstall_all
		
		cp $INSTALL/datastore_server-plugins.jar $WORK/datastore_server_yeastx/lib/
		chmod_exec $WORK/datastore_server_yeastx/takeCifsOwnershipRecursive.sh
		
		# Prepare empty incoming data
    rm -fr $MY_DATA
    mkdir -p $MY_DATA
    cp -R $TEMPLATE/data-yeastx/* $MY_DATA/
    clean_svn $MY_DATA
    
		echo Dropping metabolomics database
		psql_cmd=`run_psql`
		$psql_cmd -U postgres -c "drop database $METABOL_DB" 
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

# prints all the error messages from incoming folder in a canonic form 
function print_incoming_errorlog_fingerprint {
	local incoming_dir=$1

	for dir in `ls -1 $incoming_dir | sort`; do
			echo $dir --------------------
	    local error_log=$incoming_dir/$dir/error-log.txt
	    if [ -e $error_log ]; then
	        while read line; do
	            # remove all prefixes with dates
	            line=${line//*ERROR/ERROR} 
	            # remove all warnings
	            line=${line//*WARNING*/} 
	            echo $line
	        done < $error_log
	  	fi
	done
}

# checks that the error messages for the folders which could not be processed 
# in the incomming folder are correct
function assert_correct_errorlogs {
	local incoming_dir=$1
	
	local errorlogs=$WORK/incoming_current_content.txt
	print_incoming_errorlog_fingerprint $incoming_dir > $errorlogs
	local diff_cmd="diff -w $TEMPLATE_INCOMING_CONTENT $errorlogs"
	
	if [ ! "`$diff_cmd`" == "" ]; then
		report_error Unexpected content of the incoming directory $incoming_dir:
		$diff_cmd
	fi
}

function count_db_table_records {
	local db=$1
	local table=$2
	
  local psql=`run_psql`
  local count=`$psql -U postgres -d $db -c "select count(*) from $table"  \
       | head -n 3 | tail -n 1 | awk '{gsub(/ /,"");print}'
	echo $count
}

function assert_correct_datasets_metabol_database {
    local eicms_runs=`count_db_table_records $METABOL_DB eic_ms_runs`
    local fiams_runs=`count_db_table_records $METABOL_DB fia_ms_runs`

    assert_equals "Wrong number of eic MS runs in the metablomics db" 1 $eicms_runs  
    assert_equals "Wrong number of fia MS runs in the metablomics db" 1 $fiams_runs
}

function assert_correct_incoming_contents {
	local incoming_dir=$1

	# check content of incoming directory
  assert_files_number $incoming_dir 12
	assert_correct_incoming_content faulty-duplicated-mapping 4 true
	assert_correct_incoming_content faulty-experiment-code 4 true
	assert_correct_incoming_content faulty-mapped-file-does-not-exist 3 true
	assert_correct_incoming_content faulty-no-email-specified 3 true
	assert_correct_incoming_content faulty-no-mapping 4 true
	assert_correct_incoming_content faulty-non-unique-mapping 6 true
	assert_correct_incoming_content faulty-to-many-mapping-files 4 true
	assert_correct_incoming_content faulty-unknow-property 4 true
	assert_correct_incoming_content faulty-unknown-mapping 8 true
	assert_correct_incoming_content faulty-wrong-conversion 6 true
	assert_correct_incoming_content ignore-empty-dir 0 false
	assert_correct_incoming_content ignore-no-index 1 false
	
	assert_correct_errorlogs $incoming_dir
	
	# check content of dropboxes
	assert_files_number "$MY_DATA/dropbox-eicml/TEST&TEST_PROJECT&EXP_TEST.*.mzXML" 6
	assert_files_number "$MY_DATA/dropbox-fiaml/TEST&TEST_PROJECT&EXP_TEST.*.mzXML" 2

	# check content of the store	
	local store=$MY_DATA/store
	local store_files_count=`find $store -type f | wc -l`
	assert_equals "Wrong number of files in the store $store" 14 $store_files_count
	
	assert_correct_datasets_metabol_database
}


function integration_tests_yeastx {
    local use_local_source=$1

    build_and_install_yeastx $use_local_source
    switch_dss "on" datastore_server_yeastx

		sleep 90
    assert_correct_incoming_contents $MY_DATA/incoming
    
    switch_dss "off" datastore_server_yeastx
    shutdown_openbis_server
    exit_if_assertion_failed
}

# TODO
integration_tests_yeastx true
#assert_correct_incoming_contents $MY_DATA/incoming
#exit_if_assertion_failed
