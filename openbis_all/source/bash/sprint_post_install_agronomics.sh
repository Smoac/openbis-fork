#!/bin/bash
# Finishes sprint installation of agronomics server 
# Warning: all changes to this file should be transfered to SVN repository

PFS_DIR=/localhome/openbis/sprint/datastore_server/data/incoming/PHENOTYPE_FEATURE_SET
PCE_DIR=/localhome/openbis/sprint/datastore_server/data/incoming/transcriptomics/pce
TSU1_DIR=/localhome/openbis/sprint/datastore_server/data/incoming/transcriptomics/tsu1
TSU2P1_DIR=/localhome/openbis/sprint/datastore_server/data/incoming/transcriptomics/tsu2p1
TSU2P2_DIR=/localhome/openbis/sprint/datastore_server/data/incoming/transcriptomics/tsu2p2


[ -d $PFS_DIR ] || mkdir -p $PFS_DIR
[ -d $PCE_DIR ] || mkdir -p $PCE_DIR
[ -d $TSU1_DIR ] || mkdir -p $TSU1_DIR
[ -d $TSU2P1_DIR ] || mkdir -p $TSU2P1_DIR
[ -d $TSU2P2_DIR ] || mkdir -p $TSU2P2_DIR
