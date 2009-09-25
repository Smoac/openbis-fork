
./test-3v.sh $@
result_3v=$?
./test-yeastx.sh $@
result_yeastx=$?
if [ $result_3v -ne 0 -o $result_yeastx -ne 0 ]; then
	exit 1;
fi

