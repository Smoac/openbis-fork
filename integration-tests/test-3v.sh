#!/bin/sh
# author: Franz-Josef Elmer, Tomasz Pylak
#
# The integration test scenario for screening workflow similar to the one used in 3V.
#     assumption: postgres is running on the local machine
# -------------------
# - lims server is launched
# - lims client registers some cell plates 
# - one etl server and one datamover is launched, one pair for raw data and one for image analysis data
# - some data are generated for each cell plate
# - 'raw' datamover moves the data, creating additional copy
# - 'raw' etl server registers raw data
# - dummy script does the image analysis and moves the data for 'analysis' datamover
# - 'analysis' datamover moves the data
# - 'analysis' etl server registers analysis data
#
# Integration test in branches with the datamover
# ---------------------------------------------
# Due to the fact, that the datamover is not part of our branch, we need a possibility
# to test the integration test with a existing datamover distribution.
#  
# For this create the directory 'install' in the target directory and copy a distibution of the CISD datamover in it
# which matches the pattern 'datamover-*.zip'. 
# 
# If you checked out the whole branch, you can run the integration test script with the followin parameter:
# 	./test-3v.sh --etl --lims --local-source --reinstall-all

TIME_TO_COMPLETE=60 # time (in seconds) needed by the whole pipeline to process everything
TEST_DATA=testData

# --- include external sources ------------------------ 

source common.bash

# ----------------------- Test data

function generate_test_data {
    echo Generate incoming data
    local DIR=$DATA/in-raw
    
    # drop an identifyable valid data set
    copy_test_data 3VCP1 $DIR
    sleep 30
    
    # drop 3VCP1 twice which should yield an error (same external data set code)
    copy_test_data 3VCP1 $DIR
    sleep 30
    
    # drop an identifyable invalid data set (wrong image name, missing plate)
    copy_test_data 3VCP3 $DIR
    sleep 30
    
    # drop an unidentifyable data set
    copy_test_data UnknownPlate $DIR
    sleep 30
    
    # drop 3VCP1 again but this time it is a valid data set
    copy_test_data 3VCP3 $DATA
    mv $DATA/3VCP3/TIFF/blabla_3VCP1_K13_8_w460.tif  $DATA/3VCP3/TIFF/blabla_3VCP3_K13_8_w460.tif
    echo image for well M03 > $DATA/3VCP3/TIFF/blabla_3VCP3_M03_2_w350.tif
    mv $DATA/3VCP3 $DIR 
    sleep 30
    
    # register not at a sample but at an experiment and two data set parents
    echo hello world > $DATA/incoming-a/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3.txt
}

# -----------------------

function copy_test_data {
    local NAME=$1
    local DIR=$2
    cp -RPp $TEST_DATA/$NAME $DIR
    clean_svn $DIR/$NAME
}


function switch_processing_pipeline {
    new_state=$1
    switch_dss $new_state datastore_server1
    sleep 5
    switch_dss $new_state datastore_server2
    switch_dmv $new_state datamover-analysis
    switch_sth $new_state dummy-img-analyser start.sh stop.sh $TRUE
    switch_dmv $new_state datamover-raw
}


function launch_tests {
    switch_processing_pipeline "on"
    sleep 4

    generate_test_data
    sleep $TIME_TO_COMPLETE

    switch_processing_pipeline "off"
}

function find_dataset_dir {
	local pattern=$1
	local dir=`find $DATA/main-store/E96C8910-596A-409D-BDA4-BBD3FE6629A7 -type d | grep "$pattern"`
        if [ "$dir" != "" ]; then
                if [ ! -d "$dir" ]; then
                        report_error Directory \"$dir\" does not exist!  
                else
                        echo $dir
                        return
	        fi
	fi
        report_error "$DATA/main-store/identified does not contain a directory matching $pattern: $dir"
}

function assert_empty_in_out_folders {
    echo ==== assert empty in/out folders ====
    assert_dir_empty $DATA/in-raw
    assert_dir_empty $DATA/in-analysis
    assert_dir_empty $DATA/out-analysis
    assert_dir_empty $DATA/analysis-copy
}

