#! /bin/bash
APPLICATION_NAME=genericopenbis

usage() {
	echo "Usage: $0 [--port <port number>] <server folder> [<service properties file> <log configuration file>]"
	exit 1
}

# Checks whether the number of arguments is smaller than one.
# We at least need the server folder.
check_arguments() {
	if [ $# -lt 1 ]; then
		usage
	fi
}

check_arguments $@
JETTY_PORT=8443
if [ $1 == "--port" ]; then
	shift
	check_arguments $@
	JETTY_PORT=$1
	shift
fi
check_arguments $@

# Installation folder: where the distribution zip file has been unzipped (and where this script resides)
installation_folder="`dirname $0`"
if [ ${installation_folder#/} == ${installation_folder} ]; then
	installation_folder="`pwd`/${installation_folder}"
fi
# Where the server will be installed.
server_folder=$1

if [ ${server_folder#/} == ${server_folder} ]; then
	server_folder="`pwd`/${server_folder}"
fi

properties_file="$installation_folder/service.properties"
logconf_file="$installation_folder/log.xml"
if [ $# -gt 1 ]; then
	if [ $# -lt 3 ]; then
		usage
	fi
	properties_file="$2"
	# Specify properties file path as absolute
	if [ "${properties_file#/}" == "${properties_file}" ]; then
		properties_file="`pwd`/${properties_file}"
	fi
	logconf_file="$3"
	# Specify log configuration file path as absolute
	if [ "logconf_file#/}" == "logconf_file}" ]; then
		logconf_file="`pwd`/logconf_file}"
	fi
fi
# Check whether given properties file exists and is a regular file.
if [ ! -f $properties_file ]; then
	echo Given properties file \'$properties_file\' does not exist!
	exit 1
fi

# Check whether given log configuration file exists and is a regular file.
if [ ! -f $logconf_file ]; then
	echo Given log configuration file \'$logconf_file\' does not exist!
	exit 1
fi

rel_jetty_folder="jetty-`cat $installation_folder/jetty-version.txt`"
jetty_folder="${server_folder}/${rel_jetty_folder}"

# Creates server folder.
mkdir -p "$server_folder"

# Checks whether a jetty folder already exists.
if [ -d $jetty_folder ]; then
	echo "There exists already a Jetty folder."
	echo "Please shutdown and remove this Jetty installation"
	echo "or choose another server folder."
	exit 1
fi

echo Unzipping Jetty...
# Files are unzipped in $rel_jetty_folder
unzip -q "$installation_folder/jetty.zip" -d "$server_folder"
cp -p "$installation_folder"/jetty.xml "$jetty_folder"/etc

echo Preparing and installing web archive...
war_classes=WEB-INF/classes
mkdir -p "$war_classes"/etc
# Replace 'service.properties' and 'log.xml' files in war
cp -p "$properties_file" "$war_classes/service.properties"
cp -p "$logconf_file" "$war_classes/etc/log.xml"
zip -u "$installation_folder"/$APPLICATION_NAME.war "$war_classes"/service.properties "$war_classes"/etc/log.xml
cp -p "$installation_folder"/$APPLICATION_NAME.war "$jetty_folder"/webapps
rm -rf WEB-INF

# Create symlinks for easier access.
cd "$server_folder"
ln -s "${rel_jetty_folder}" jetty

JETTY_BIN_DIR="$jetty_folder"/bin
cp -p "$installation_folder"/startup.sh "$JETTY_BIN_DIR"
cp -p "$installation_folder"/shutdown.sh "$JETTY_BIN_DIR"
cp -p "$installation_folder"/passwd.sh "$JETTY_BIN_DIR"

# Create a file called 'jetty.properties'.
JETTY_PROPERTIES="$JETTY_BIN_DIR"/jetty.properties
echo "JETTY_PORT=$JETTY_PORT" > "$JETTY_PROPERTIES"
echo "JETTY_STOP_PORT=8079" >> "$JETTY_PROPERTIES"
echo "JETTY_STOP_KEY=secret" >> "$JETTY_PROPERTIES"
# Here goes the path of the JVM in case you need to set it hard
echo "JVM=\"java\"" >> "$JETTY_PROPERTIES"
# The default memory of the JVM at start up.
echo "VM_STARTUP_MEM=\"256M\"" >> "$JETTY_PROPERTIES"
# The maximum memory for the JVM
echo "VM_MAX_MEM=\"786M\"" >> "$JETTY_PROPERTIES"

# Create a 'work' directory in jetty folder. Web applications will be unpacked there.
mkdir -p "$jetty_folder"/work

cd "$jetty_folder"
echo Starting Jetty...
./bin/startup.sh