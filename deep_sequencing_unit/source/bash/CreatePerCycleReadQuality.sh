#!/bin/bash

R_SCRIP_PATH=/usr/local/dsu/R-scripts

for i in `ls -1 *.fastq`
do
        Rscript --vanilla $R_SCRIP_PATH/CreatePerCycleReadQuality.R $i &
        FC_NAME=`echo $i | cut -f1 -d \. | cut -f1-4 -d _`
done

wait $!

sleep 180

/usr/local/dsu/pdftk-1.41/pdftk/pdftk *.pdf cat output "${FC_NAME}_ALL_boxplots.pdf"

for i in 1 2 3 4 6 7 8
do
        if [ -f "${FC_NAME}_${i}.fastq_boxplot.pdf" ]; then
                /usr/local/dsu/pdftk-1.41/pdftk/pdftk "${FC_NAME}_${i}.fastq_boxplot.pdf"  "${FC_NAME}_5.fastq_boxplot.pdf" cat output "${FC_NAME}_${i}_boxplot.pdf"
                rm "${FC_NAME}_${i}.fastq_boxplot.pdf"
        fi
done
mv ${FC_NAME}_5.fastq_boxplot.pdf ${FC_NAME}_5_boxplot.pdf

echo DONE
