#! /bin/bash

if [ $# -lt 1 ]; then
    echo "Usage: $0 <server folder> [<service properties file>]"
    exit 1
fi

# Installation folder: where the stuff have been installed, where this script is,...
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
# Specify properties file path as absolute
if [ $# -gt 1 ]; then
	properties_file="$2"
	if [ "${properties_file#/}" == "${properties_file}" ]; then
		properties_file="`pwd`/${properties_file}"
	fi
fi
# Check whether given properties file exists and is a regular file.
if [ ! -f $properties_file ]; then
	echo Given properties file \'$properties_file\' does not exist!
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
cp -p "$installation_folder"/*.xml "$jetty_folder"/etc
cp -p "$installation_folder"/source-systemsx.ethz.ch.keystore "$jetty_folder"/etc

echo Preparing and installing web archive...
war_classes=WEB-INF/classes
mkdir -p "$war_classes" 
cp -p "$properties_file" "$war_classes"
jar -ufv "$installation_folder"/cifex.war "$war_classes"/service.properties
cp -p "$installation_folder"/cifex.war "$jetty_folder"/webapps
rm -rf WEB-INF

# Create symlinks for easier access
cd "$server_folder"
ln -s "${rel_jetty_folder}" jetty

cp -p "$installation_folder"/startup.sh "$jetty_folder"
cp -p "$installation_folder"/shutdown.sh "$jetty_folder"
cp -p "$installation_folder"/jetty.properties "$jetty_folder"

cd "$jetty_folder"
echo Starting Jetty...
./startup.sh