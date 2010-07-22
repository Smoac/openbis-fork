#!/bin/bash

R_SCRIP_PATH=/usr/local/dsu/R-scripts

for i in `ls -1 *.fastq`
do
        Rscript --vanilla $R_SCRIP_PATH/createQaReport.R $i &
done
wait $!
echo * DONE *
