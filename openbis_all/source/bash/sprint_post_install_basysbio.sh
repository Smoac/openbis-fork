#!/bin/bash

NGQQNR_DIR=/local0/home/openbis/sprint/datastore_server/data/incoming/NGQQNR
GLI_DIR=/local0/home/openbis/sprint/datastore_server/data/incoming/GLI
CIFEX_DIR=/local0/home/openbis/sprint/datastore_server/data/incoming-cifex
MFS_DIR=/local0/home/openbis/sprint/datastore_server/data/incoming/MFS

[ -d $NGQQNR_DIR ] || mkdir -p $NGQQNR_DIR
[ -d $GLI_DIR    ] || mkdir -p $GLI_DIR
[ -d $CIFEX_DIR  ] || mkdir -p $CIFEX_DIR
[ -d $MFS_DIR    ] || mkdir -p $MFS_DIR
