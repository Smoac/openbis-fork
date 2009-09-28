#!/bin/bash
# Simply removes the p from the int and nse files  

for i in `ls *.p`; do 
  j=`echo $i |  cut -c 1-16` 
  mv $i $j
  echo -e "Moving $i to $j";
done
