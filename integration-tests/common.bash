# author: Tomasz Pylak, 2007-09-27
# Integration tests functions

# ----------------------------- global constants

TRUE=1
FALSE=0

# all paths are relative to the template directory
TEMPLATE=templates
TARGETS=targets
WORK=$TARGETS/playground
ERR_LOG=$WORK/all_err_log.txt

INSTALL=$TARGETS/install
LOCAL_PROJECTS=..

OPENBIS_SERVER=$WORK/openBIS-server

CI_HOST=cisd-vesuvio.ethz.ch
CI_HOME=/localhome/ci
SSH_CRUISE_CONTROL_NAME=ci@$CI_HOST
HUDSON_ARTIFACTS=hudson/jobs
CI_HOST_IP=`host $CI_HOST|grep address|awk '{print $4}'`
MY_HOST=`hostname`
MY_HOST_IP=`host $MY_HOST|grep address|awk '{print $4}'`

# ----------------------------- global state

TEST_FAILED=false # working variable, if true then some tests failed

# ----------------------------- assertions to check the tests results

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

function assert_file_not_exists {
    local file=$1
    if [ -f "$file" ]; then
        report_error File $file does exist although it should not!  
    else
        echo [OK] File $file does not exists
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

function assert_files_number {
        local dir=$1
        local expected_files_count=$2
        
        local files_count=`ls -1 $dir | wc -l`
        assert_equals "Wrong number of files in $dir directory" $expected_files_count $files_count
}


# -----------------------------
# Scripts to build and install components needed in integration tests.
#
# Implementation assumptions:
# - the current directory after calling a function does not change
# -----------------------------

# ----------------------------- configuration

BIN_PATHS="/opt/local/bin /usr/bin /usr/sbin"
USER=`whoami`
DATABASE=openbis_integration_test

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
    assert_file_exists_or_die "$INSTALL/datastore_server-*.zip"
    assert_file_exists_or_die "$INSTALL/datastore_server_plugin-yeastx-*.zip"
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
    local dir=$LOCAL_PROJECTS/$PROJECT_NAME/targets/dist/
    mv $dir/*.zip $INSTALL
}

function build_components {
    build_cmd=$1
    build_dss=$2
    build_dmv=$3
    build_openbis=$4

    if [ $build_dss == "true" ]; then
        rm -f $INSTALL/datastore_server*.zip
        rm -f $INSTALL/dss_client*.zip
        $build_cmd datastore_server
        $build_cmd rtd_yeastx
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
    ./build.sh --private $PROJECT_NAME
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

function fetch_latest_artifacts_from_cruise_control {
    local proj_name=$1
    local dest_dir=$2
    
    local last_build="$HUDSON_ARTIFACTS/$proj_name/lastSuccessful/archive/_main/targets/dist"
    if [ $MY_HOST_IP == $CI_HOST_IP ]; then
        local last=`ls -1 $CI_HOME/ci/$last_build | sort | tail -1`
        echo "Fetching artifacts for $proj_name: $last" 
        cp $CI_HOME/$last_build/*.zip $dest_dir
    else
        local list_cmd="ls -1 $last_build | sort | tail -1"
        local last=`echo $list_cmd | ssh $SSH_CRUISE_CONTROL_NAME -T`
        echo "Fetching artifacts for $proj_name: $last" 
        scp $SSH_CRUISE_CONTROL_NAME:$last_build/*.zip $dest_dir
    fi
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
    unzip -d $WORK $INSTALL/$file_pattern*.zip
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

function restore_database {
	local db_name=$1
	local db_file_path=$2 
		
    psql_cmd=`run_psql`
    $psql_cmd -U postgres -c "drop database if exists $db_name"
    $psql_cmd -U postgres -c "create database $db_name with owner $USER template = template0 encoding = 'UNICODE'"
    $psql_cmd -U $USER -d $db_name -f $db_file_path
}

#
# Installs openbis server in the specified directory. 
# Copies templates from the directory with the same name in $TEMPLATE diretcory.
#
function install_openbis_server {
    local install_openbis=$1
		local openbis_server_dir=$2
		
		local openbis_server_name=`basename $openbis_server_dir`
		
		restore_database $DATABASE $TEMPLATE/$openbis_server_name/test_database.sql
    if [ $install_openbis == "true" ]; then
        rm -fr $openbis_server_dir
				copy_templates $openbis_server_name
    
        unzip -d $openbis_server_dir $INSTALL/openBIS*.zip
				$openbis_server_dir/openBIS-server/install.sh $PWD/$openbis_server_dir $openbis_server_dir/service.properties $openbis_server_dir/openbis.conf
        startup_openbis_server $openbis_server_dir
				wait_for_server
    else
        copy_templates $openbis_server_name
        restart_openbis $openbis_server_dir
    fi
}


function startup_openbis_server {
	local openbis_server_dir=$1 
		
    call_in_dir bin/startup.sh $openbis_server_dir/jetty
    wait_for_server
}

function shutdown_openbis_server {
		local openbis_server_dir=$1 
		
    if [ "`check_server_port`" != "" ]; then
        $openbis_server_dir/jetty/bin/shutdown.sh
    fi
}

# unpack everything, override default configuration with test configuation        
function install_dsss {
    local install_dss=$1
    local dss_dirs="datastore_server1 datastore_server2 datastore_server_yeastx"
    if [ $install_dss == "true" ]; then
        unpack datastore_server-
        for dss_dir in $dss_dirs; do
                prepare datastore_server $dss_dir
        done
        remove_unpacked datastore_server
    else
        for dss_dir in $dss_dirs; do
                copy_templates $dss_dir
        done
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
	local openbis_server_dir=$1
		
    assert_dir_exists_or_die $openbis_server_dir
    if [ "`check_server_port`" != "" ]; then
            # maybe server is just closing, wait a moment
            sleep 5
    fi
    if [ "`check_server_port`" != "" ]; then
    	echo Shutting down openbis server.
	    shutdown_openbis_server $openbis_server_dir
	    sleep 1
  	fi
    startup_openbis_server $openbis_server_dir
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
            install_openbis_server "true" $OPENBIS_SERVER
    else
            install_dsss $install_dss
            install_datamovers $install_dmv
            install_openbis_server $install_openbis $OPENBIS_SERVER
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
    switch_sth $1 $2 "datastore_server.sh start" "datastore_server.sh stop" $FALSE
}

function switch_dmv {
    switch_sth $1 $2 "datamover.sh start" "datamover.sh stop" $TRUE
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

function assert_dss_registration {
    local dss=$1
    echo ==== assert registration of DSS $dss ====
    assert_pattern_present $WORK/$dss/log/datastore_server_log.txt 1 getVersion
}

function build_and_install {
    install_dss=$1
    install_dmv=$2
    install_openbis=$3
    use_local_source=$4
    reinstall_all=$5
    
    init_log
    # NOTE: Comment this line if you want to use different libraries.
    build_zips $install_dss $install_dmv $install_openbis $use_local_source
    
    install $install_dss $install_dmv $install_openbis $reinstall_all
}

function clean_after_tests {
    echo "Cleaning $INSTALL..."
    rm -fr $INSTALL
    echo "Cleaning $WORK..."
    rm -fr $WORK
}
