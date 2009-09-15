#!/bin/sh
# author: Tomasz Pylak, 2007-09-27
# Implementation assumptions:
# - the current directory after calling a function does not change

# ----------------------------- configuration
TIME_TO_COMPLETE=60 # time (in seconds) needed by the whole pipeline to process everything
BIN_PATHS="/opt/local/bin /usr/bin /usr/sbin"
TRUE=1
FALSE=0
USER=`whoami`
DATABASE=openbis_integration_test

# all paths are relative to the template directory
TEMPLATE=templates
TARGETS=targets
TEST_DATA=testData
WORK=$TARGETS/playground
INSTALL=$TARGETS/install
LOCAL_PROJECTS=..

OPENBIS_SERVER_NAME=openBIS-server
OPENBIS_SERVER=$WORK/$OPENBIS_SERVER_NAME

DATA=$WORK/data
ERR_LOG=$WORK/all_err_log.txt

# ---- global state
TEST_FAILED=false # working variable, if true then some tests failed

# --------------------------- build distributions from sources

# Replaces the ':' in $PATH with ' '.
function get_env_path {
    echo $PATH | tr ":" " "
}

# Looks for a specified executable in environment paths and
# paths given as a parameter (space separated).
function locate_file {
    local file=$1
    shift
    local additional_paths=$@
    for dir in `get_env_path` $additional_paths; do 
	local full_path=$dir/$file
	if [ -x $full_path ]; then
    	    echo $full_path;
	    return
	fi 
    done
}

function run_svn {
    `locate_file svn $BIN_PATHS` $@
}

function run_lsof {
    `locate_file lsof $BIN_PATHS` $@
}

# Tries to find PostgreSQL executable and returns its absolute path.
# If not found, then exits the script with an appropriate error message.
function run_psql {
	for prg in psql psql84 psql83; do
		exe=`locate_file $prg $BIN_PATHS`
		if [ $exe ]; then
			echo $exe
			return
		fi
	done
	echo "Cannot find PostgreSQL"
	echo "This executable is needed to run the integration tests"
	exit 1
}

function build_zips {
    build_dss=$1
    build_dmv=$2
    build_openbis=$3
    use_local_source=$4

    if [ $build_dss == "true" -o $build_dmv == "true" -o $build_openbis == "true" ]; then
        mkdir -p $INSTALL
		if [ "$use_local_source" = "true" ]; then
    		build_zips_from_local $build_dss $build_dmv $build_openbis
    	else
	    	build_zips_from_svn $build_dss $build_dmv $build_openbis
		fi
    else
		echo "No components to build were specified (--help explains how to do this)."
		echo "Build process skipped."
    fi
    assert_file_exists_or_die "$INSTALL/openBIS*.zip"
    assert_file_exists_or_die "$INSTALL/datastore_server*.zip"
    assert_file_exists_or_die "$INSTALL/datamover*.zip"

}

function build_zips_from_local {
    build_dss=$1
    build_dmv=$2
    build_openbis=$3

    build_components build_local $build_dss $build_dmv $build_openbis
}

