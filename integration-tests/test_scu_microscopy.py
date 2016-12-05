#!/usr/bin/python
import os
import os.path
import re
import time
from time import mktime

import settings
import systemtest.testcase
import systemtest.util as util
from systemtest.artifactrepository import GitArtifactRepository

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
        self.dropTestExample(openbisController, testDataFolder, "cj8p7jgmqlvv8g12ng1it8a5os")
#        self.dropTestExample(openbisController, testDataFolder, "m9du561cidup7n0gdp97k8gh6u")
#       self.dropTestExample(openbisController, testDataFolder, "okn3scc5tmhk199m6qk80d20op")
        for exampleName in sorted(os.listdir(testDataFolder)):
            if os.path.isdir("%s/%s" % (testDataFolder, exampleName)):
                break
            
    def setUpAndStartOpenbis(self):
        util.printWhoAmI()
        self.installOpenbis(technologies = ['microscopy'])
        self.setThumbnailResolutions(openbisController, ['256x256'])
        openbisController = self.createOpenbisController(databasesToDrop=['openbis', 'pathinfo'])
        openbisController.setDssMaxHeapSize("3g")
        openbisController.createTestDatabase("openbis")
        openbisController.allUp()
        return openbisController

    def setThumbnailResolutions(self, openbisController, resolutions):
        path = "%s/servers/core-plugins/microscopy/1/dss/drop-boxes/MicroscopyDropbox/GlobalSettings.py" % openbisController.installPath
        with open(path, "r")  as f:
            content = f.readlines()
        with open(path, "w") as f:
            for line in content:
                if line.find('ImageResolutions') > 0:
                    line = line.replace('[]', str(resolutions))
                f.write("%s" % line)

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
        openbisController.waitUntilDataSetRegistrationFinished(expectations.numberOfPhysicalDataSets, 
                                                               timeOutInMinutes = expectations.registrationTimeout)
        logInfo = self.getLogInfo(openbisController, exampleName)
        self.assertEquals("Number of experiments created", expectations.numberOfExperiments, logInfo.numberOfExperiments)
        self.assertEquals("Number of samples created", expectations.numberOfSamples, logInfo.numberOfSamples)
        self.assertEquals("Number of data sets created", expectations.numberOfDataSets, logInfo.numberOfDataSets)
        self.assertSmaller("Thumbnail generation time", expectations.thumbnailGenerationTime, logInfo.thumbnailTime)
        self.assertSmaller("Registration time", expectations.registrationTime, logInfo.jobTime)
            
    def getLogInfo(self, openbisController, exampleName):
        logFolder = "%s/servers/datastore_server/log-registrations/succeeded" % openbisController.installPath
        logFile = self.getLogFile(logFolder, exampleName)
        logInfo = LogInfo(exampleName)
        with open(logFile) as fid:
            content = fid.readlines()
            initial_time = mktime(time.strptime(content[0][0:19], '%Y-%m-%d %H:%M:%S'))
            last_time = mktime(time.strptime(content[-1][0:19], '%Y-%m-%d %H:%M:%S'))
            logInfo.jobTime = last_time - initial_time
            for line in content:
                if "Experiments created:" in line:
                    logInfo.numberOfExperiments = self.getNumFromString(line)
                if "Samples created:" in line:
                    logInfo.numberOfSamples = self.getNumFromString(line)
                if "Data sets created:" in line:
                    logInfo.numberOfDataSets = self.getNumFromString(line)
                if "Start registration" in line:
                    initial_thumb_time = mktime(time.strptime(line[0:19], '%Y-%m-%d %H:%M:%S'))
                if "Prepared registration" in line:
                    last_thumb_time = mktime(time.strptime(line[0:19], '%Y-%m-%d %H:%M:%S'))
            
            logInfo.thumbnailTime = last_thumb_time - initial_thumb_time
        util.printAndFlush(("Registration took %s seconds (%s seconds for thumbnail creation) " 
                           + "to create %s experiments, %s samples, %s data sets") 
                           % (logInfo.jobTime, logInfo.thumbnailTime, logInfo.numberOfExperiments, 
                              logInfo.numberOfSamples, logInfo.numberOfDataSets))
        return logInfo

    def getNumFromString(self, string):
        """Get the number at the end of a string."""
    
        m = re.search(r'\d+$', string)
        if m is not None:
            return int(m.group())
        else:
            return 0
    
    def getLogFile(self, logFolder, exampleName):
        for f in os.listdir(logFolder):
            if f.find(exampleName) > 0:
                return "%s/%s" % (logFolder, f)
        raise Exception("No log file for example %s found in %s." % (exampleName, logFolder))

        

class Expectations(object):
    def __init__(self, openbisController, exampleName):
        properties = util.readProperties(openbisController.dataFile("%s.properties" % exampleName))
        self.numberOfSeries = int(properties['number-of-series'])
        numberOfAdditinalDataSets = int(properties['number-of-additional-data-sets'])
        self.numberOfPhysicalDataSets = self.numberOfSeries + 1 + numberOfAdditinalDataSets
        self.numberOfDataSets = self.numberOfPhysicalDataSets + self.numberOfSeries
        self.numberOfExperiments = int(properties['number-of-experiments'])
        self.numberOfSamples = int(properties['number-of-samples'])
        self.registrationTimeout = int(properties['registration-timeout-in-minutes']);
        self.thumbnailGenerationTime = int(properties['thumbnail-generation-time-in-seconds'])
        self.registrationTime = int(properties['registration-time-in-seconds'])

class LogInfo(object):
    def __init__(self, exampleName):
        self.exampleName = exampleName
        self.numberOfExperiments = 0
        self.numberOfSamples = 0
        self.numberOfDataSets = 0
        self.jobTime = 0
        self.thumbnailTime = 0

    
TestCase(settings, __file__).runTest()
