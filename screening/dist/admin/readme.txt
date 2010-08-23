How to install 'openbis for Screening' from scratch:
- enter the directory where you want to install openbis (e.g. ~/openbis)
- create 'servers' directory and copy 2 zip distributions for openBIS AS and openBIS DSS into it 
- create the bin directory in a following way:
  mkdir bin
  cd bin
  wget svncisd.ethz.ch/repos/cisd/screening/trunk/dist/admin/svn-update.sh
  wget svncisd.ethz.ch/repos/cisd/screening/trunk/dist/admin/env
  wget svncisd.ethz.ch/repos/cisd/screening/trunk/dist/admin/empty-screening-database.sql
  . ./svn-update.sh
- adapt the 'bin/env' file 
- run install script
	bin/install.sh