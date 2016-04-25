#!/usr/bin/python
import os
import os.path

import settings
import systemtest.testcase
import systemtest.util as util
from systemtest.artifactrepository import GitHubArtifactReporistory

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        openbisController = self.setUpAndStartOpenbis()
        testDataFolder = self.getTestDataFolder(openbisController)
        for exampleName in sorted(os.listdir(testDataFolder)):
            if os.path.isdir("%s/%s" % (testDataFolder, exampleName)):
                self.dropTestExample(openbisController, testDataFolder, exampleName)

    def executeInDevMode(self):
        openbisController = self.setUpAndStartOpenbis()
#        openbisController = self.createOpenbisController(dropDatabases=False)
        testDataFolder = self.getTestDataFolder(openbisController)
#        self.dropTestExample(openbisController, testDataFolder, "ivmtcbeb087rjsmilcus17nu18")
        self.dropTestExample(openbisController, testDataFolder, "m9du561cidup7n0gdp97k8gh6u")
        for exampleName in sorted(os.listdir(testDataFolder)):
            if os.path.isdir("%s/%s" % (testDataFolder, exampleName)):
                break
            
    def setUpAndStartOpenbis(self):
        util.printWhoAmI()
        self.installOpenbis(technologies = ['screening'])
        openbisController = self.createOpenbisController(databasesToDrop=['openbis', 'pathinfo'])
        self.installMicroscopyPlugin(openbisController)
        corePluginsPropertiesFile = "%s/servers/core-plugins/core-plugins.properties" % openbisController.installPath
        corePluginsProperties = util.readProperties(corePluginsPropertiesFile)
        corePluginsProperties['disabled-core-plugins'] = 'screening:dropboxes, screening:initialize-master-data, ' \
            + 'screening:image-overview-plugins, screening:maintenance-tasks, screening:reporting-plugins, ' \
            + 'openbis:data-sources, openbis:services'
        util.writeProperties(corePluginsPropertiesFile, corePluginsProperties)
        openbisController.setDssMaxHeapSize("3g")
        openbisController.createTestDatabase("openbis")
        openbisController.allUp()
        return openbisController
    
    def installMicroscopyPlugin(self, openbisController):
        repository = GitHubArtifactReporistory(self.artifactRepository.localRepositoryFolder)
        path = repository.getPathToArtifact('aarpon/obit_microscopy_core_technology', 'master.zip')
        util.printAndFlush("path to core plugin in the repository: %s" % path)
        destination = "%s/servers/core-plugins/openbis/" % openbisController.installPath
        util.unzipSubfolder(path, 'obit_microscopy_core_technology-master/core-plugins/microscopy/1/', destination)
        
    def getTestDataFolder(self, openbisController):
        testDataFolder = "%s/../../test-data/integration_%s" % (self.playgroundFolder, self.name)
        if os.path.exists(testDataFolder):
            util.printAndFlush("Path exists as expected: %s" % testDataFolder)
        else:
            self.fail("Test data folder missing: %s" % testDataFolder)
        return testDataFolder
    
    def dropTestExample(self, openbisController, testDataFolder, exampleName):
        header = "\n/\\/\\/\\/\\/\\/\\ %s /\\/\\/\\/\\/\\/\\ %%s" % exampleName
        util.printWhoAmI(template = header)
        expectations = Expectations(openbisController, exampleName)
        destination = "%s/data/incoming-microscopy" % (openbisController.installPath)
        exampleFolder = "%s/%s" % (testDataFolder, exampleName)
        util.copyFromTo(testDataFolder, destination, exampleName)
        markerFile = "%s/.MARKER_is_finished_%s" % (destination, exampleName)
        open(markerFile, 'a').close()
        openbisController.waitUntilDataSetRegistrationFinished(expectations.numberOfPhysicalDataSets, timeOutInMinutes = 10)

class Expectations(object):
    def __init__(self, openbisController, exampleName):
        properties = util.readProperties(openbisController.dataFile("%s.properties" % exampleName))
        self.numberOfSeries = int(properties['number-of-series'])
        numberOfAdditinalDataSets = int(properties['number-of-additional-data-sets'])
        self.numberOfPhysicalDataSets = self.numberOfSeries + 1 + numberOfAdditinalDataSets
        self.numberOfDataSets = self.numberOfPhysicalDataSets + self.numberOfSeries


    
TestCase(settings, __file__).runTest()
