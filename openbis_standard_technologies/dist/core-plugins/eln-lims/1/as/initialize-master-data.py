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
from ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl import MasterDataRegistrationHelper
import sys

from ch.systemsx.cisd.openbis.generic.server.hotfix import ELNFixes
from ch.systemsx.cisd.openbis.generic.server.hotfix import ELNAnnotationsMigration
from ch.systemsx.cisd.openbis.generic.server.hotfix import ELNCollectionTypeMigration

if ELNFixes.isELNInstalled():
    ELNFixes.beforeUpgrade()
ELNAnnotationsMigration.beforeUpgrade()
ELNCollectionTypeMigration.beforeUpgrade()

helper = MasterDataRegistrationHelper(sys.path)
api = CommonServiceProvider.getApplicationContext().getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME)
sessionToken = api.loginAsSystem()
props = CustomASServiceExecutionOptions().withParameter('xls', helper.listXlsByteArrays())\
    .withParameter('xls_name', 'ELN-LIMS').withParameter('update_mode', 'UPDATE_IF_EXISTS')\
    .withParameter('scripts', helper.getAllScripts())
result = api.executeCustomASService(sessionToken, CustomASServiceCode("xls-import-api"), props)

ELNCollectionTypeMigration.afterUpgrade()

print("======================== master-data xls ingestion result ========================")
print(result)
print("======================== master-data xls ingestion result ========================")
