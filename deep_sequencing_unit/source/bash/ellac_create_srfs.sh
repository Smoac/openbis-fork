#!/bin/bash
# Processing steps needed from the Illumina Output to the SRF files

export SEQUENCER=ellac
export RUN_BASE=/net/bs-dsu-data/array0/dsu/runs/$SEQUENCER
export LATEST_FOLDER=`ls -1dtr $RUN_BASE/*XX* | tail -1`
export INTENSITY_FOLDER=Data/Intensities
export BUSTARD=/usr/local/dsu/GAPipeline/bin/bustard.py
export CONFIG_FILE=$LATEST_FOLDER/$INTENSITY_FOLDER/config.txt
export CIF2TXT=/usr/local/dsu/bin/cif2txt.sh
export LANES2SRF=/usr/local/dsu/bin/lanes2srf_RTA
export MAILX=/bin/mailx
export MAIL_LIST="manuel.kohler@bsse.ethz.ch"
export RM=/bin/rm
export MAKE=/usr/bin/make
export GERALD=/usr/local/CASAVA-1.7.0/bin/GERALD.pl
export LF=`echo $LATEST_FOLDER | cut -d / -f8`
export RUN_COMPLETED=$LATEST_FOLDER/Run.completed
export ANALYSIS_STARTED=$LATEST_FOLDER/Analysis.started
export OPENBIS_SERVER=http://openbis-dsu.bsse.ethz.ch:8080/openbis/openbis
export PROCESSING=/net/bs-dsu-data/array0/dsu/processing
export BCL=/usr/local/dsu/OLB/OLB-1.8.0/bin/setupBclToQseq.py

export PRG=`basename $0`
export USAGE="Usage: ${PRG} <Path_to_Run_Base> <Number_of_Lanes> \n\nEXAMPLE: ${PRG} /array0/Runs/ 8" 

#if [ -z "${RUN_BASE}"  -o -z "${NUMBER_OF_LANES}" ]
#then
#   echo "${USAGE}"
#   exit 1
#fi

function gerald_basic {
        cd $LATEST_FOLDER/$INTENSITY_FOLDER
        export PAIRED_END=`cat  $LATEST_FOLDER/$INTENSITY_FOLDER/RTAConfiguration.xml | grep IsPairedEndRun  | cut -d \> -f2 | cut -d \< -f1`
        echo "5:ELAND_GENOME /array0/Genomes/PhiX" > $CONFIG_FILE
        if $PAIRED_END;
         then echo "5:ANALYSIS eland_pair" >> $CONFIG_FILE;
         else echo "5:ANALYSIS eland_extended" >> $CONFIG_FILE;
        fi
        echo "ELAND_SET_SIZE 8" >> $CONFIG_FILE
        echo "EMAIL_LIST manuel.kohler@bsse.ethz.ch" >> $CONFIG_FILE
        $GERALD $CONFIG_FILE --EXPT_DIR $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls --OUT_DIR $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/GERALD --make
        cd  $LATEST_FOLDER/$INTENSITY_FOLDER/GERALD
        make -j 10 all
        /usr/local/dsu/bin/check_read_numbers.sh $SEQUENCER
}

function gerald {
        cd $LATEST_FOLDER/$INTENSITY_FOLDER
        /usr/local/dsu/openbis-property-client/client/lib/properties.sh $OPENBIS_SERVER $LF
        sleep 5
        mv /usr/local/dsu/openbis-property-client/client/lib/config.txt $CONFIG_FILE
        mv /usr/local/dsu/openbis-property-client/client/lib/bowtie.txt $LATEST_FOLDER/$INTENSITY_FOLDER
#       chmod -R 755 BaseCalls
        mkdir -p $PROCESSING/$LF/BaseCalls
        $GERALD $CONFIG_FILE --EXPT_DIR $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls --OUT_DIR $PROCESSING/$LF/GERALD --make
        cd $PROCESSING/$LF/GERALD
        make -j 10 all
        wait $!
        chmod -R 755 .
        rsync -av ../GERALD $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/
        rm -rf $PROCESSING/$LF
        /usr/local/dsu/bin/check_read_numbers.sh $SEQUENCER
#       rm -rf Temp
}

function bowtie {
        /usr/local/dsu/bin/openBIS_bowtie_alignment.sh
}


# no longer needed with the use of the RTA
function bustard {
        $BUSTARD --CIF . --GERALD=$CONFIG_FILE --control-lane=5 --make
        cd Bustard*
        echo "Starting parallel make..."
        make recursive -j 15 2>seq_make.err >seq_make.log
}


