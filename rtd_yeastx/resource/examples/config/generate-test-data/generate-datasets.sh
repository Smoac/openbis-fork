#!/bin/bash
# Quick and dirty script ot generate test data for yeastX metabol database.
# In your openBIS database there has to be a sample with the code 'TEST_SAMPLE' inside the 'TEST' space.
# The sample has to be connected to any experiment.
# After running the script copy the content of the 'test-datasets' directory to the:
     targets/yeastx/incoming/

# how many datasets should be created per one upload dir
FILES_NUM=3
# how many upload directories should be generated
DIRS_NUM=3

dir_num=1; 
while [ $dir_num -le $DIRS_NUM ]; do 
  DIR=test-datasets/t$dir_num

  rm -rf $DIR
  mkdir -p $DIR
  cat header.tsv > $DIR/index.tsv; 
  i=1; 
  while [ $i -le $FILES_NUM ]; do 
	l=`cat line-eicml.tsv`; 
	# Uncomment this line and comment the one above to generate fiaML datasets 
	#l=`cat line-fiaml.tsv`; 
	echo $i$l | tr " " "\t" >> $DIR/index.tsv; 
	cp x.eicML $DIR/$i.eicML; 
	# Uncomment this line and comment the one above to generate fiaML datasets 
	#cp x.fiaML $DIR/$i.fiaML; 
	let i+=1; 
  done

  let dir_num+=1
done