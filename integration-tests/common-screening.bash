# This code is ment to be common for all screening integration tests,
# but for now only biozentrum integration tests are using it.

function install_and_run_openbis_server_screening {
    local install_openbis=$1
    local local_template_dir=$2
    
		local openbis_server_dir=$OPENBIS_SERVER_HCS
		local openbis_server_name=`basename $openbis_server_dir`
		
		restore_database $OPENBIS_DATABASE_HCS $local_template_dir/$openbis_server_name/test_database.sql
    if [ $install_openbis == "true" ]; then
        rm -fr $openbis_server_dir
    
        unzip -q -d $openbis_server_dir $INSTALL/openBIS*.zip
        mv $openbis_server_dir/openBIS-server/* $openbis_server_dir
				rmdir $openbis_server_dir/openBIS-server

				cp -v $local_template_dir/$openbis_server_name/service.properties $openbis_server_dir/service.properties
				$openbis_server_dir/install.sh $PWD/$openbis_server_dir
				startup_openbis_server $openbis_server_dir
    else
        restart_openbis $openbis_server_dir
				wait_for_server
    fi
}

function install_dss_screening {
		local local_template_dir=$1

		local dss_dest=$DSS_SERVER_HCS
		local dss_template=$local_template_dir/$DSS_DIR_NAME

		rm -fr $dss_dest
		unzip -q $INSTALL/datastore_server-screening*.zip -d $dss_dest
		mv $dss_dest/datastore_server/* $dss_dest
		rmdir $dss_dest/datastore_server

		# override default DSS configuration
		cp -v $dss_template/etc/* $dss_dest/etc
}

# installs AS and DSS and starts AS
function install_screening {
		local local_template=$1
		
		# FIXME uncomment !!!!!!!!!!!!!!!!!!!!
		#rm -fr $INSTALL
		#fetch_distributions datastore_server
		#fetch_distributions screening

		echo Dropping imaging database: $IMAGING_DB
		psql_cmd=`run_psql`
		$psql_cmd -U postgres -c "drop database if exists $IMAGING_DB" 
		
		rm -fr $WORK
		mkdir -p $WORK
		install_dss_screening $local_template
		install_and_run_openbis_server_screening "true" $local_template
}

function assertFeatureVectorDef {
    local psql=`run_psql`
    local result=`$psql -t -U postgres -d $IMAGING_DB \
       -c "select label from feature_defs where code = '$1'"  \
       | awk '{gsub(/\|/,";");print}'`
    echo "Checking feature $1"
    assert_equals "Feature code and label" " $2" "$result"
}

# returns 0 on success, 1 otherwise
function wait_for_file {
	local file_name=$1

	local timeout=40
	local total_time=0
	local check_interval=5	
	echo -n Waiting for creation of $file_name
	while [ ! -e $file_name ]; do 
	  echo -n .
	  sleep $check_interval
    total_time=$(( $total_time + $check_interval ))
	  if [ $total_time -gt $timeout ]; then
	  	report_error "File $file_name has not been detected after $timeout sec [Error]"
	  	return 1
	  fi
	done
	echo "  [OK]"
	return 0
}
