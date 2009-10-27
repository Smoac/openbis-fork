#!/bin/bash
# Processing steps needed from the Illumina Output to the SRF files


export RUN_BASE=/array0/Runs
export LATEST_FOLDER=`ls -1tr $RUN_BASE | tail -1`
export INTENSITY_FOLDER=Data/Intensities
export BUSTARD=/usr/local/dsu/GAPipeline/bin/bustard.py
export CONFIG_FILE=$RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER/config.txt
export CIF2TXT=/usr/local/dsu/bin/cif2txt.sh
export LANES2SRF=/usr/local/dsu/bin/lanes2srf_RTA
export NUMBER_OF_LANES=8
export NUMBER_OF_CYCLES=`ls -1tr $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER/L00$NUMBER_OF_LANES/ | tail -1 | cut -d . -f1 | cut -d C -f2`

cd $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER

echo "5:ELAND_GENOME /array0/Genomes/PhiX" > $CONFIG_FILE
echo "5:ANALYSIS eland_extended" >> $CONFIG_FILE
echo "ELAND_MULTIPLE_INSTANCES 8" >> $CONFIG_FILE
echo "EMAIL_LIST manuel.kohler@bsse.ethz.ch" >> $CONFIG_FILE

$BUSTARD --CIF . --GERALD=$CONFIG_FILE --control-lane=5 --make

cd Bustard*
make recursive -j 15 2>seq_make.err >seq_make.log

$CIF2TXT $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER $NUMBER_OF_CYCLES

$LANES2SRF .
