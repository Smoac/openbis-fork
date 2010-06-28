#! /bin/bash

usage() {
	echo "Usage: $0 <server folder> [<service properties file> <startup properties file>]"
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

# Installation folder: where the distribution zip file has been unzipped (and where this script resides)
installation_folder="`dirname $0`"
if [ ${installation_folder#/} == ${installation_folder} ]; then
	installation_folder="`pwd`/${installation_folder}"
fi
# Where the server will be installed.
server_folder=$1
shift

if [ ${server_folder#/} == ${server_folder} ]; then
	server_folder="`pwd`/${server_folder}"
fi

properties_file="$installation_folder/service.properties"
logconf_file="$installation_folder/log.xml"
startup_properties_file="${installation_folder}/openbis.conf"
# Check whether given properties files exist and are regular file.
if [ "$1" -a "$2" ]; then
	if [ ! -f "$1" ]; then
		echo "Given properties file '$1' does not exist!"
		exit 1
	fi
	properties_file="$1"
	shift
	if [ "${properties_file#/}" == "${properties_file}" ]; then
		properties_file="`pwd`/${properties_file}"
	fi
        if [ ! -f "$1" ]; then
                echo "Given properties file '$1' does not exist!"
                exit 1
        fi
        startup_properties_file="$1"
        shift
        if [ "${startup_properties_file#/}" == "${startup_properties_file}" ]; then
                startup_properties_file="`pwd`/${startup_properties_file}"
        fi
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
test -f "$installation_folder"/jetty.xml && cp -p "$installation_folder"/jetty.xml "$jetty_folder"/etc
test -f "$jetty_folder"/etc/keystore && rm "$jetty_folder"/etc/keystore
cp -p "$installation_folder"/openBIS.keystore "$jetty_folder"/etc

echo Preparing and installing web archive...
war_file=openBIS.war
war_classes=WEB-INF/classes
mkdir -p "$war_classes"/etc
# Replace 'service.properties' and 'log.xml' files in war
test -f "$properties_file" && cp -p "$properties_file" "$war_classes/"
test -f "$logconf_file" && cp -p "$logconf_file" "$war_classes/etc/"
zip "$installation_folder"/$war_file "$war_classes"/service.properties "$war_classes"/etc/log.xml *.js *.html images/*
cp -p "$installation_folder"/$war_file "$jetty_folder"
rm -rf WEB-INF

# Create symlinks for easier access.
cd "$server_folder"
ln -s "${rel_jetty_folder}" jetty
cd jetty/etc
ln -s ../work/openbis/webapp/WEB-INF/classes/service.properties .
ln -s ../work/openbis/webapp/WEB-INF/classes/etc/log.xml .
ln -s ../bin/jetty.properties .
cd ../..

JETTY_BIN_DIR="$jetty_folder"/bin
cp -p "$installation_folder"/startup.sh "$JETTY_BIN_DIR"
cp -p "$installation_folder"/shutdown.sh "$JETTY_BIN_DIR"
cp -p "$installation_folder"/setup-env "$JETTY_BIN_DIR"
cp -p "$installation_folder"/passwd.sh "$JETTY_BIN_DIR"

# Create a file called 'jetty.properties'.
JETTY_PROPERTIES="$JETTY_BIN_DIR"/jetty.properties
cp $startup_properties_file "$JETTY_BIN_DIR"
echo "JETTY_STOP_PORT=8079" > "$JETTY_PROPERTIES"
echo "JETTY_STOP_KEY=secret" >> "$JETTY_PROPERTIES"

# Create a 'work' directory in jetty folder. Web applications will be unpacked there.
mkdir -p "$jetty_folder"/work

cd "$jetty_folder"