function assert_correct_content_of_plate_3VCP1_in_store {
    local cell_plate=3VCP1
    echo ==== assert correct content of plate 3VCP1 in store ====
    
    local raw_data_dir=`find_dataset_dir ".*-3VCP1$"`
    local raw_data_set=$raw_data_dir
    
    echo == check data structure version
    assert_equals_as_in_file 1 $raw_data_set/version/major
    assert_equals_as_in_file 1 $raw_data_set/version/minor
    
    echo == check annotations
    local annotations_dir="$raw_data_set/annotations"
    assert_dir_exists "$annotations_dir"
    assert_equals_as_in_file 460 $annotations_dir/channel1/wavelength
    assert_equals_as_in_file 530 $annotations_dir/channel2/wavelength
    
    echo == check original data
    local original_data_set=$raw_data_set/data/original/microX_200801011213_3VCP1
    assert_dir_exists $original_data_set
    assert_same_content $TEST_DATA/3VCP1 $original_data_set
    
    echo == check standard data
    local standard_dir=$raw_data_set/data/standard
    assert_dir_exists $standard_dir
    assert_same_inode $original_data_set/TIFF/blabla_3VCP1_K13_8_w460.tif \
                      $standard_dir/channel1/row11/column13/row1_column2.tiff
    assert_same_inode $original_data_set/TIFF/blabla_3VCP1_M03_2_w530.tif \
                      $standard_dir/channel2/row13/column3/row3_column2.tiff
                        
    echo == check metadata
    local metadata_dir=$raw_data_set/metadata
    assert_dir_exists $metadata_dir
    # Data set
    assert_equals_as_in_file microX-3VCP1 $metadata_dir/data_set/code
    assert_equals_as_in_file TRUE $metadata_dir/data_set/is_measured
    assert_equals_as_in_file FALSE $metadata_dir/data_set/is_complete
    assert_equals_as_in_file HCS_IMAGE $metadata_dir/data_set/data_set_type
    assert_equals_as_in_file microX $metadata_dir/data_set/producer_code
    # Sample
    assert_equals_as_in_file 3VCP1 $metadata_dir/sample/code
    assert_equals_as_in_file CELL_PLATE $metadata_dir/sample/type_code
    assert_equals_as_in_file 'Screening Plate' $metadata_dir/sample/type_description
    assert_equals_as_in_file CISD $metadata_dir/sample/space_code
    assert_equals_as_in_file CISD $metadata_dir/sample/instance_code
	assert_file_exists $metadata_dir/sample/instance_uuid
    # Experiment identifier
    assert_equals_as_in_file CISD $metadata_dir/experiment_identifier/instance_code
    assert_equals_as_in_file CISD $metadata_dir/experiment_identifier/space_code
    assert_equals_as_in_file NEMO $metadata_dir/experiment_identifier/project_code
    assert_equals_as_in_file EXP1 $metadata_dir/experiment_identifier/experiment_code
    assert_file_exists $metadata_dir/experiment_identifier/instance_uuid
    # Experiment registration
    assert_file_exists $metadata_dir/experiment_registration_timestamp
    assert_file_exists $metadata_dir/experiment_registrator/email
    assert_file_exists $metadata_dir/experiment_registrator/first_name
    assert_file_exists $metadata_dir/experiment_registrator/last_name
    # Format
    assert_equals_as_in_file HCS_IMAGE $metadata_dir/format/code
    assert_equals_as_in_file 1 $metadata_dir/format/version/major
    assert_equals_as_in_file 0 $metadata_dir/format/version/minor
    assert_pattern_present $metadata_dir/md5sum/original 1 ".* microX_200801011213_3VCP1/log.txt"
    assert_pattern_present $metadata_dir/md5sum/original 1 ".* microX_200801011213_3VCP1/TIFF/blabla_3VCP1_K13_8_w460.tif"
    assert_pattern_present $metadata_dir/md5sum/original 1 ".* microX_200801011213_3VCP1/TIFF/blabla_3VCP1_M03_2_w530.tif"
    assert_pattern_present $metadata_dir/md5sum/original 1 ".* microX_200801011213_3VCP1/TIFF/readme-not.txt"
    assert_file_exists $metadata_dir/standard_original_mapping
    
    echo == check format parameters
    local parameters_dir=$metadata_dir/parameters
    assert_dir_exists $parameters_dir
    assert_equals_as_in_file TRUE $parameters_dir/contains_original_data
    assert_equals_as_in_file 2 $parameters_dir/number_of_channels
    assert_equals_as_in_file 24 $parameters_dir/plate_geometry/columns
    assert_equals_as_in_file 16 $parameters_dir/plate_geometry/rows
    assert_equals_as_in_file 3 $parameters_dir/well_geometry/columns
    assert_equals_as_in_file 3 $parameters_dir/well_geometry/rows
}

function assert_correct_content_of_invalid_plate_in_store {
    local cell_plate=$1
    echo ==== assert correct content of invalid plate $cell_plate in store ====
    
    local error_dir=$DATA/main-store/error/DataSetType_HCS_IMAGE
    assert_dir_exists $error_dir
    local data_set=$error_dir/microX_200801011213_$cell_plate
    assert_same_content $TEST_DATA/$cell_plate $data_set
    assert_file_exists $data_set.exception
}
    
function assert_correct_content_of_image_analysis_data {
    local cell_plate=$1
    local pattern=$2
    
    echo ====  check image analysis data for cell plate $cell_plate ====
    local plate_with_img_analysis=`find_dataset_dir $pattern`
    assert_same_content $TEST_DATA/$cell_plate $plate_with_img_analysis
}

function assert_correct_content_of_unidentified_plate_in_store {
    local cell_plate=$1
    echo ==== assert correct content of unidentified plate $cell_plate in store ====
    
    local unidentified_dir=$DATA/main-store/unidentified
    assert_dir_exists $unidentified_dir
    assert_same_content $TEST_DATA/$cell_plate $unidentified_dir/DataSetType_HCS_IMAGE/microX_200801011213_$cell_plate
    assert_same_content $TEST_DATA/$cell_plate $unidentified_dir/DataSetType_HCS_IMAGE_ANALYSIS_DATA/microX_200801011213_$cell_plate
}

