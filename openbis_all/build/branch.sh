#!/bin/bash

if [ `dirname $0` != "." ]
then
	echo "Please run from the same directory than the script source file is in"
	exit 1
fi

if [ $# -ne 1 ]
then
  echo "Usage: ./branch.sh [branch]"
  echo ""
  echo "Example: ./branch.sh release/13.04.x"
  exit 1
fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 2>/dev/null
if [ $? -eq 0 ]; then echo "Branch already exists!"; exit 1; fi

for project in gradle authentication common datastore_server dbmigration deep_sequencing_unit installation integration-tests js-test openbis openbis_all openbis_api openbis_knime openbis_mobile openbis_standard_technologies openbis-common plasmid rtd_cina rtd_phosphonetx rtd_yeastx screening ui-test; do
	svn mkdir --parents svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 -m "parent folder";
	svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/$project/trunk svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1/$project -m "create branch $1";
done
