#!/bin/bash

RM=/usr/bin/rm

ssh bs-isvr01-s.ethz.ch /net/bs-bsse/local0/local/dsu/bin/create_srfs.sh

# get rid of the intensity and noise files generated for the srfs
$RM /array0/dsu/processing/*/Data/Intensities/*int* /array0/dsu/processing/*/Data/Intensities/*nse*

rsync /array0/dsu/processing/* /array0/dsu/dss/incoming