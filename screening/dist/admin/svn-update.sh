#!/bin/bash
# Updates all the admin scripts to the version found in SVN.

# screening-specific
SVN=svnsis.ethz.ch/repos/cisd/screening/trunk/dist/admin
SVN2=svnsis.ethz.ch/repos/cisd/installation/trunk/resource/installer/bin/backup-databases.sh

# set directory
BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

if [ -z $OPENBIS_HOME ]; then
    OPENBIS_HOME=${BASE}
fi

# checking for wget
WGET_HOME=`which wget`

# download the scripts
$WGET_HOME -nH --cut-dirs=6 --directory-prefix $OPENBIS_HOME --no-verbose -r -l2 -A.{sh,sql} http://$SVN/
$WGET_HOME -nH  --directory-prefix $OPENBIS_HOME --no-verbose http://$SVN2

# set permissions
find $OPENBIS_HOME -type f -name "*.sh" -exec chmod 700 {} \;

