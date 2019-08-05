#
# Copyright 2016 ETH Zuerich, Scientific IT Services
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
import jarray
# To obtain the openBIS URL
from ch.systemsx.cisd.openbis.dss.generic.server import DataStoreServer;
from ch.systemsx.cisd.openbis.generic.client.web.client.exception import UserFailureException
from collections import deque
# Zip Format
from java.io import File;
from java.io import FileInputStream;
from java.io import FileOutputStream;
from java.lang import String
from java.lang import StringBuilder
from java.util.zip import ZipEntry;
from java.util.zip import ZipOutputStream;
from org.apache.commons.io import FileUtils
# Java Core
from org.apache.commons.io import IOUtils

OPENBISURL = DataStoreServer.getConfigParameters().getServerURL() + "/openbis/openbis";
V3_DSS_BEAN = "data-store-server_INTERNAL";

#V3 API - Metadata

from HTMLParser import HTMLParser

from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentTypeSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentTypeFetchOptions;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleTypeSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetTypeSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.property import DataType

#V3 API - Files
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search import DataSetFileSearchCriteria;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions import DataSetFileFetchOptions;
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id import DataSetFilePermId;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id import DataSetPermId;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download import DataSetFileDownloadOptions;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download import DataSetFileDownloadReader

#JSON
from ch.ethz.sis.openbis.generic.server.sharedapi.v3.json import GenericObjectMapper;
from com.fasterxml.jackson.databind import SerializationFeature

#Session Workspace
from ch.systemsx.cisd.openbis.dss.client.api.v1 import DssComponentFactory

#Logging
from ch.systemsx.cisd.common.logging import LogCategory;
from org.apache.log4j import Logger;
operationLog = Logger.getLogger(str(LogCategory.OPERATION) + ".exportsApi.py");

#AVI API - DTO
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project import Project
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment import Experiment
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample import Sample
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset import DataSet

#DOCX
from ch.ethz.sis import DOCXBuilder

#Images export for word
from org.jsoup import Jsoup;

class MLStripper(HTMLParser):
    def __init__(self):
        self.reset()
        self.fed = []
    def handle_data(self, d):
        self.fed.append(d)
    def get_data(self):
        return ''.join(self.fed)

def strip_tags(html):
    s = MLStripper()
    s.feed(html)
    return s.get_data()


def displayResult(isOk, tableBuilder, result=None):
    if isOk:
        tableBuilder.addHeader("STATUS");
        tableBuilder.addHeader("MESSAGE");
        tableBuilder.addHeader("RESULT");
        row = tableBuilder.addRow();
        row.setCell("STATUS", "OK");
        row.setCell("MESSAGE", "Operation Successful");
        row.setCell("RESULT", result);
    else:
        tableBuilder.addHeader("STATUS");
        tableBuilder.addHeader("MESSAGE");
        row = tableBuilder.addRow();
        row.setCell("STATUS", "FAIL");
        row.setCell("MESSAGE", "Operation Failed");


def addToExportWithoutRepeating(entitiesToExport, entityFound):
    found = False;
    for entityToExport in entitiesToExport:
        if entityToExport["type"] != "FILE" and entityToExport["permId"] == entityFound["permId"] and entityToExport["type"] == entityFound["type"]:
            found = True;
            break;
    if not found:
        entitiesToExport.append(entityFound);


def validateDataSize(entitiesToExport, tr):
    limitDataSizeInMegabytes = getConfigurationProperty(tr, 'limit-data-size-megabytes')
    if limitDataSizeInMegabytes is None:
        limitDataSizeInMegabytes = 500;
    else:
        limitDataSizeInMegabytes = int(limitDataSizeInMegabytes);
    limitDataSizeInBytes = 1000000 * limitDataSizeInMegabytes;
    estimatedSizeInBytes = 0;
    for entityToExport in entitiesToExport:
        if entityToExport["type"] == "FILE" and entityToExport["isDirectory"] == False:
            estimatedSizeInBytes += entityToExport["length"];
        elif entityToExport["type"] != "FILE":
            estimatedSizeInBytes += 12000;  # AVG File Metadata size
    estimatedSizeInMegabytes = estimatedSizeInBytes / 1000000;
    operationLog.info(
        "Size Limit check - limitDataSizeInBytes: " + str(limitDataSizeInBytes) + " > " + " estimatedSizeInBytes: " + str(estimatedSizeInBytes));
    if estimatedSizeInBytes > limitDataSizeInBytes:
        raise UserFailureException("The selected data is " + str(estimatedSizeInMegabytes) + " MB that is bigger than the configured limit of " + str(
            limitDataSizeInMegabytes) + " MB");


