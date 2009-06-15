RES=../../../datastore_server/targets
TMP=./tmp

rm -rf $TMP	
mkdir -p $TMP
cp -R $RES/incoming $TMP
cp -R $RES/store $TMP
cp -R $RES/dropbox1 $TMP
cp -R $RES/dropbox2 $TMP
diff -r -x ".svn" expected-result $TMP