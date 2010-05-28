#! /bin/bash

usage() {
	echo "Usage: $0 [--port <port number>] <server folder>"
	echo "Note that service.properties, log.xml, keystore, all .js and .html files and all files in the images/ sub-directory"
	echo "are picked up from this directory if they exist. This enables to customize e.g. the favicon the login header page."
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
test -f "$installation_folder"/keystore && cp -p "$installation_folder"/keystore "$jetty_folder"/etc

echo Preparing and installing web archive...
war_classes=WEB-INF/classes
mkdir -p "$war_classes"/etc
# Replace 'service.properties' and 'log.xml' files in war
test -f "$properties_file" && cp -p "$properties_file" "$war_classes/"
test -f "$logconf_file" && cp -p "$logconf_file" "$war_classes/etc/"
zip -u "$installation_folder"/cifex.war "$war_classes"/service.properties "$war_classes"/etc/log.xml *.js *.html images/*
cp -p "$installation_folder"/cifex.war "$jetty_folder"/webapps
rm -rf WEB-INF

# Create symlinks for easier access.
cd "$server_folder"
ln -s "${rel_jetty_folder}" jetty
cd jetty/etc
ln -s ../work/webapp/WEB-INF/classes/service.properties .
ln -s ../work/webapp/WEB-INF/classes/etc/log.xml .
ln -s ../work/webapp/info-dictionary.js .
ln -s ../work/webapp/faq.html .
ln -s ../work/webapp/disclaimer.html .
ln -s ../work/webapp/tools.html .
ln -s ../work/webapp/loginHeader.html .
ln -s ../bin/jetty.properties .
cd ../..

JETTY_BIN_DIR="$jetty_folder"/bin
cp -p "$installation_folder"/startup.sh "$JETTY_BIN_DIR"
cp -p "$installation_folder"/shutdown.sh "$JETTY_BIN_DIR"

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