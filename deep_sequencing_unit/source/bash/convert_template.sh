#!/bin/bash
#Usage: eland2wig <chromosomes> <trackname> <fragmentsize> <windowsize> <strand>
eland2wig drosmel.genome Lane5 160 10 0 <s_5_export.txt | gzip - -9 >s_5.wig.gz
