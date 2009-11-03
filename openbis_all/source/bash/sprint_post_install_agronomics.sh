#!/bin/bash
# Finishes sprint installation of agronomics server 
# Warning: all changes to this file should be transfered to SVN repository

MPAF_DIR=/local0/data/dss_incoming/transcriptomics/mpaf
[ -d $MPAF_DIR ] || mkdir -p $MPAF_DIR

OPENBIS_SERVER_HOME=/localhome/openbis/sprint/openBIS-server/apache-tomcat-5.5.26/webapps/openbis
cp ~/config/Guide_to_Using_Agron-omics_openBIS.pdf $OPENBIS_SERVER_HOME