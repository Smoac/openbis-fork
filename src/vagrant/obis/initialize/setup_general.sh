#!/bin/env bash

# set localtime
sudo rm /etc/localtime
sudo ln -s /usr/share/zoneinfo/Europe/Zurich /etc/localtime

# set locale
sudo echo "export LC_ALL=en_US.utf8" >> /etc/environment
sudo echo "export LANG=en_US.utf8" >> /etc/environment

# install some often used packages
sudo yum -y install wget
sudo yum -y install epel-release
sudo yum -y install unzip
sudo yum -y install vim
sudo yum -y install nano
sudo yum -y install nmap
sudo yum -y install net-tools
