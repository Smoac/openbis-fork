#!/bin/bash

ADD_ARG=$1

STOR=bs-ssvr01
RUN=$(basename $(pwd))
cd /array0/Runs/${RUN} || exit 1

DEST_BSSE="dsf/Samples"
DEST_FMI="dsf-fmi/Samples"
DEST_BZ="dsf-biozentrum"
DEST_UB="dsf-unibs"
DEST_C1="dsf-customer1"
DEST_C2="dsf-customer2"
DEST_C3="dsf-customer3"

DEST1="${STOR}::${DEST_BZ}"
DEST2="${STOR}::${DEST_BZ}"
DEST3="${STOR}::${DEST_BZ}"
DEST4="${STOR}::${DEST_BZ}"
DEST5="${STOR}::${DEST_BSSE}"
DEST6="${STOR}::${DEST_BZ}"
DEST7="${STOR}::${DEST_BZ}"
DEST8="${STOR}::${DEST_FMI}"

# Lane
LANE1="UB-DE-257"  
LANE2="UB-DE-258"  
LANE3="UB-DE-259"  
LANE4="UB-DE-260"  
LANE6="UB-DE-261"  
LANE7="UB-DE-262"  
LANE8="FMI-HG-263"  
# Lane 5 BSSE Control Lane
LANE5="BSSE-IN-134"

rsync -xav --delete --delete-excluded --exclude "*-tmp-*" --exclude "Images" --exclude "*~" --exclude "s_[2345678]*" Data/ ${DEST1}/${LANE1}-${RUN}-1 --stats $ADD_ARG
rsync -xav --delete --delete-excluded --exclude "*-tmp-*" --exclude "Images" --exclude "*~" --exclude "s_[1345678]*" Data/ ${DEST2}/${LANE2}-${RUN}-2 --stats $ADD_ARG
rsync -xav --delete --delete-excluded --exclude "*-tmp-*" --exclude "Images" --exclude "*~" --exclude "s_[1245678]*" Data/ ${DEST3}/${LANE3}-${RUN}-3 --stats $ADD_ARG
rsync -xav --delete --delete-excluded --exclude "*-tmp-*" --exclude "Images" --exclude "*~" --exclude "s_[1235678]*" Data/ ${DEST4}/${LANE4}-${RUN}-4 --stats $ADD_ARG
rsync -xav --delete --delete-excluded --exclude "*-tmp-*" --exclude "Images" --exclude "*~" --exclude "s_[1234678]*" Data/ ${DEST5}/${LANE5}-${RUN}-5 --stats $ADD_ARG
rsync -xav --delete --delete-excluded --exclude "*-tmp-*" --exclude "Images" --exclude "*~" --exclude "s_[1234578]*" Data/ ${DEST6}/${LANE6}-${RUN}-6 --stats $ADD_ARG
rsync -xav --delete --delete-excluded --exclude "*-tmp-*" --exclude "Images" --exclude "*~" --exclude "s_[1234568]*" Data/ ${DEST7}/${LANE7}-${RUN}-7 --stats $ADD_ARG
rsync -xav --delete --delete-excluded --exclude "*-tmp-*" --exclude "Images" --exclude "*~" --exclude "s_[1234567]*" Data/ ${DEST8}/${LANE8}-${RUN}-8 --stats $ADD_ARG
# dirvish-timestemp
