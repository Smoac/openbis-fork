#!/bin/bash

export RUN_BASE=/net/bs-dsu-data/array0/dsu/runs/ellac
export LATEST_FOLDER=`ls -1dtr $RUN_BASE/*XX* | tail -1`
export FC=`echo $LATEST_FOLDER | cut -f8 -d/`
export NUMBER_OF_TILES=`cat $LATEST_FOLDER/Data/Intensities/RTAConfiguration.xml | grep TilesPerLane | cut -d \> -f2 | cut -d \< -f1`
export MAILX=/bin/mailx
export MAIL_LIST="manuel.kohler@bsse.ethz.ch"
export SUMMARY_FILE=$LATEST_FOLDER/Data/Intensities/BaseCalls/GERALD*/Summary.xml
export SRF_INFO_OUTPUT=$LATEST_FOLDER/Data/Intensities/BaseCalls/Srf/srf_info.txt


cat $SRF_INFO_OUTPUT | grep GOOD | cut -f 4 -d " " > numbers
j=0
for i in `cat numbers`; do n[j]=$i; j=$j+1 ;done

grep -A 279 \<LaneResultsSummary\> $SUMMARY_FILE | grep -A 1 \<clusterCountPF\> | grep mean | cut -f 2 -d \< | cut -f2 -d \> > summary_temp 
j=0
for i in `cat summary_temp`; do echo $i*$NUMBER_OF_TILES| bc -l >> summary_numbers ; done
for i in `cat summary_numbers`; do sn[j]=$i j=$j+1; done

for k in {0..7}; do echo ${sn[$k]}-${n[$k]} | bc >> difference; done

$MAILX -s "Flow Cell $FC: Number comparison of srf_info and Eland Summary" < difference $MAIL_LIST

rm difference summary_numbers summary_temp numbers 
