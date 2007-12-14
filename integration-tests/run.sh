#!/bin/sh
# author: Tomasz Pylak, 2007-09-27
# Implementation assumptions:
# - the current directory after calling a function does not change

# ----------------------------- configuration
TIME_TO_COMPLETE=40 # time (in seconds) needed by the whole pipeline to process everything
SVN_PATHS="/opt/local/bin /usr/bin"
LSOF_PATHS="/usr/sbin"

# all paths are relative to the template directory
TEMPLATE=templates
TARGETS=targets
WORK=$TARGETS/playground
INSTALL=$TARGETS/install

LIMS_SERVER_NAME=openBIS-server
LIMS_SERVER=$WORK/$LIMS_SERVER_NAME
LIMS_CLIENT_NAME=openBIS-client
LIMS_CLIENT=$WORK/$LIMS_CLIENT_NAME

DATA=$WORK/data
ERR_LOG=$WORK/all_err_log.txt

# ---- global state
TEST_FAILED=false # working variable, if true then some tests failed

# -------------------------- installation

function get_env_path {
    echo $PATH | tr ":" " "
}

# looks for a specified file in environment paths and paths given as a parameter (space separated)
function locate_file {
    local file=$1
    shift
    local additional_paths=$@
    for dir in `get_env_path` $additional_paths; do 
	local full_path=$dir/$file
	if [ -f $full_path ]; then
    	    echo $full_path;
	    return
	fi 
    done
}

function run_svn {
    `locate_file svn $SVN_PATHS` $@
}

