#!/bin/bash
# Updates all the admin scripts to the version found in SVN.

# screening-specific
SVN=svncisd.ethz.ch/repos/cisd/screening/trunk/dist/admin

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
$WGET_HOME --no-directories --directory-prefix $OPENBIS_HOME --no-verbose -r -l1 -A.sh http://$SVN/

# set permissions
find $OPENBIS_HOME -type f -name "*.sh" -exec chmod 700 {} \;

