#!/bin/bash
# Processing steps needed from the Illumina Output to the SRF files


export RUN_BASE=/net/bs-dsu-data/array0/dsu/processing
export LATEST_FOLDER=`ls -1tr $RUN_BASE | tail -1`
export INTENSITY_FOLDER=Data/Intensities
export BUSTARD=/usr/local/dsu/GAPipeline/bin/bustard.py
export CONFIG_FILE=$RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER/config.txt
export CIF2TXT=/usr/local/dsu/bin/cif2txt.sh
export LANES2SRF=/usr/local/dsu/bin/lanes2srf_RTA
export NUMBER_OF_LANES=`cat $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER/RTAConfiguration.xml | grep NumberOfLanes | cut -d \> -f2 | cut -d \< -f1`
export NUMBER_OF_TILES=`cat $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER/RTAConfiguration.xml | grep TilesPerLane | cut -d \> -f2 | cut -d \< -f1`
export NUMBER_OF_CYCLES=`ls -1tr $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER/L00$NUMBER_OF_LANES/ | tail -1 | cut -d . -f1 | cut -d C -f2`
export MAILX=/bin/mailx
export MAIL_LIST="manuel.kohler@bsse.ethz.ch"
export RM=/bin/rm
export MAKE=/usr/bin/make

export PRG=`basename $0`
export USAGE="Usage: ${PRG} <Path_to_Run_Base> <Number_of_Lanes> \n\nEXAMPLE: ${PRG} /array0/Runs/ 8"

if [ -z "${RUN_BASE}"  -o -z "${NUMBER_OF_LANES}" ]
then
   echo "${USAGE}"
   exit 1
fi

cd $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER

echo "5:ELAND_GENOME /array0/Genomes/PhiX" > $CONFIG_FILE
echo "5:ANALYSIS eland_extended" >> $CONFIG_FILE
echo "ELAND_MULTIPLE_INSTANCES 8" >> $CONFIG_FILE
echo "EMAIL_LIST manuel.kohler@bsse.ethz.ch" >> $CONFIG_FILE

/usr/local/dsu/GAPipeline/bin/GERALD.pl config.txt --FLOW_CELL v4  --EXPT_DIR BaseCalls/ --make
cd BaseCalls/GERALD*
make -j 8 all

#$BUSTARD --CIF . --GERALD=$CONFIG_FILE --control-lane=5 --make

#cd Bustard*
#echo "Starting parallel make..."

#make recursive -j 15 2>seq_make.err >seq_make.log
cd $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls

$CIF2TXT $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER $NUMBER_OF_CYCLES $NUMBER_OF_TILES

$LANES2SRF $RUN_BASE/$LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/ $NUMBER_OF_LANES
#$LANES2SRF /net/bs-dsu-data/array0/dsu/processing/091208_433DAAXX/Data/Intensities/BaseCalls/ 8

# clean up
#chmod -R 755 $RUN_BASE/$LATEST_FOLDER

echo -e "SRF creation finished for $LATEST_FOLDER :-)" | $MAILX -s "SRF creation finished for $LATEST_FOLDER :-)" $MAIL_LIST