#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# MasterDataRegistrationTransaction Class
from ch.ethz.sis.openbis.generic.server.asapi.v3 import ApplicationServerApi
from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider
from ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id import CustomASServiceCode
from ch.ethz.sis.openbis.generic.asapi.v3.dto.service import CustomASServiceExecutionOptions

# "======================== Helper Functions ========================"

import os
import sys
from collections import deque
from java.nio.file import Files, Paths
from java.io import File
from java.util import HashMap
from java.util import ArrayList

TYPES_FOLDER = "%s/master-data/" % [p for p in sys.path if p.find('core-plugins') >= 0][0]
SCRIPTS = os.path.join(TYPES_FOLDER, 'scripts')


def get_all_scripts():
    scripts = HashMap()
    for rel_path, script in list_all_files(SCRIPTS):
        scripts.put(rel_path, script)

    return scripts

class ListForBinaries(ArrayList):
    def toString(self):
        return "%s blobs" % self.size()

def list_xls_byte_arrays():
    xls = ListForBinaries()
    for f in os.listdir(TYPES_FOLDER):
        if f.endswith('.xls') or f.endswith('.xlsx'):
            excel_file = open(os.path.join(TYPES_FOLDER, f))
            xls.add(excel_file.read())
            excel_file.close()
    return xls


def list_all_files(source_root_path):
    todo = []
    todo.append(File(source_root_path))
    while todo:
        f = todo.pop()
        if f.isDirectory():
            new_files = f.listFiles()
            if new_files is not None:
                todo.extend(f.listFiles())
            continue
        if f.isFile():
            source_file = f.getAbsolutePath()
            script_file = open(source_file)
            script = script_file.read()
            script_file.close()
            file_path = source_file.replace(source_root_path, "")
            if file_path.startswith("/"):
                file_path = file_path[1:]
            yield file_path, script

# "======================== Helper Functions ========================"

api = CommonServiceProvider.getApplicationContext().getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME)
sessionToken = api.loginAsSystem()
props = CustomASServiceExecutionOptions().withParameter('xls', list_xls_byte_arrays()).withParameter('scripts', get_all_scripts())
result = api.executeCustomASService(sessionToken, CustomASServiceCode("xls-import-api"), props);
print("======================== master-data xls ingestion result ========================")
print(result)
print("======================== master-data xls ingestion result ========================")
