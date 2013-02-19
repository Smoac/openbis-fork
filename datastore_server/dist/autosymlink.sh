#!/bin/bash
pushd . > /dev/null
cd `dirname $0`

deleted="false"
java -cp "lib/datastore_server.jar:lib/*" ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil 2>/dev/null|while read line
do	

	if [ "$deleted" = "false" ];
	then
		rm -f lib/autolink-*.jar;
		deleted="true";
	fi;

	dropbox=`echo $line|cut -d" " -f1`;
        folder=`echo $line|cut -d" " -f2`;

	if [ -d ${folder}/lib ];
	then
		for jar in `ls ${folder}/lib/*.jar`;
		do
			echo "# ln -s $jar `pwd`/lib/autolink-${dropbox}-`basename $jar`";
			ln -s $jar `pwd`/lib/autolink-${dropbox}-`basename $jar`;
		done;
	fi;
done
popd > /dev/null