function build_zips {
    RSC=build_resources
    rm -fr $RSC
    run_svn checkout svn+ssh://source.systemsx.ch/repos/cisd/build_resources/trunk $RSC
    cd $RSC
    ./build.sh lims_webclient
    ./build.sh datamover
    ./build.sh etlserver
    cd ..
    rm -fr $INSTALL
    mkdir -p $INSTALL
    mv $RSC/*.zip $INSTALL
    rm -fr $RSC 
}

function clean_svn {
    local DIR=$1
    for file in `find $DIR -name ".svn"`; do 
	rm -fr $file; 
    done
}

function prepare {
    src=$1
    dest=$2
    cp -R $WORK/$src $WORK/$dest
    cp -fR $TEMPLATE/$dest $WORK
    clean_svn $WORK/$dest
}

function unpack { # from ZIPS to BUILD
    file_pattern=$1
    unzip -d $WORK $INSTALL/$file_pattern*
}

function remove_unpacked {
    rm -fR $WORK/$1
}

function run_lsof {
    `locate_file lsof $LSOF_PATHS` $@
}

function check_server_port {
    run_lsof -i -n -P | grep 8443
}

function wait_for_server {
    echo -n "Server starting"
    i=0; 
    while [ "`check_server_port`" == "" -a $i -lt 5 ]; do 
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

function install_lims_server {
    cp -R $TEMPLATE/$LIMS_SERVER_NAME $WORK
    
    unzip -d $LIMS_SERVER $INSTALL/openBIS-server*.zip
    $LIMS_SERVER/openBIS-server/install.sh $PWD/$LIMS_SERVER $LIMS_SERVER/service.properties $LIMS_SERVER/roles.conf
    wait_for_server
}


function startup_lims_server {
    call_in_dir bin/startup.sh $LIMS_SERVER/apache-tomcat
    wait_for_server
}

function shutdown_lims_server {
    if [ "`check_server_port`" != "" ]; then
        $LIMS_SERVER/apache-tomcat/bin/shutdown.sh
    fi
}

function register_cell_plates {
    call_in_dir load-lims-data.sh $LIMS_CLIENT
}

function install_lims_client {
    unpack openBIS-client
    cp -fR $TEMPLATE/$LIMS_CLIENT_NAME $WORK
}

# unpack everything, override default configuration with test configuation	
function install_etls {
    unpack etlserver
    prepare etlserver etlserver-raw
    prepare etlserver etlserver-analys
    remove_unpacked etlserver
}

function install_datamovers {
    unpack datamover
    prepare datamover datamover-raw
    prepare datamover datamover-analys
    remove_unpacked datamover
    cp -fR $TEMPLATE/dummy-img-analyser $WORK
}

function install {
    rm -fr $WORK
    mkdir -p $WORK
    
    install_etls
    install_datamovers
    install_lims_server
    install_lims_client
    register_cell_plates
}

function restart_lims {
    shutdown_lims_server
    sleep 1
    startup_lims_server
    sleep 4
    register_cell_plates
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

    echo [ERROR] $msg | tee -a $ERR_LOG    
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

function assert_dir_exists {
    local DIR=$1
    if [ ! -d "$DIR" ]; then
	report_error $DIR does not exist!  
    else
	echo [OK] $DIR exists
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


function assert_pattern_present {
  local file=$1
  local occurences=$2
  local pattern=$3

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

function create_test_data_file {
    local FILE_PATH=$1
    local file_size=2000000
    openssl rand -base64 $file_size -out $FILE_PATH
}

function create_test_data_dir {
    local NAME=$1
    local DIR=$2
    mkdir $DIR/$NAME
    local i=0  
    while [  $i -lt 18 ]; do
	create_test_data_file $DIR/$NAME/$NAME-data$i.txt
	let i=i+1 
    done
}

function generate_test_data {
    echo Generate incoming data
    local DIR=$DATA/in-raw
    create_test_data_dir "3VCP1" $DIR
    create_test_data_dir "3VCP3" $DIR
    create_test_data_dir "3VCP4" $DIR
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
    chmod_exec $dir/$cmd_start
    chmod_exec $dir/$cmd_stop

    if [ "$switch_on" == "on" ]; then
	echo "Launching $dir..."
	rm -fr $dir/log/*
	call_in_dir "$cmd_start" $dir
    else
	echo "Stopping $dir, displaying errors from the log"
	if [ "`cat $dir/log/* | grep ERROR | tee -a $ERR_LOG`" != "" ]; then
	    report_error $dir reported errors.
	    cat $dir/log/* | grep ERROR	    
	fi
	call_in_dir "$cmd_stop" $dir
    fi
}


function switch_etl {
    switch_sth $1 $2 etlserver.sh shutdown.sh
}

function switch_dmv {
    switch_sth $1 $2 "datamover.sh start" "datamover.sh stop"
}

function switch_processing_pipeline {
    new_state=$1
    switch_etl $new_state etlserver-analys
    switch_etl $new_state etlserver-raw 
    switch_dmv $new_state datamover-analys
    switch_sth $new_state dummy-img-analyser start.sh stop.sh
    switch_dmv $new_state datamover-raw
}


function launch_tests {
    # prepare empty incoming data
    rm -fr $DATA
    cp -R $TEMPLATE/data $WORK
    clean_svn $DATA

    switch_processing_pipeline "on"
    sleep 4

    generate_test_data
    sleep $TIME_TO_COMPLETE

    switch_processing_pipeline "off"
}

function assert_correct_results {
    local res=$WORK/client-result.txt
    call_in_dir check-results.sh $LIMS_CLIENT/ > $res
    assert_pattern_present $res 3 ".*NEMO.*EXP1.*IMAGE_ANALYSIS_DATA.*3VCP[[:digit:]].*microX.*3VCP[[:digit:]]" 
    assert_pattern_present $res 3 ".*NEMO.*EXP1.*IMAGE\/.*3VCP[[:digit:]].*microX.*3VCP[[:digit:]]" 

    assert_dir_empty $DATA/in-raw
    assert_dir_empty $DATA/out-raw
    assert_dir_empty $DATA/in-analys
    assert_dir_empty $DATA/out-analys
    assert_dir_empty $DATA/analys-copy
    imgAnalys="$DATA/store-analys/Project_NEMO/Experiment_EXP1/ObservableType_IMAGE_ANALYSIS_DATA/Barcode_3VCP1/1"
    assert_dir_exists "$imgAnalys"
    rawData="$DATA/store-raw/3V/Project_NEMO/Experiment_EXP1/ObservableType_IMAGE/Barcode_3VCP1/1"
    assert_dir_exists "$rawData"
}

function integration_tests {
    force_rebuild=$1
    force_reinstall=$2

    init_log
    if [ ! -d $INSTALL -o "$force_rebuild" == "true" ]; then
        build_zips
        install
    else
        if [ ! -d $WORK -o "$force_reinstall" == "true" ]; then
	    install
	else
	    restart_lims
	fi
    fi
    launch_tests
    assert_correct_results
    shutdown_lims_server
    exit_if_assertion_failed
}

function clean_after_tests {
	echo "Cleaning $INSTALL..."
    rm -fr $INSTALL
	echo "Cleaning $WORK..."
    rm -fr $WORK
}

# -- MAIN ------------ 
if [ "$1" = "clean" ]; then
    clean_after_tests
else
    force_rebuild=false
    force_reinstall=false
    if [ "$1" = "--force-rebuild" ]; then
			force_rebuild=true
		fi
    integration_tests $force_rebuild $force_reinstall
fi