function cif2txt {
        cd $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls
        $CIF2TXT $LATEST_FOLDER/$INTENSITY_FOLDER $NUMBER_OF_CYCLES $NUMBER_OF_TILES

}


function bcl2qseq {
        export LF=`echo $LATEST_FOLDER | cut -d / -f8`
        $BCL -i $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/ -p $LATEST_FOLDER/$INTENSITY_FOLDER -o /net/bs-dsu-data/array0/dsu/processing/$LF --in-place --overwrite
        cd $PROCESSING/$LF
        make -j15
        chmod -R 755 .
        rsync -av . $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls
        rm -rf $PROCESSING/$LF
}


function create_srf {
        export SRF_DIR=Srf
        export ILLUMINA2SRF=/usr/local/bin/illumina2srf
        export LF=`echo $LATEST_FOLDER | cut -d / -f8`

        cd $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls
        [ -d $SRF_DIR ] || mkdir $SRF_DIR
        $ILLUMINA2SRF -clobber -b -o $SRF_DIR/"ETHZ_BSSE_"$LF"_1.srf" $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/s_1_*_qseq.txt 
        $ILLUMINA2SRF -clobber -b -o $SRF_DIR/"ETHZ_BSSE_"$LF"_2.srf" $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/s_2_*_qseq.txt &
        $ILLUMINA2SRF -clobber -b -o $SRF_DIR/"ETHZ_BSSE_"$LF"_3.srf" $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/s_3_*_qseq.txt &
        $ILLUMINA2SRF -clobber -b -o $SRF_DIR/"ETHZ_BSSE_"$LF"_4.srf" $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/s_4_*_qseq.txt &
        $ILLUMINA2SRF -clobber -b -o $SRF_DIR/"ETHZ_BSSE_"$LF"_5.srf" $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/s_5_*_qseq.txt &
        $ILLUMINA2SRF -clobber -b -o $SRF_DIR/"ETHZ_BSSE_"$LF"_6.srf" $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/s_6_*_qseq.txt &
        $ILLUMINA2SRF -clobber -b -o $SRF_DIR/"ETHZ_BSSE_"$LF"_7.srf" $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/s_7_*_qseq.txt &
        $ILLUMINA2SRF -clobber -b -o $SRF_DIR/"ETHZ_BSSE_"$LF"_8.srf" $LATEST_FOLDER/$INTENSITY_FOLDER/BaseCalls/s_8_*_qseq.txt &
        wait $!
        echo sleeping for 600 secs...
        sleep 600
        echo creating srf_info.txt...
        for i in `ls -1 ${SRF_DIR}/*.srf`; do
                /usr/local/bin/srf_info -l1 $i >> ${SRF_DIR}/srf_info.txt;
        done
        echo creating fastqs...
        cd ${SRF_DIR}
        /usr/local/dsu/bin/create_fastq.sh
        echo removing int and nse files ...
        rm $LATEST_FOLDER/$INTENSITY_FOLDER/*int* $LATEST_FOLDER/$INTENSITY_FOLDER/*nse*
}

if [ -f $ANALYSIS_STARTED ];then
        echo "Analysis already started/done"
        exit 255
fi

if [ -f $RUN_COMPLETED ];then
        touch $ANALYSIS_STARTED
        echo "Run completed! Starting Analysis..."
        export NUMBER_OF_LANES=`cat $LATEST_FOLDER/$INTENSITY_FOLDER/RTAConfiguration.xml | grep NumberOfLanes | cut -d \> -f2 | cut -d \< -f1`
        export NUMBER_OF_TILES=`cat $LATEST_FOLDER/$INTENSITY_FOLDER/RTAConfiguration.xml | grep TilesPerLane | cut -d \> -f2 | cut -d \< -f1`
        export PAIRED_END=`cat  $LATEST_FOLDER/$INTENSITY_FOLDER/RTAConfiguration.xml | grep IsPairedEndRun  | cut -d \> -f2 | cut -d \< -f1`
        export NUMBER_OF_CYCLES=`cat  $LATEST_FOLDER/$INTENSITY_FOLDER/RTAConfiguration.xml | grep MaxCycles | cut -d \> -f2 | cut -d \< -f1`
        sleep 600

        cif2txt
        bcl2qseq
        create_srf
        gerald_basic
        #gerald
        #bowtie
fi

