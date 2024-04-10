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
VENV_NAME=imaging_nanonis_venv
echo "Installing required libraries. Please wait."
# Creates python virtual environment for the sake of handling imaging-nanonis data
python3 -m venv $VENV_NAME

# Activation
source $VENV_NAME/bin/activate
# Installation of packages remove '-q' flag to see output
pip3 install -r python_requirements.txt -q

echo python3-path=$(pwd)/$VENV_NAME/bin/python >> ../../../imaging/1/dss/services/imaging/plugin.properties
echo "Installation finished"