function assert_correct_content {
    assert_dss_registration datastore_server1
    assert_dss_registration datastore_server2
    assert_empty_in_out_folders
    assert_dir_exists $DATA/out-raw/microX_200801011213_3VCP1/TIFF
    assert_pattern_present $DATA/out-raw/.faulty_paths 1 ".*data/out-raw/.MARKER_is_finished_microX_200801011213_3VCP1"
    assert_pattern_present $WORK/datamover-raw/data-completed-info.txt 4 "Data complete.*3VCP[0-9]" 
    assert_correct_content_of_plate_3VCP1_in_store
    assert_correct_content_of_image_analysis_data 3VCP1 ".*-22.*3VCP1$"
    assert_correct_content_of_image_analysis_data 3VCP3 ".*-24.*3VCP3$"
    assert_correct_content_of_unidentified_plate_in_store UnknownPlate
    local file=`find_dataset_dir ".*-27$"`/original/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3.txt
    assert_equals_as_in_file "hello world" $file
    # result set columns are
    # id;experiment_code;data_store_code;code;is_placeholder;data_id_parent;is_complete;data_producer_code;production_timestamp
    assert_correct_dataset_content_in_database 2 "2;EXP1;DSS1;MICROX-3VCP1;f;;F;microX;2008-01-01.*"
    assert_correct_dataset_content_in_database 3 "3;EXP1;DSS1;20[0-9]*-22;f;;U;;"
    assert_correct_dataset_content_in_database 4 "4;EXP1;DSS1;20[0-9]*-23;f;;U;;"
    assert_correct_dataset_content_in_database 5 "5;EXP1;DSS1;20[0-9]*-24;f;;U;;"   
    assert_correct_dataset_content_in_database 6 "6;EXP1;DSS1;MICROX-3VCP3;f;;F;microX;2008-01-01.*"
    assert_correct_dataset_content_in_database 7 "7;EXP1;DSS1;20[0-9]*-26;f;;U;;"
    assert_correct_dataset_content_in_database 8 ".*8;EXP1;DSS2;20[0-9]*-27;f;2;U;;.*"
    assert_correct_dataset_content_in_database 8 ".*8;EXP1;DSS2;20[0-9]*-27;f;6;U;;.*"
    assert_equals "Content of file in drop box1" "hello world" "`cat $DATA/drop-box1/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3*-27.txt`"
    assert_equals "Content of file in drop box2" "hello world" "`cat $DATA/drop-box2/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3*-27.txt`"
}

function print_help {
    echo "Usage: $0 [ (--dss | --openbis | --dmv)* | --all [ --local-source ]]"
    echo "	--dss, --openbis, --dmv	build chosen components only"
    echo "	--all 			build all components"
    echo "	--local-source		use local source code during building process instead of downloading it from svn"
    echo "	--reinstall-all		reinstalls all packeges new from the zip file which is in the installation direcory (also reinstall the packages which are not build)"  
    echo "	--assert-content	only checks content"
    echo "	--clean			clean and exit"
    echo "	--help			displays this help"
    echo "If no option is given, integration tests will be restarted without building anything."
    echo "Examples:"
    echo "- Rebuild everything, fetch sources from svn:"
    echo "	$0 --all"
    echo "- Use openbis server and client installation from previous tests, rebuild data store server and datamover using local source:"
    echo "	$0 --dss --dmv --local-source"
    echo "- Rebuild data store server only fetching sources from svn:"
    echo "	$0 --dss"
}

# Prepare template incoming data and some destination data structures
function prepare_data {
    DATA=$WORK/data
    rm -fr $DATA
    mkdir -p $DATA
    cp -R $TEMPLATE/data $WORK
    clean_svn $DATA
    
    rm -f $WORK/datamover-raw/data-completed-info.txt
}

function integration_tests {
    prepare_data
    build_and_install $@

    launch_tests
    
    assert_correct_content
    
    shutdown_openbis_server
    exit_if_assertion_failed
}

# -- MAIN ------------

if [ "$1" = "--clean" ]; then
    clean_after_tests
else
    install_dss=false
    install_dmv=false
    install_openbis=false
    use_local_source=false
    reinstall_all=false
    while [ ! "$1" = "" ]; do
	case "$1" in
	    '-e'|'--dss')
	        install_dss=true
		;;
	    '-d'|'--dmv')
		install_dmv=true
		;;
	    '-o'|'--openbis')
		install_openbis=true
		;;
	    '-a'|'--all')
	        install_dss=true
		install_dmv=true
		install_openbis=true
		;;
	    '--local-source')
		use_local_source=true
		;;
		'--reinstall-all')
		reinstall_all=true
		;;			
	    '--help')
		print_help
		exit 0
		;;
		'--assert-content')
		assert_correct_content
		exit 0
		;;
	    *)
		echo "Illegal option $1."
		print_help
		exit 1
		;;
         esac
	 shift
    done
    integration_tests $install_dss $install_dmv $install_openbis $use_local_source $reinstall_all
fi