def findEntitiesToExport(params):
    sessionToken = params.get("sessionToken");
    v3 = ServiceProvider.getV3ApplicationService();
    v3d = ServiceProvider.getApplicationContext().getBean(V3_DSS_BEAN);
    metadataOnly = params.get("metadataOnly");
    entitiesToExpand = deque([]);
    entitiesToExport = [];
    entities = params.get("entities");

    for entity in entities:
        entityAsPythonMap = { "type" : entity.get("type"), "permId" : entity.get("permId"), "expand" : entity.get("expand") };
        entitiesToExport.append(entityAsPythonMap);
        if entity.get("expand"):
            entitiesToExpand.append(entityAsPythonMap);

    operationLog.info("Found %d entities to expand." % len(entitiesToExpand))
    while entitiesToExpand:
        entityToExpand = entitiesToExpand.popleft();
        type = entityToExpand["type"];
        permId = entityToExpand["permId"];
        operationLog.info("Expanding type: " + str(type) + " permId: " + str(permId));

        if type == "ROOT":
            criteria = SpaceSearchCriteria();
            results = v3.searchSpaces(sessionToken, criteria, SpaceFetchOptions());
            operationLog.info("Found: " + str(results.getTotalCount()) + " spaces");
            for space in results.getObjects():
                entityFound = {"type": "SPACE", "permId": space.getCode(), "registrationDate": space.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "SPACE":
            criteria = ProjectSearchCriteria();
            criteria.withSpace().withCode().thatEquals(permId);
            results = v3.searchProjects(sessionToken, criteria, ProjectFetchOptions());
            operationLog.info("Found: " + str(results.getTotalCount()) + " projects");
            for project in results.getObjects():
                entityFound = {"type": "PROJECT", "permId": project.getPermId().getPermId(), "registrationDate": project.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "PROJECT":
            criteria = ExperimentSearchCriteria();
            criteria.withProject().withPermId().thatEquals(permId);
            results = v3.searchExperiments(sessionToken, criteria, ExperimentFetchOptions());
            operationLog.info("Found: " + str(results.getTotalCount()) + " experiments");
            for experiment in results.getObjects():
                entityFound = {"type": "EXPERIMENT", "permId": experiment.getPermId().getPermId(),
                               "registrationDate": experiment.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "EXPERIMENT":
            criteria = SampleSearchCriteria();
            criteria.withExperiment().withPermId().thatEquals(permId);
            results = v3.searchSamples(sessionToken, criteria, SampleFetchOptions());
            operationLog.info("Found: " + str(results.getTotalCount()) + " samples");

            dCriteria = DataSetSearchCriteria();
            dCriteria.withExperiment().withPermId().thatEquals(permId);
            dCriteria.withoutSample();
            fetchOptions = DataSetFetchOptions()
            fetchOptions.withDataStore()
            dResults = v3.searchDataSets(sessionToken, dCriteria, fetchOptions);
            operationLog.info("Found: " + str(dResults.getTotalCount()) + " datasets");
            for dataset in dResults.getObjects():
                entityFound = {"type": "DATASET", "permId": dataset.getPermId().getPermId(), "registrationDate": dataset.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);

            operationLog.info("Found: " + str(results.getTotalCount()) + " samples");
            for sample in results.getObjects():
                entityFound = {"type": "SAMPLE", "permId": sample.getPermId().getPermId(), "registrationDate": sample.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "SAMPLE":
            criteria = DataSetSearchCriteria();
            criteria.withSample().withPermId().thatEquals(permId);
            fetchOptions = DataSetFetchOptions()
            fetchOptions.withDataStore()
            results = v3.searchDataSets(sessionToken, criteria, fetchOptions);
            operationLog.info("Found: " + str(results.getTotalCount()) + " datasets");
            for dataset in results.getObjects():
                entityFound = {"type": "DATASET", "permId": dataset.getPermId().getPermId(), "registrationDate": dataset.getRegistrationDate()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
                entitiesToExpand.append(entityFound);
        if type == "DATASET" and not metadataOnly:
            criteria = DataSetFileSearchCriteria();
            criteria.withDataSet().withPermId().thatEquals(permId);
            results = v3d.searchFiles(sessionToken, criteria, DataSetFileFetchOptions());
            operationLog.info("Found: " + str(results.getTotalCount()) + " files");
            for file in results.getObjects():
                entityFound = {"type": "FILE", "permId": permId, "path": file.getPath(), "isDirectory": file.isDirectory(),
                               "length": file.getFileLength()};
                addToExportWithoutRepeating(entitiesToExport, entityFound);
    return entitiesToExport


# Removes temporal folder and zip
def cleanUp(tempDirPath, tempZipFilePath):
    FileUtils.forceDelete(File(tempDirPath));
    FileUtils.forceDelete(File(tempZipFilePath));


# Generates ZIP file and stores it in workspace
def generateZipFile(entities, includeRoot, sessionToken, tempDirPath, tempZipFilePath):
    # Create Zip File
    fos = FileOutputStream(tempZipFilePath);
    zos = ZipOutputStream(fos);

    generateFilesInZip(zos, entities, includeRoot, sessionToken, tempDirPath)

    zos.close();
    fos.close();


def generateFilesInZip(zos, entities, includeRoot, sessionToken, tempDirPath):
    # Services used during the export process
    v3 = ServiceProvider.getV3ApplicationService();
    v3d = ServiceProvider.getApplicationContext().getBean(V3_DSS_BEAN);
    objectCache = {};
    objectMapper = GenericObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    # To avoid empty directories on the zip file, it makes the first found entity the base directory
    baseDirToCut = None;
    fileMetadata = []
    emptyZip = True

    for entity in entities:
        type = entity["type"];
        permId = entity["permId"];
        operationLog.info("exporting type: " + str(type) + " permId: " + str(permId));
        entityObj = None;
        entityFilePath = None;

        if type == "SPACE":
            pass  # Do nothing
        if type == "PROJECT":
            criteria = ProjectSearchCriteria();
            criteria.withPermId().thatEquals(permId);
            fetchOps = ProjectFetchOptions();
            fetchOps.withSpace();
            fetchOps.withRegistrator();
            fetchOps.withModifier();
            entityObj = v3.searchProjects(sessionToken, criteria, fetchOps).getObjects().get(0);
            entityFilePath = getFilePath(entityObj.getSpace().getCode(), entityObj.getCode(), None, None, None);
        if type == "EXPERIMENT":
            criteria = ExperimentSearchCriteria();
            criteria.withPermId().thatEquals(permId);
            fetchOps = ExperimentFetchOptions();
            fetchOps.withType();
            fetchOps.withProject().withSpace();
            fetchOps.withRegistrator();
            fetchOps.withModifier();
            fetchOps.withProperties();
            fetchOps.withTags();
            entityObj = v3.searchExperiments(sessionToken, criteria, fetchOps).getObjects().get(0);
            entityFilePath = getFilePath(entityObj.getProject().getSpace().getCode(), entityObj.getProject().getCode(), entityObj.getCode(), None,
                                         None);
        if type == "SAMPLE":
            criteria = SampleSearchCriteria();
            criteria.withPermId().thatEquals(permId);
            fetchOps = SampleFetchOptions();
            fetchOps.withType();
            fetchOps.withExperiment().withProject().withSpace();
            fetchOps.withRegistrator();
            fetchOps.withModifier();
            fetchOps.withProperties();
            fetchOps.withTags();
            fetchOps.withParents().withProperties();
            fetchOps.withChildren().withProperties();
            entityObj = v3.searchSamples(sessionToken, criteria, fetchOps).getObjects().get(0);
            entityFilePath = getFilePath(entityObj.getExperiment().getProject().getSpace().getCode(),
                                         entityObj.getExperiment().getProject().getCode(), entityObj.getExperiment().getCode(), entityObj.getCode(),
                                         None);
        if type == "DATASET":
            criteria = DataSetSearchCriteria();
            criteria.withPermId().thatEquals(permId);
            fetchOps = DataSetFetchOptions();
            fetchOps.withType();
            fetchOps.withSample();
            fetchOps.withExperiment().withProject().withSpace();
            fetchOps.withRegistrator();
            fetchOps.withModifier();
            fetchOps.withProperties();
            fetchOps.withTags();
            fetchOps.withParents().withProperties();
            fetchOps.withChildren().withProperties();
            entityObj = v3.searchDataSets(sessionToken, criteria, fetchOps).getObjects().get(0);

            sampleCode = None
            if (entityObj.getSample() is not None):
                sampleCode = entityObj.getSample().getCode();

            entityFilePath = getFilePath(entityObj.getExperiment().getProject().getSpace().getCode(),
                                         entityObj.getExperiment().getProject().getCode(), entityObj.getExperiment().getCode(), sampleCode,
                                         entityObj.getCode());
        if type == "FILE" and not entity["isDirectory"]:
            datasetEntityObj = objectCache[entity["permId"]];
            sampleCode = None
            if (datasetEntityObj.getSample() is not None):
                sampleCode = datasetEntityObj.getSample().getCode();

            datasetEntityFilePath = getFilePath(datasetEntityObj.getExperiment().getProject().getSpace().getCode(),
                                                datasetEntityObj.getExperiment().getProject().getCode(), datasetEntityObj.getExperiment().getCode(),
                                                sampleCode, datasetEntityObj.getCode());
            filePath = datasetEntityFilePath + "/" + entity["path"];

            if not includeRoot:
                filePath = filePath[
                           len(baseDirToCut):]  # To avoid empty directories on the zip file, it makes the first found entity the base directory

            rawFileInputStream = DataSetFileDownloadReader(v3d.downloadFiles(sessionToken, [DataSetFilePermId(DataSetPermId(permId), entity["path"])],
                                                                             DataSetFileDownloadOptions())).read().getInputStream();
            rawFile = File(tempDirPath + filePath + ".json");
            rawFile.getParentFile().mkdirs();
            IOUtils.copyLarge(rawFileInputStream, FileOutputStream(rawFile));
            addToZipFile(filePath, rawFile, zos);
            emptyZip = False

        # To avoid empty directories on the zip file, it makes the first found entity the base directory
        if not includeRoot:
            if baseDirToCut is None and entityFilePath is not None:
                baseDirToCut = entityFilePath[:entityFilePath.rfind('/')];
            if entityFilePath is not None:
                entityFilePath = entityFilePath[len(baseDirToCut):]
        #

        if entityObj is not None:
            objectCache[permId] = entityObj;

        operationLog.info("--> Entity type: " + type + " permId: " + permId + " obj: " + str(entityObj is not None) + " path: " + str(
            entityFilePath) + " before files.");
        if entityObj is not None and entityFilePath is not None:
            # JSON
            entityJson = String(objectMapper.writeValueAsString(entityObj));
            fileMetadatum = addFile(tempDirPath, entityFilePath, "json", entityJson.getBytes(), zos);
            fileMetadata.append(fileMetadatum)
            emptyZip = False
            # TEXT
            entityTXT = String(getTXT(entityObj, v3, sessionToken, False));
            fileMetadatum = addFile(tempDirPath, entityFilePath, "txt", entityTXT.getBytes(), zos);
            fileMetadata.append(fileMetadatum)
            # DOCX
            entityDOCX = getDOCX(entityObj, v3, sessionToken, False);
            fileMetadatum = addFile(tempDirPath, entityFilePath, "docx", entityDOCX, zos);
            fileMetadata.append(fileMetadatum)
            # HTML
            entityHTML = getDOCX(entityObj, v3, sessionToken, True);
            fileMetadatum = addFile(tempDirPath, entityFilePath, "html", entityHTML, zos);
            fileMetadata.append(fileMetadatum)
            operationLog.info("--> Entity type: " + type + " permId: " + permId + " post html.");
    if emptyZip:
        raise IOError('Nothing added to ZIP file.')
    return fileMetadata


def generateDownloadUrl(sessionToken, tempZipFileName, tempZipFilePath):
    dssComponent = DssComponentFactory.tryCreate(sessionToken, OPENBISURL);

    # Store on workspace to be able to generate a download link
    operationLog.info("Zip file can be found on the temporal directory: " + tempZipFilePath);
    dssComponent.putFileToSessionWorkspace(tempZipFileName, FileInputStream(File(tempZipFilePath)));
    tempZipFileWorkspaceURL = DataStoreServer.getConfigParameters().getDownloadURL() + "/datastore_server/session_workspace_file_download?sessionID=" + sessionToken + "&filePath=" + tempZipFileName;
    operationLog.info("Zip file can be downloaded from the workspace: " + tempZipFileWorkspaceURL);
    return tempZipFileWorkspaceURL


def getDOCX(entityObj, v3, sessionToken, isHTML):
    docxBuilder = DOCXBuilder();
    docxBuilder.addTitle(entityObj.getCode());
    docxBuilder.addHeader("Identification Info");
    
    typeObj = None
    if isinstance(entityObj, Project):
        docxBuilder.addProperty("Kind", "Project");
    if isinstance(entityObj, Experiment):
        docxBuilder.addProperty("Kind", "Experiment");
        searchCriteria = ExperimentTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    if isinstance(entityObj, Sample):
        docxBuilder.addProperty("Kind", "Sample");
        searchCriteria = SampleTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    if isinstance(entityObj, DataSet):
        docxBuilder.addProperty("Kind", "DataSet");
        searchCriteria = DataSetTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    
    if not isinstance(entityObj, Project):
        docxBuilder.addProperty("Type", entityObj.getType().getCode());
    
    if(entityObj.getRegistrator() is not None):
        docxBuilder.addProperty("Registrator", entityObj.getRegistrator().getUserId());
        docxBuilder.addProperty("Registration Date", str(entityObj.getRegistrationDate()));
    
    if entityObj.getModifier() is not None:
        docxBuilder.addProperty("Modifier", entityObj.getModifier().getUserId());
        docxBuilder.addProperty("Modification Date", str(entityObj.getModificationDate()));
    
    
    if isinstance(entityObj, Project):
        description = entityObj.getDescription();
        if description is not None:
            docxBuilder.addHeader("Description");
            docxBuilder.addParagraph(entityObj.getDescription());
    
    if isinstance(entityObj, Sample) or isinstance(entityObj, DataSet):
        docxBuilder.addHeader("Parents");
        parents = entityObj.getParents();
        for parent in parents:
            relCodeName = parent.getCode();
            if "NAME" in parent.getProperties():
                relCodeName = relCodeName + " (" + parent.getProperties()["NAME"] + ")";
            docxBuilder.addParagraph(relCodeName);
        
        docxBuilder.addHeader("Children");
        children = entityObj.getChildren();
        for child in children:
            relCodeName = child.getCode();
            if "NAME" in child.getProperties():
                relCodeName = relCodeName + " (" + child.getProperties()["NAME"] + ")";
            docxBuilder.addParagraph(relCodeName);
    
    if not isinstance(entityObj, Project):
        docxBuilder.addHeader("Properties");
        propertyAssigments = typeObj.getPropertyAssignments();
        properties = entityObj.getProperties();
        for propertyAssigment in propertyAssigments:
            propertyType = propertyAssigment.getPropertyType();
            if propertyType.getCode() in properties:
                propertyValue = properties[propertyType.getCode()];
                if propertyType.getDataType() is DataType.MULTILINE_VARCHAR:
                    doc = Jsoup.parse(propertyValue);
                    imageElements = doc.select("img");
                    for imageElement in imageElements:
                        imageSrc = imageElement.attr("src");
                        propertyValue = propertyValue.replace(imageSrc, DataStoreServer.getConfigParameters().getServerURL() + imageSrc + "?sessionID=" + sessionToken);
                if propertyValue != u"\uFFFD(undefined)":
                    docxBuilder.addProperty(propertyType.getLabel(), propertyValue);
    
    if isHTML:
        return docxBuilder.getHTMLBytes();
    else:
        return docxBuilder.getDocBytes();

def getTXT(entityObj, v3, sessionToken, isRichText):
    txtBuilder = StringBuilder();
    txtBuilder.append(entityObj.getCode()).append("\n");
    txtBuilder.append("# Identification Info:").append("\n");
    
    typeObj = None
    if isinstance(entityObj, Project):
        txtBuilder.append("- Kind: Project").append("\n");
    if isinstance(entityObj, Experiment):
        txtBuilder.append("- Kind: Experiment").append("\n");
        searchCriteria = ExperimentTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    if isinstance(entityObj, Sample):
        txtBuilder.append("- Kind: Sample").append("\n");
        searchCriteria = SampleTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    if isinstance(entityObj, DataSet):
        txtBuilder.append("- Kind: DataSet").append("\n");
        searchCriteria = DataSetTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(entityObj.getType().getCode());
        fetchOptions = DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        results = v3.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);
        typeObj = results.getObjects().get(0);
    
    if not isinstance(entityObj, Project):
        txtBuilder.append("- Type: " + entityObj.getType().getCode()).append("\n");
    
    if(entityObj.getRegistrator() is not None):
        txtBuilder.append("- Registrator: ").append(entityObj.getRegistrator().getUserId()).append("\n");
        txtBuilder.append("- Registration Date: ").append(str(entityObj.getRegistrationDate())).append("\n");
    
    if entityObj.getModifier() is not None:
        txtBuilder.append("- Modifier: ").append(entityObj.getModifier().getUserId()).append("\n");
        txtBuilder.append("- Modification Date: ").append(str(entityObj.getModificationDate())).append("\n");
    
    
    if isinstance(entityObj, Project):
        description = entityObj.getDescription();
        if description is not None:
            txtBuilder.append("# Description:").append("\n");
            txtBuilder.append(entityObj.getDescription()).append("\n");
    
    if isinstance(entityObj, Sample) or isinstance(entityObj, DataSet):
        txtBuilder.append("# Parents:").append("\n");
        parents = entityObj.getParents();
        for parent in parents:
            relCodeName = parent.getCode();
            if "NAME" in parent.getProperties():
                relCodeName = relCodeName + " (" + parent.getProperties()["NAME"] + ")";
            txtBuilder.append("- ").append(relCodeName).append("\n");
        txtBuilder.append("# Children:").append("\n");
        children = entityObj.getChildren();
        for child in children:
            relCodeName = child.getCode();
            if "NAME" in child.getProperties():
                relCodeName = relCodeName + " (" + child.getProperties()["NAME"] + ")";
            txtBuilder.append("- ").append(relCodeName).append("\n");
    
    if not isinstance(entityObj, Project):
        txtBuilder.append("# Properties:").append("\n");
        propertyAssigments = typeObj.getPropertyAssignments();
        properties = entityObj.getProperties();
        for propertyAssigment in propertyAssigments:
            propertyType = propertyAssigment.getPropertyType();
            if propertyType.getCode() in properties:
                propertyValue = properties[propertyType.getCode()];
                if propertyValue != u"\uFFFD(undefined)":
                    if(propertyType.getDataType() == DataType.MULTILINE_VARCHAR and isRichText is False):
                        propertyValue = strip_tags(propertyValue).strip();
                    txtBuilder.append("- ").append(propertyType.getLabel()).append(": ").append(propertyValue).append("\n");
    return txtBuilder.toString();

def addFile(tempDirPath, entityFilePath, extension, fileContent, zos):
    entityFileNameWithExtension = entityFilePath + "." + extension
    entityFile = File(tempDirPath + entityFileNameWithExtension);
    entityFile.getParentFile().mkdirs();
    IOUtils.write(fileContent, FileOutputStream(entityFile));
    addToZipFile(entityFileNameWithExtension, entityFile, zos);
    FileUtils.forceDelete(entityFile);

    extensionToMimeType = {
        'json': 'application/json',
        'txt': 'text/plain',
        'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'html': 'text/html',
    }

    return {
        'fileName': entityFileNameWithExtension[1:],
        'mimeType': extensionToMimeType.get(extension, 'application/octet-stream')
    }

def getFilePath(spaceCode, projCode, expCode, sampCode, dataCode):
    fileName = "";
    if spaceCode is not None:
        fileName += "/" + spaceCode;
    if projCode is not None:
        fileName += "/" + projCode;
    if expCode is not None:
        fileName += "/" + expCode;
    if sampCode is not None:
        fileName += "/" + sampCode;
    if dataCode is not None:
        fileName += "/" + dataCode;
    return fileName;

def addToZipFile(path, file, zos):
    fis = FileInputStream(file);
    zipEntry = ZipEntry(path[1:]); # Making paths relative to make them compatible with Windows zip implementation
    zos.putNextEntry(zipEntry);

    bytes = jarray.zeros(1024, "b");
    length = fis.read(bytes);
    while length >= 0:
        zos.write(bytes, 0, length);
        length = fis.read(bytes);

    zos.closeEntry();
    fis.close();

def getConfigurationProperty(transaction, propertyName):
    threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties();
    try:
        return threadProperties.getProperty(propertyName);
    except:
        return None