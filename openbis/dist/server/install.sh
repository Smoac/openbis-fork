#! /bin/bash

nostartup=0
if [[ "$1" = '-n' || "$1" = '--nostartup' ]]; then
	nostartup=1
	shift
fi

if [ $# -lt 1 ]; then
    echo "Usage: $0 [--nostartup] <server folder> [<service properties file>]"
    echo "  --nostartup: do not start up tomcat automatically"
    exit 1
fi

installation_folder="`dirname $0`"
if [ ${installation_folder#/} == ${installation_folder} ]; then
	installation_folder="`pwd`/${installation_folder}"
fi
server_folder=$1
shift

if [ ${server_folder#/} == ${server_folder} ]; then
	server_folder="`pwd`/${server_folder}"
fi

properties_file="${installation_folder}/service.properties"
# Check whether given properties file exists and is a regular file.
if [ $1 ]; then
	if [ ! -f $1 ]; then
		echo "Given properties file '$1' does not exist!"
		exit 1
	fi
	properties_file="$1"
	if [ "${properties_file#/}" == "${properties_file}" ]; then
		properties_file="`pwd`/${properties_file}"
	fi
fi

rel_tomcat_folder="apache-tomcat-`cat \"${installation_folder}/tomcat-version.txt\"`"
tomcat_folder="${server_folder}/${rel_tomcat_folder}"
rel_openbis_web_folder=webapps/openbis
openbis_web_folder="${tomcat_folder}/${rel_openbis_web_folder}"
rel_openbis_web_inf=${rel_openbis_web_folder}/WEB-INF
openbis_web_inf="${tomcat_folder}/${rel_openbis_web_inf}"
startup_script_path="${tomcat_folder}/bin/startup.sh"

# Creates server folder.
mkdir -p "${server_folder}"

# Checks whether a tomcat folder already exists.
if [ -d "${tomcat_folder}" ]; then
	echo "There exists already a Tomcat folder."
	echo "Please shutdown and remove this Tomcat installation"
	echo "or choose another server folder."
	exit 1
fi

unzip -q "${installation_folder}/apache-tomcat.zip" -d "$server_folder"
cp -p "${installation_folder}"/openBIS.keystore "${tomcat_folder}"
cp -p "${installation_folder}/server.xml" "${tomcat_folder}/conf"
cp -p "${installation_folder}/passwd.sh" "${tomcat_folder}/bin"
chmod 755 "${tomcat_folder}/bin/passwd.sh"
STARTUP_TMP=`mktemp startup.sh.XXXXXX`
sed -e "33i\\
CATALINA_OPTS=\"-Xmx512M -Djavax.net.ssl.trustStore=openBIS.keystore -Ddatabase.create-from-scratch=false -Ddatabase.script-single-step-mode=false\"\\
export CATALINA_OPTS\\
\\
if [ \${PRGDIR#/} == \${PRGDIR} ]; then\\
        PRGDIR=\"\`pwd\`/\${PRGDIR}\"\\
fi\\
cd \"\${PRGDIR}/..\"\\
" -e "s/\/bin\/sh/\/bin\/bash/" "${startup_script_path}" "${startup_script_path}" > ${STARTUP_TMP}
mv ${STARTUP_TMP} "${startup_script_path}"
chmod 744 "${tomcat_folder}"/bin/*.sh

unzip -q "${installation_folder}/openBIS.war" -d "${openbis_web_folder}"
mkdir "${openbis_web_inf}/conf"

# Copy configuration files

cp -p "${properties_file}" "${openbis_web_inf}/classes/service.properties"
echo Given properties file \'${properties_file}\' copied to \'${openbis_web_inf}/classes/service.properties\'

# Create symlinks for easier access

cd "${server_folder}"
ln -s "${rel_tomcat_folder}" apache-tomcat
cd "${tomcat_folder}"
mkdir etc
ln -s "../${rel_openbis_web_inf}"/classes/service.properties etc/
cp -p "${rel_openbis_web_inf}"/classes/etc/passwd etc/

if [ "$nostartup" -eq 0 ]; then
	echo Starting tomcat...
	bin/startup.sh
fi