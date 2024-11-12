#   Copyright ETH 2023 Zürich, Scientific IT Services
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
from ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl import MasterDataRegistrationHelper
from ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data import ImportData
from ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options import ImportOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data import ImportFormat
from ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options import ImportMode
from java.lang import ProcessBuilder
from java.lang import String
from java.nio.charset import StandardCharsets
import sys
import inspect
import os

helper = MasterDataRegistrationHelper(sys.path)
api = CommonServiceProvider.getApplicationContext().getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME)
sessionToken = api.loginAsSystem()
sessionWorkspaceFiles = helper.uploadToAsSessionWorkspace(sessionToken, "imaging-test-data-model.xls")
print(sessionWorkspaceFiles)
importData = ImportData(ImportFormat.EXCEL, [sessionWorkspaceFiles[0]])
importOptions = ImportOptions(ImportMode.UPDATE_IF_EXISTS)
importResult = api.executeImport(sessionToken, importData, importOptions)

print("======================== imaging-test-data-model xls ingestion result ========================")
print(importResult.getObjectIds())

from java.util import ArrayList
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import SampleCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id import SpacePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id import ProjectIdentifier
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentIdentifier


def create_sample(api, space, project, experiment, code):
    creation = SampleCreation()
    creation.setTypeId(EntityTypePermId("IMAGING_SAMPLE"))
    creation.setSpaceId(SpacePermId(space))
    creation.setProjectId(ProjectIdentifier(project))
    creation.setExperimentId(ExperimentIdentifier(experiment))
    creation.setCode(code)

    creations = ArrayList()
    creations.add(creation)

    result = api.createSamples(sessionToken, creations)
    print(result)
    return result


# create_sample(api, "IMAGING", "/IMAGING/TEST", "/IMAGING/TEST/TEST_COLLECTION", "TEMPLATE-TEST")



# print(os.path.abspath(inspect.stack()[0][1]))
# # process = ProcessBuilder(["python3", "./source/core-plugins/imaging-test/1/imaging_test_example/importer.py"]).start()
# process = ProcessBuilder(["/home/alaskowski/venv/bin/python3", "./source/core-plugins/imaging-test/1/imaging_test_example/importer.py"]).start()
#
# exitCode = process.waitFor()
# if exitCode != 0:
#     error = String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8)
#     raise ValueError("Script evaluation failed: " + str(error))


api.logout(sessionToken)
print("======================== imaging-test-data-model xls ingestion result ========================")