function build_local {
    local PROJECT_NAME=$1
    $LOCAL_PROJECTS/$PROJECT_NAME/build/antrun.sh
    mv $LOCAL_PROJECTS/$PROJECT_NAME/targets/dist/*.zip $INSTALL
}

function build_components {
    build_cmd=$1
    build_dss=$2
    build_dmv=$3
    build_openbis=$4

    if [ $build_dss == "true" ]; then
	rm -f $INSTALL/datastore_server*.zip
        $build_cmd datastore_server
    fi
    if [ $build_dmv == "true" ]; then
	rm -f $INSTALL/datamover*.zip
	$build_cmd datamover
    fi
    if [ $build_openbis == "true" ]; then
	rm -f $INSTALL/openBIS*.zip
        $build_cmd openbis
    fi
}

function build_remote {
    local RSC=$1
    local PROJECT_NAME=$2
    
    cd $RSC
    ./build.sh $PROJECT_NAME
    cd ..
}

function build_zips_from_svn {
    build_dss=$1
    build_dmv=$2
    build_openbis=$3

    RSC=build_resources
    rm -fr $RSC
    run_svn checkout svn+ssh://svncisd.ethz.ch/repos/cisd/build_resources/trunk $RSC
    build_components "build_remote $RSC" $build_dss $build_dmv $build_openbis
    mv $RSC/*.zip $INSTALL
    rm -fr $RSC 
}

# -------------------------- installation

# Recursively removes '.svn' directory in passed directory.
function clean_svn {
    local DIR=$1
    for file in `find $DIR -name ".svn"`; do 
	rm -fr $file; 
    done
}

function copy_templates {
    local template_dir=$1
    cp -fR $TEMPLATE/$template_dir $WORK
    clean_svn $WORK/$template_dir
}

function prepare {
    src=$1
    dest=$2
    rm -fr $WORK/$dest
    cp -R $WORK/$src $WORK/$dest
    copy_templates $dest
}

function unpack { # from ZIPS to BUILD
    local file_pattern=$1
    unzip -d $WORK $INSTALL/$file_pattern*
}

function remove_unpacked {
    rm -fR $WORK/$1
}

function check_server_port {
    run_lsof -i -n -P | grep 8443
}

function wait_for_server {
    echo -n "Server starting"
    i=0; 
    while [ "`check_server_port`" == "" -a $i -lt 20 ]; do 
	sleep 2; 
	echo -n "."; 
	let i=$i+1; 
    done
    if [ "`check_server_port`" == "" ]; then
	report_error "Server could not be started!"
	exit 1
    else
	echo "...[Done]"
    fi
}

function install_openbis_server {
    local install_openbis=$1
    psql_cmd=`run_psql`
    $psql_cmd -U postgres -c "drop database $DATABASE"
    $psql_cmd -U postgres -c "create database $DATABASE with owner $USER template = template0 encoding = 'UNICODE'"
    $psql_cmd -U $USER -d $DATABASE -f $TEMPLATE/$OPENBIS_SERVER_NAME/test_database.sql

    if [ $install_openbis == "true" ]; then
        rm -fr $OPENBIS_SERVER
	copy_templates $OPENBIS_SERVER_NAME
    
        unzip -d $OPENBIS_SERVER $INSTALL/openBIS*.zip
	$OPENBIS_SERVER/openBIS-server/install.sh $PWD/$OPENBIS_SERVER $OPENBIS_SERVER/service.properties $OPENBIS_SERVER/openbis.conf
	wait_for_server
    else
        copy_templates $OPENBIS_SERVER_NAME
        restart_openbis
    fi
}


function startup_openbis_server {
    call_in_dir bin/startup.sh $OPENBIS_SERVER/apache-tomcat
    wait_for_server
}

function shutdown_openbis_server {
    if [ "`check_server_port`" != "" ]; then
        $OPENBIS_SERVER/apache-tomcat/bin/shutdown.sh
    fi
}

# unpack everything, override default configuration with test configuation	
function install_dsss {
    local install_dss=$1
    if [ $install_dss == "true" ]; then
        unpack datastore_server
	prepare datastore_server datastore_server1
	prepare datastore_server datastore_server2
	remove_unpacked datastore_server
    else
	copy_templates datastore_server1
	copy_templates datastore_server2
    fi
}

function install_datamovers {
    local install_dmv=$1
    if [ $install_dmv == "true" ]; then
        unpack datamover
		prepare datamover datamover-raw
    	prepare datamover datamover-analysis
		remove_unpacked datamover
		cp -fR $TEMPLATE/dummy-img-analyser $WORK
		copy_templates datamover-raw
		copy_templates datamover-analysis
    else 
		copy_templates datamover-raw
		copy_templates datamover-analysis
    fi
}

function restart_openbis {
    assert_dir_exists_or_die $OPENBIS_SERVER
    shutdown_openbis_server
    sleep 1
    startup_openbis_server
    sleep 4
}

function install {
    local install_dss=$1
    local install_dmv=$2
    local install_openbis=$3
    local reinstall_all=$4

    mkdir -p $WORK
    if [ $reinstall_all == "true" ];then
	    install_dsss "true"
	    install_datamovers "true"
	    install_openbis_server "true"
    else
	    install_dsss $install_dss
	    install_datamovers $install_dmv
	    install_openbis_server $install_openbis
    fi
}


# ----------------------------- general

# calls $cmd script, changing directory to $dir
function call_in_dir {
    cmd=$1
    dir=$2
    
    prev=$PWD
    cd $dir
    sh $cmd
    cd $prev
}

function is_empty_dir {
    dir=$1
    if [ "`ls $dir`" = "" ]; then
	return 1;
    else
	return 0;
    fi
}

# ----------------------------- assertions

function init_log {
    rm -fr $ERR_LOG
}

function report_error {
    local msg=$@

    echo [ERROR] $msg | tee -a $ERR_LOG >&2
    TEST_FAILED="true"
}

function exit_if_assertion_failed {
    if [ "$TEST_FAILED" = "true" ]; then
	report_error Test failed.
	exit 1;
    else
	echo [OK] Test was successful!
    fi
}

function assert_file_exists {
    local file=$1
    if [ ! -f "$file" ]; then
	report_error File $file does not exist!  
    else
	echo [OK] File $file exists
    fi
}

function assert_same_inode {
    local file1=$1
    local file2=$2
    
    if [ $file1 -ef $file2 ]; then
        echo [OK] $file1 and $file2 have the same inode number.
    else
        report_error "$file1 and $file2 do not have the same inode number."
    fi
}

function assert_dir_exists {
    local DIR=$1
    if [ ! -d "$DIR" ]; then
	report_error Directory \"$DIR\" does not exist!  
    else
	echo [OK] Directory \"$DIR\" exists
    fi
}

function fatal_error {
    local MSG=$@
    report_error $MSG
    exit_if_assertion_failed
}

# remember to pass the parameter in quote marks
function assert_file_exists_or_die {
    local F="$1"
    local files_num=`ls -1 $F 2> /dev/null | wc -l`
    if [ $files_num -gt 1 ]; then
	fatal_error "One file expected for pattern $F, but more found: " $F
    else 
	if [ ! -f $F ]; then
	    fatal_error "No file matching pattern $F exists"
	fi
    fi
}

function assert_dir_exists_or_die {
    local DIR=$1
    if [ ! -d $DIR ]; then
	fatal_error "Directory $DIR does not exist!"
    fi
}

function assert_dir_empty {
    dir=$1
    is_empty_dir $dir
    empty=$?
    if [ $empty == 0 ]; then
	report_error Directory \'$dir\' should be empty!
    fi
}

function assert_same_content {
    local expected_file=$1
    local actual_file=$2
    cmd="diff --exclude=\.svn -r $expected_file $actual_file"
    supress=`eval $cmd`
    is_different=$?
    if [ $is_different == 1 ]; then
        report_error "Different content in $expected_file (marked by '<') and $actual_file (marked by '>')"
        eval $cmd
    else
        echo "[OK] Same content in $expected_file and $actual_file"
    fi
}

function assert_equals {
    local message=$1
    local expected_text=$2
    local actual_text=$3
    if [ "$expected_text" != "$actual_text" ]; then
        report_error "$message: expected: <$expected_text> but was: <$actual_text>"
    fi
}

function assert_equals_as_in_file {
    local expected_text=$1
    local file_with_actual_text=$2
    
    assert_file_exists $file_with_actual_text
    assert_equals "Content of file $file_with_actual_text" "$expected_text" "`cat $file_with_actual_text`"
}

function assert_pattern_present {
  local file=$1
  local occurences=$2
  local pattern=$3

  assert_file_exists $file
  echo Matched lines: 
  cat $file | grep "$pattern"  
  local lines=`cat $file | grep "$pattern" | wc -l`
  if [ $lines != $occurences ]; then
	report_error $lines instead of $occurences occurences of pattern $pattern found!
  else
	echo [OK] $occurences occurences of pattern $pattern found
  fi 
}

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


function copy_test_data {
    local NAME=$1
    local DIR=$2
    cp -RPp $TEST_DATA/$NAME $DIR
    clean_svn $DIR/$NAME
}


# ----------------------- Launching 

function chmod_exec {
    for file in $@; do
        if [ -f $file ]; then
	    chmod u+x $file
	fi
    done 
}


function switch_sth {
    switch_on=$1 # on/off
    dir=$WORK/$2
    cmd_start=$3
    cmd_stop=$4
    report_error=$5

    assert_dir_exists_or_die $dir
    chmod_exec $dir/$cmd_start
    chmod_exec $dir/$cmd_stop

    if [ "$switch_on" == "on" ]; then
	echo "Launching $dir..."
	rm -fr $dir/log/*
	call_in_dir "$cmd_start" $dir
    else
	echo "Stopping $dir, displaying errors from the log"
	if [ "`cat $dir/log/* | grep ERROR | tee -a $ERR_LOG`" != "" ]; then
	    if [ $report_error -eq $TRUE ]; then
	        report_error $dir reported errors.
	        cat $dir/log/* | grep ERROR >&2    
	    fi
	fi
	call_in_dir "$cmd_stop" $dir
    fi
}


function switch_dss {
    switch_sth $1 $2 datastore_server.sh shutdown.sh $FALSE
}

function switch_dmv {
    switch_sth $1 $2 "datamover.sh start" "datamover.sh stop" $TRUE
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

function assert_dss_registration {
    local dss=$1
    echo ==== assert registration of DSS $dss ====
    assert_pattern_present $WORK/$dss/log/datastore_server_log.txt 1 getVersion
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
                      $standard_dir/channel1/row11/column13/row3_column2.tiff
    assert_same_inode $original_data_set/TIFF/blabla_3VCP1_M03_2_w530.tif \
                      $standard_dir/channel2/row13/column3/row1_column2.tiff
                        
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
    assert_equals_as_in_file CISD $metadata_dir/sample/group_code
    assert_equals_as_in_file CISD $metadata_dir/sample/instance_code
	assert_file_exists $metadata_dir/sample/instance_uuid
    # Experiment identifier
    assert_equals_as_in_file CISD $metadata_dir/experiment_identifier/instance_code
    assert_equals_as_in_file CISD $metadata_dir/experiment_identifier/group_code
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

function assert_correct_dataset_content_in_database {
    local dataset_id=$1
    local pattern=$2
    echo ==== assert correct dataset $dataset_id content in database with pattern $pattern ====
    local psql=`run_psql`
    local dataset=`$psql -U postgres -d $DATABASE \
       -c "select d.id, e.code, ds.code, d.code, d.is_placeholder, r.data_id_parent, \
                  ed.is_complete, d.data_producer_code, d.production_timestamp \
           from data as d left join data_set_relationships as r on r.data_id_child = d.id \
                          left join data_stores as ds on ds.id = d.dast_id \
                          left join external_data as ed on ed.data_id = d.id,
                experiments as e
           where d.id = $dataset_id and d.expe_id = e.id"  \
       | awk '/ +[0-9]+/' \
       | awk '{gsub(/ /,"");print}' \
       | awk '{gsub(/\|/,";");print}'`
    local lines=`echo "$dataset" | grep "$pattern" | wc -l`
    if [ $lines == 0 ]; then
        report_error dataset does not match pattern $pattern: $dataset
    fi 
}
    
function assert_correct_content {
    assert_dss_registration datastore_server1
    assert_dss_registration datastore_server2
    assert_empty_in_out_folders
    assert_dir_exists $DATA/out-raw/microX_200801011213_3VCP1/TIFF
    assert_pattern_present $DATA/out-raw/.faulty_paths 1 ".*data/out-raw/.MARKER_is_finished_microX_200801011213_3VCP1"
    assert_pattern_present $WORK/datamover-raw/data-completed-info.txt 4 "Data complete.*3VCP[0-9]" 
    assert_correct_content_of_plate_3VCP1_in_store
    assert_correct_content_of_image_analysis_data 3VCP1 ".*-19.*3VCP1$"
    assert_correct_content_of_image_analysis_data 3VCP3 ".*-21.*3VCP3$"
    assert_correct_content_of_unidentified_plate_in_store UnknownPlate
    local file=`find_dataset_dir ".*-24$"`/original/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3.txt
    assert_equals_as_in_file "hello world" $file
    # result set columns are
    # id;experiment_code;data_store_code;code;is_placeholder;data_id_parent;is_complete;data_producer_code;production_timestamp
    assert_correct_dataset_content_in_database 2 "2;EXP1;DSS1;MICROX-3VCP1;f;;F;microX;2008-01-01.*"
    assert_correct_dataset_content_in_database 3 "3;EXP1;DSS1;20[0-9]*-19;f;;U;;"
    assert_correct_dataset_content_in_database 4 "4;EXP1;DSS1;20[0-9]*-20;f;;U;;"
    assert_correct_dataset_content_in_database 5 "5;EXP1;DSS1;20[0-9]*-21;f;;U;;"   
    assert_correct_dataset_content_in_database 6 "6;EXP1;DSS1;MICROX-3VCP3;f;;F;microX;2008-01-01.*"
    assert_correct_dataset_content_in_database 7 "7;EXP1;DSS1;20[0-9]*-23;f;;U;;"
    assert_correct_dataset_content_in_database 8 ".*8;EXP1;DSS2;20[0-9]*-24;f;2;U;;.*"
    assert_correct_dataset_content_in_database 8 ".*8;EXP1;DSS2;20[0-9]*-24;f;6;U;;.*"
    assert_equals "Content of file in drop box1" "hello world" "`cat $DATA/drop-box1/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3*-24.txt`"
    assert_equals "Content of file in drop box2" "hello world" "`cat $DATA/drop-box2/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3*-24.txt`"
}

function integration_tests {
    install_dss=$1
    install_dmv=$2
    install_openbis=$3
    use_local_source=$4
    reinstall_all=$5
    
    init_log
    # NOTE: Comment this line if you want to use different libraries.
    build_zips $install_dss $install_dmv $install_openbis $use_local_source
    
    # Prepare empty incoming data
    rm -fr $DATA
    mkdir -p $DATA
    cp -R $TEMPLATE/data $WORK
    clean_svn $DATA

    install $install_dss $install_dmv $install_openbis $reinstall_all
    launch_tests
    
    assert_correct_content
    
    shutdown_openbis_server
    exit_if_assertion_failed
}

function clean_after_tests {
    echo "Cleaning $INSTALL..."
    rm -fr $INSTALL
    echo "Cleaning $WORK..."
    rm -fr $WORK
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
