CL=./bis.sh
CMDS_FILE=commands.txt
PASSWORD=unimportant

echo "=========== Loading data from client ================="
while read cmd; do
    cmd=${cmd/<password>/$PASSWORD}
    $CL $cmd
done < $CMDS_FILE
