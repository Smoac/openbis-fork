#!/bin/sh
# author: Kaloyan Enimanev
#
# The integration test scenario for archiving.
#     assumptions: 
#        1) postgres is running on the local machine
#        2) The command "ssh locahost" completes sussessfully without requiring any user input (e.g. password input)
#     
# -------------------
# - openBIS + DataStore servers are launched
# - STEP 1
#      * one data set is registered via a python dropbox 
#      * a backup copy of the data set is expected to appear in the archive
# - STEP 2
#      * the data in the archive is damaged on purpose
#      * archiving + delete from store is triggered
#      * we expect the archiving process to "repair" the broken archive copy before deleting 
#        the data set from the data store
#

# --- include external sources ------------------------ 

source common.bash



# ----- local constants --------------------

TIME_TO_COMPLETE=60 # time (in seconds) needed by the whole pipeline to process everything
TEST_DATA_DIR=templates/data-archiving
ARCHIVE_DIR=/tmp/integration-tests/archiving/rsync-archive
ARCHIVE_DATASET=ARCHIVE_DATASET
DATA_STORE_DIR=$WORK/data/main-store
DSS_SERVICE_PROPS=$WORK/datastore_server_archiving/etc/service.properties



# ---- Testcase-specific preparation steps ------------

function prepare_step1 {
    
    echo Recreating an emtpy archive directory "$ARCHIVE_DIR"
    rm -fr $ARCHIVE_DIR
    mkdir -p $ARCHIVE_DIR
    
    echo Copying test dataset to jython dropbox...
    local DIR=$WORK/data/incoming-jython
    copy_test_data $ARCHIVE_DATASET $DIR
}


function prepare_step2 {
   damage_archive  
   reconfigure_datastore_server
   unset_presentInArchiveFlag_DB
}


function damage_archive {
   echo "Inserting invalid content in the archived dataset copy..."
   echo "INVALID CONTENT AT THE END OF ARCHIVE" >> $ARCHIVED_DATASET_DIR/archive-me.txt
}


function reconfigure_datastore_server {
    # after the reconfiguration 
    # the auto-archiving will start deleting datasets from store
    echo "Reconfiguring auto-archiving to remove datasets from store ...."
    local tmpFile=$DSS_SERVICE_PROPS.tmp
    sed -e "s/auto-archiver.remove-datasets-from-store=false/auto-archiver.remove-datasets-from-store=true/g" < $DSS_SERVICE_PROPS  > $tmpFile
    mv $tmpFile $DSS_SERVICE_PROPS
}


function unset_presentInArchiveFlag_DB {
    local psql=`run_psql`
    $psql -U postgres -d $DATABASE -c "update external_data set present_in_archive=false"
}




#
# ---------  assertions --------------------
#

function assert_last_dataset_content_in_database {
    local pattern=$1
    echo ==== assert correct last dataset content in database with pattern $pattern ====
    local psql=`run_psql`
    local dataset=`$psql -U postgres -d $DATABASE \
       -c "select d.code, ed.status, ed.present_in_archive \
               from data as d  \
               left join external_data as ed on ed.data_id = d.id \
           where d.id = (select max(id) from data)" \
       | awk '/ +[0-9]+/' \
       | awk '{gsub(/ /,"");print}' \
       | awk '{gsub(/\|/,";");print}'`
    local lines=`echo "$dataset" | grep "$pattern" | wc -l`
    if [ $lines == 0 ]; then
        report_error Last dataset does not match pattern "$pattern": $dataset
    fi 
}


function asserts_step1 {

    assert_last_dataset_content_in_database ".*;AVAILABLE;t"

    ARCHIVED_DATASET_DIR=`find $ARCHIVE_DIR -type d -name "$ARCHIVE_DATASET"`
    if [ "$ARCHIVED_DATASET_DIR" == "" ]; then
      report_error "Cannot find archived dataset copy under $ARCHIVE_DIR"
    else 
      asserts_valid_archive_copy
    fi    
}


function asserts_step2 {
    
    assert_last_dataset_content_in_database ".*;ARCHIVED;t"
    asserts_valid_archive_copy
    
    local dataset_in_store=`find $DATA_STORE_DIR -type d -name "$ARCHIVE_DATASET"`
    if [  -d "$dataset_in_store" ]; then
        report_error Data set \"$dataset_in_store\" should be deleted from the datastore !  
    fi
     
}


function asserts_valid_archive_copy {
    assert_same_content $TEST_DATA_DIR/$ARCHIVE_DATASET $ARCHIVED_DATASET_DIR
}



# --- helper functions ---------

function logAndSleep {
    local sleepSec=$1
    echo "Sleeping for $sleepSec seconds ..."
    sleep $sleepSec
}


function copy_test_data {
    local NAME=$1
    local DIR=$2
    cp -RPpv $TEST_DATA_DIR/$NAME $DIR
    clean_svn $DIR/$NAME
}

# Prepare template incoming data and some destination data structures
function prepare_directory_structures {
    echo Re-creating in/out directory structures  
    for data_folder in data data-archiving; do
        work_destination=$WORK/$data_folder
        rm -fr $work_destination
        mkdir -p $work_destination
        cp -Rv $TEMPLATE/$data_folder $WORK
        clean_svn $work_destination
    done
}




#
# ------- test workflow --------------
#

function test_step1 {
    #
    # step 1 : expect new data set to be archived
    #
    prepare_step1
    switch_dss "on" datastore_server_archiving

    logAndSleep $TIME_TO_COMPLETE

    switch_dss "off" datastore_server_archiving
    logAndSleep 5
    
    asserts_step1
}

function test_step2 {
    #
    # step 2 : damage archive copy and expect the Rsync archiverto detect the error
    #
    prepare_step2
    switch_dss "on" datastore_server_archiving

    logAndSleep $TIME_TO_COMPLETE

    switch_dss "off" datastore_server_archiving
    logAndSleep 5
    
    asserts_step2
}

function integration_test {
    prepare_directory_structures
    
    build_and_install $@
    
    test_step1
    # TODO KE: remove this comment when we implement filesize check in the RSyncArchiver
    #test_step2
    
    shutdown_openbis_server $OPENBIS_SERVER
    exit_if_assertion_failed
}




#
# ------ CLI utility/HELP functions ----------------
#
function print_help {
    echo "Usage: $0 [ (--dss | --openbis)* | --all [ --local-source ]]"
    echo "	--dss, --openbis, build chosen components only"
    echo "	--all 			build all components"
    echo "	--local-source		use local source code during building process instead of downloading it from svn"
    echo "	--reinstall-all		reinstalls all packeges new from the zip file which is in the installation direcory (also reinstall the packages which are not build)"  
    echo "	--clean			clean and exit"
    echo "	--help			displays this help"
    echo "If no option is given, integration tests will be restarted without building anything."
    echo "Examples:"
    echo "- Rebuild everything, fetch sources from svn:"
    echo "	$0 --all"
    echo "- Use openbis server and client installation from previous tests, rebuild data store server using local source:"
    echo "	$0 --dss --local-source"
    echo "- Rebuild data store server only fetching sources from svn:"
    echo "	$0 --dss"
}



#
# -- MAIN (copied from)------------
#

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
	    *)
		echo "Illegal option $1."
		print_help
		exit 1
		;;
         esac
	 shift
    done
    integration_test $install_dss $install_dmv $install_openbis $use_local_source $reinstall_all
fi
