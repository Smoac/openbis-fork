#!/bin/bash
# Finishes sprint installation of sprint server 
# Warning: all changes to this file should be transfered to SVN repository

CIFEX_DIR=~/sprint/datastore_server/data/incoming-cifex
INCOMING_DIR=~/sprint/datastore_server/data/incoming-batch
DROPBOX_FIA_DIR=~/sprint/datastore_server/data/dropbox-fiaML
DROPBOX_EIC_DIR=~/sprint/datastore_server/data/dropbox-eicML

[ -d $CIFEX_DIR ] || mkdir -p $CIFEX_DIR
[ -d $INCOMING_DIR ] || mkdir -p $INCOMING_DIR
[ -d $DROPBOX_FIA_DIR ] || mkdir -p $DROPBOX_FIA_DIR 
[ -d $DROPBOX_EIC_DIR ] || mkdir -p $DROPBOX_EIC_DIR 
