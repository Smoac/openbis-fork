# Manuel Kohler 2009, CISD, ETH Zürich
# wrapper for the cifToTxt binary provided by Illumina

#Usage: cifToTxt [options]

#Command line options:
#  -h [ --help ]                 produce help message and exit
#  -I [ --IPAR ]                 generate IPAR data
#  -N [ --noise ]                convert noise instead of intensities
#  -l [ --lane ] arg             identifier of the lane (1, 2, 3, ..., 8)
#  -t [ --tile ] arg             identifier of the tile (1, 2, 3, ..., 110)
#  -r [ --repeat ] arg (=1)      identifier of the repeat (1, 2, ...)
#  -f [ --first-cycle ] arg (=1) first cycle to use (1-based)
#  -n [ --number-of-cycles ] arg number of cycles to convert
#  -i [ --input-dir ] arg (=.)   directory where the CIF directories are located
#  -o [ --output-dir ] arg (=.)  directory where the output file should be 
#                                written
#  -c [ --compression ] arg      where arg=bzip2|gzip|none. The data compression
#                                format used for the output file.
#!/bin/bash

INTENSITY_FOLDER=$1
CYCLES=$2
NUMBER_OF_LANES=8
NUMBER_OF_TILES=100
#INT_NSE_DIR=$1/int_nse
PRG=`basename $0`
USAGE="Usage: ${PRG} <Path_to_Intensity_Folder> <Number_of_Cycles> \n\nEXAMPLE: ${PRG} /array0/Runs/090720_42HUDAAXX/Data/Intensities/ 38" 

if [ -z "${INTENSITY_FOLDER}" -o -z "${CYCLES}" ]
then
   echo -e "${USAGE}"
   exit 1
fi


# INT_NSE_DIR  there?
[ -d $INT_NSE_DIR ] || mkdir -p $INT_NSE_DIR

for (( l = 1; l <= $NUMBER_OF_LANES; l++ )); do
 for (( t = 1; t <= $NUMBER_OF_TILES; t++ )); do
  echo Lane $l Tile $t;
  # Convert Signal cif to int
  /dsf/GAPipeline/bin/cifToTxt -l $l -t $t -n $CYCLES -i $INTENSITY_FOLDER -o $INTENSITY_FOLDER -c none 
  # Convert Noise cnf to nse (additional -N)
  /dsf/GAPipeline/bin/cifToTxt -N -l $l -t $t -n $CYCLES -i $INTENSITY_FOLDER -o $INTENSITY_FOLDER -c none 
 done
done
