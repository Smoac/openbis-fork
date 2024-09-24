#!/bin/bash
#
#  Copyright ETH 2024 Zürich, Scientific IT Services
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#
echo "Installing Python 3.10 for Nanonis files handling"

# install setup tools
apt install software-properties-common curl -y
# add repo with python
add-apt-repository ppa:deadsnakes/ppa
apt update -y
# install python and venv
apt install python3.10 python3.10-venv -y
# install pip
curl -sS https://bootstrap.pypa.io/get-pip.py | python3.10

echo "Python3.10 installed"