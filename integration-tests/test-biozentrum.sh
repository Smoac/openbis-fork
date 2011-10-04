#!/bin/sh
#
# The integration test scenario for Biozentrum screening workflow (iBrain2 integration).
#

# --- include external sources ------------------------ 

source ./common.bash

ERR_LOG=$WORK/all_err_log.txt
SVN_WEB_SRC_HCS=http://svncisd.ethz.ch/repos/cisd/screening/trunk

WORK=$TARGETS/playground-screening-ibrain2
OPENBIS_DATABASE_HCS=openbis_screening_biozentrum_integration_tests
IMAGING_DB=imaging_biozentrum_integration_tests

# dir which contains AS and DSS configuration and AS core db dump
LOCAL_TEMPLATE=$TEMPLATE/data-biozentrum
DATA_TEMPLATE=$LOCAL_TEMPLATE/examples/ibrain2-dropboxes-test
OPENBIS_SERVER_HCS=$WORK/openBIS-server
DSS_DIR_NAME=datastore_server

DSS_SERVER_HCS=$WORK/$DSS_DIR_NAME
DSS_INCOMING_PARENT_DIR=$DSS_SERVER_HCS/data

source ./common-screening.bash

# --- Specific part -----------------

TEST_ROOT_DIR=$DSS_INCOMING_PARENT_DIR/dropboxes
INCOMING_ROOT_DIR=$TEST_ROOT_DIR/incoming
CONFIRMATION_DIR=$TEST_ROOT_DIR/registration-status

function copyDataset {
  local dropbox_type=$1
  local name=$2

  local dropbox=$INCOMING_ROOT_DIR/$dropbox_type
  
  echo Copy $name to $dropbox as $name
  if [ -e $dropbox/$name ]; then
     fatal_error Directory already exits in the dropbox!
  fi
  cp -r $DATA_TEMPLATE/$dropbox_type/$name $dropbox/$name
	clean_svn $dropbox/$name
}

function submitRawDataset {
  
  local dropbox=$INCOMING_ROOT_DIR/$1
  local name=$2
  
  copyDataset $1 $name
  touch $dropbox/.MARKER_is_finished_$name
}

function submitDerivedDataset {
  local dropbox=$INCOMING_ROOT_DIR/$1
  local name=$2
	local parent_dataset_code=$3
	  
  copyDataset $1 $name

	local key_pattern="storage_provider\.parent\.dataset\.id = "
	find $dropbox/$name -name "metadata.properties" -exec sed -i'' --in-place "s/$key_pattern.*/${key_pattern}${parent_dataset_code}/g" {} \;

  touch $dropbox/.MARKER_is_finished_$name
}

# returns 0 on success, 1 otherwise
function wait_for_confirmation_file {
	local file_name=$1
	
	wait_for_file $file_name
	local ok=$?
	
	if [ $ok == 0 ]; then
		 local succeeded=`cat $file_name | grep STORAGE_SUCCESSFUL`
		 if [ "$succeeded" == "" ]; then
		 		report_error `cat $file_name`
		 		return 1
		 else
		 		return 0
		 fi
	else
		return 1
	fi
}

function submit_correct_datasets_and_wait {
	echo Submit raw images dataset
	submitRawDataset HCS_IMAGE_RAW ibrain2_dataset_id_32

	CONF_FILE=$CONFIRMATION_DIR/ibrain2_dataset_id_32.properties
	wait_for_confirmation_file $CONF_FILE 
	
	# update parent dataset code in all files
	PARENT_CODE_KEY="storage_provider.dataset.id = "
	RAW_IMAGE_DATASET_CODE=`cat $CONF_FILE | grep "$PARENT_CODE_KEY" | tr -d "$PARENT_CODE_KEY"`
	echo Update raw image dataset code: $RAW_IMAGE_DATASET_CODE
	if [ "$RAW_IMAGE_DATASET_CODE" = "" ]; then
	   report_error Confirmation with code of raw image dataset has not been found
	   return
	fi
	
	echo Submit all derived datasets
	submitDerivedDataset HCS_IMAGE_OVERVIEW ibrain2_dataset_id_48 $RAW_IMAGE_DATASET_CODE 
	submitDerivedDataset HCS_IMAGE_SEGMENTATION ibrain2_dataset_id_99 $RAW_IMAGE_DATASET_CODE 
	submitDerivedDataset HCS_ANALYSIS_WELL_QUALITY_SUMMARY ibrain2_dataset_id_47 $RAW_IMAGE_DATASET_CODE
	submitDerivedDataset HCS_ANALYSIS_WELL_RESULTS_SUMMARIES ibrain2_dataset_id_77 $RAW_IMAGE_DATASET_CODE
	submitDerivedDataset HCS_ANALYSIS_CELL_FEATURES_CC_MAT ibrain2_dataset_id_58 $RAW_IMAGE_DATASET_CODE
	
	wait_for_confirmation_file $CONFIRMATION_DIR/ibrain2_dataset_id_48.properties
	wait_for_confirmation_file $CONFIRMATION_DIR/ibrain2_dataset_id_99.properties
	wait_for_confirmation_file $CONFIRMATION_DIR/ibrain2_dataset_id_47.properties
	wait_for_confirmation_file $CONFIRMATION_DIR/ibrain2_dataset_id_77.properties
	wait_for_confirmation_file $CONFIRMATION_DIR/ibrain2_dataset_id_58.properties
}

function getDropboxNamesList {
	echo "HCS_IMAGE_RAW HCS_IMAGE_OVERVIEW HCS_IMAGE_SEGMENTATION HCS_ANALYSIS_WELL_QUALITY_SUMMARY HCS_ANALYSIS_WELL_RESULTS_SUMMARIES HCS_ANALYSIS_CELL_FEATURES_CC_MAT"
}

function createEmptyDropoxes {
		rm -fr $TEST_ROOT_DIR
		for name in `getDropboxNamesList`; do
					mkdir -p $INCOMING_ROOT_DIR/$name
		done
		cp -r $LOCAL_TEMPLATE/dropboxes/scripts $TEST_ROOT_DIR
		mkdir $TEST_ROOT_DIR/registration-status
		mkdir $TEST_ROOT_DIR/tmp
}

function assert_datasets_in_store_number {
	local expected_number=$1
	
	local datasets=`find $DSS_INCOMING_PARENT_DIR/store -name "original*" | wc -l | tr -d " "`; 
  assert_equals "Wrong number of registered datasets" $expected_number $datasets
}

DEBUG=false

function integration_tests_screening_biozentrum {
    if [ "$DEBUG" == "false" ]; then 
			install_screening $LOCAL_TEMPLATE
		fi
		createEmptyDropoxes
    if [ "$DEBUG" == "false" ]; then 
    	switch_dss "on" $DSS_DIR_NAME
		fi
		submit_correct_datasets_and_wait
	  
	  for name in `getDropboxNamesList`; do
					assert_dir_empty $INCOMING_ROOT_DIR/$name
		done
	  
    assert_datasets_in_store_number 6

		# results_summaries dataset    
    assertFeatureVectorDef INTERPHASEINVASOMEINFECTION_INDEX InterphaseInvasomeInfection_Index
    assertFeatureVectorDef COUNT_BACTERIA Count_Bacteria
		# quality_summary dataset    
    assertFeatureVectorDef FOCUS_SCORE Focus_Score

    if [ "$DEBUG" == "false" ]; then 
    	switch_dss "off" $DSS_DIR_NAME
    	shutdown_openbis_server $OPENBIS_SERVER_HCS
    fi
    exit_if_assertion_failed
}

integration_tests_screening_biozentrum