#!/bin/bash
# Simply adds a 'p' to the int and nse files  

for i in `ls *int*`; do 
  #j=`echo $i |  cut -c 1-16` 
  mv $i $i.p
  #echo -e "Moving $i to $j";
done
