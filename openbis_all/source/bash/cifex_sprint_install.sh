# This is a very simple version of the script which installs the cifex
# You have to change the INSTALL_URL, PREV and NEW variables by yourself!

PREV=cifex-S67
NEW=cifex-S68
INSTALL_URL=http://cisd-ci.ethz.ch:8090/cruisecontrol/artifacts/cifex/20091111035157/cifex-SNAPSHOT-r13295.zip

unalias cp
unalias rm

./cifex/jetty/bin/shutdown.sh
mv cifex-* old/
rm -f cifex

mkdir $NEW
ln -s $NEW cifex
cd cifex
wget $INSTALL_URL

unzip cifex-*.zip
OLD_INSTALL=../../old/$PREV/jetty
cp $OLD_INSTALL/etc/service.properties .
cp $OLD_INSTALL/etc/keystore .
cp $OLD_INSTALL/etc/jetty.xml .
./install.sh ..
