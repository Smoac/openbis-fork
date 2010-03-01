#!/bin/bash
# Script to submit all images from one plate (tif, jpg, segmented jpg) in LMC "SSS" format.
# Creates symbolic links in the right incoming folder.
# Author: Tomasz Pylak

# --- configuration --------

# openbis incoming directory
IN_ROOT=/mnt/cluster/openbis/incoming
# absolute path to a directory where images for plates can be found
IMAGE_DIR=/mnt/cluster/tmp/openbis/openbis-test-data

# -----------

IN_TIFF=$IN_ROOT/incoming-raw
IN_JPG=$IN_ROOT/incoming-jpg
IN_SEGJPG=$IN_ROOT/incoming-segmented-jpg

function submit_plate {	
  # this should be a full path
  local PLATE=$1
  local PLATE_NAME=`basename $PLATE`

  ln -s $PLATE $IN_TIFF
  touch $IN_TIFF/.MARKER_is_finished_$PLATE_NAME
  ln -s $PLATE/anal3 $IN_JPG/$PLATE_NAME
  touch $IN_JPG/.MARKER_is_finished_$PLATE_NAME
  ln -s $PLATE/anal1 $IN_SEGJPG/$PLATE_NAME
  touch $IN_SEGJPG/.MARKER_is_finished_$PLATE_NAME
}

for p in $IMAGE_DIR/*; do
  submit_plate $p
done