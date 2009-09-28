#!/bin/bash
# Checks on missing pictures in the 'Images' folder of the new Illumina Pipeline 1.4
# July 2009
# Author: Manuel Kohler
# Needs one parameter to specify the number of cycles which may vary

export NUMBER_OF_CYCLES=$1
export NUMBER_OF_LANES=8
export NUMBER_OF_TILES_PER_LANE=100
export NUMBER_OF_IMAGES_PER_TILE=4
export IMAGES_PER_CYCLE=$[ ${NUMBER_OF_TILES_PER_LANE}*${NUMBER_OF_IMAGES_PER_TILE} ]
#----------------------------------------------
export RUN_BASE=/array0/Runs
export LATEST_FOLDER=`ls -1tr $RUN_BASE | tail -1`  
export IMAGE_PATH=$RUN_BASE/$LATEST_FOLDER/Images
#----------------------------------------------
export MAILX="/bin/mailx"
export MAIL_LIST="manuel.kohler@bsse.ethz.ch"
#----------------------------------------------
export BOX=`uname -n`
export PRG=`basename $0`
export USAGE="Usage: ${PRG} <Number_of_cycles>"
export DAY=`date |cut -c1-3`
export BUILDSTAMP=`date '+%Y.%m.%d_%H_%M'`
export MISSING_IMAGES=$IMAGE_PATH/missing_images_${BUILDSTAMP}.txt
#----------------------------------------------
if [ -z "${NUMBER_OF_CYCLES}" ]
then
   echo "${USAGE}"
   exit 1
fi
#----------------------------------------------
echo -e "Checking $IMAGE_PATH as Image Folders"
echo -e "Looking for $IMAGES_PER_CYCLE Images in $NUMBER_OF_CYCLES cycle folders..."

# Create a reference file
for (( k = 1; k <= $NUMBER_OF_TILES_PER_LANE; k++ )); do
 for j in a c g t; do
  echo $k\_$j.tif >> $IMAGE_PATH/image_reference.txt;
 done
done
#----------------------------------------------
for i in $IMAGE_PATH/L00*; do 
 for c in $i/*; do
  NUMBER=`ls -1 $c | wc -l`;
  if [ $NUMBER -ne ${IMAGES_PER_CYCLE} ]; 
   then
    echo $c 
    ls -1 $c > $IMAGE_PATH/incomplete_files.txt
    cat $IMAGE_PATH/incomplete_files.txt | cut -d "_" -f3- | sort -n > $IMAGE_PATH/incomplete_files-s.txt
    diff $IMAGE_PATH/incomplete_files-s.txt  $IMAGE_PATH/image_reference.txt; 
 fi
 done;
done > $IMAGE_PATH/missing.txt 
#----------------------------------------------
sed '/^[0-9]/d' $IMAGE_PATH/missing.txt > $IMAGE_PATH/missing_tmp.txt
sed 's/^> //' $IMAGE_PATH/missing_tmp.txt > $MISSING_IMAGES
#----------------------------------------------
rm  $IMAGE_PATH/image_reference.txt $IMAGE_PATH/incomplete_files.txt $IMAGE_PATH/incomplete_files-s.txt $IMAGE_PATH/missing.txt $IMAGE_PATH/missing_tmp.txt
#----------------------------------------------
$MAILX -s "Missing Images in last GA run on $BOX discovered! ( ${MISSING_IMAGES} )" $MAIL_LIST < ${MISSING_IMAGES}
exit 0;
