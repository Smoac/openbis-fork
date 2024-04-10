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

OPENBIS_URL=$1

if [ -z "$1" ]; then
  echo "No Openbis url specified, using localhost"
  OPENBIS_URL="http://localhost:8888/openbis"
fi

DATA_FOLDER=$2

if [ -z "$2" ]; then
  echo "No nanonis data folder specified, using default one"
  DATA_FOLDER="../nanonis_example/data"
fi

if [ -d $VENV_NAME/bin/activate ]; then
  source $VENV_NAME/bin/activate
  pip3 install pybis -q
fi

python3 ../nanonis_example/nanonis_importer.py $OPENBIS_URL $DATA_FOLDER



