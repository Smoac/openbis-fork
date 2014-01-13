#!/usr/bin/python
import os.path

import settings
import systemtest.testcase
import systemtest.util as util


class TestCase(systemtest.testcase.TestCase):
    def execute(self):
        self.installOpenbis()
        openbisController = self.createOpenbisController()
        openbisController.createTestDatabase('openbis')
        openbisController.createFolder('data/dropbox-eicml')
        openbisController.createFolder('data/dropbox-fiaml')
        openbisController.dropDatabase("metabol")
        openbisController.dssProperties['gain-write-access-script'] \
            = "../../../../../../%s/takeCifsOwnershipRecursive.sh" % openbisController.templatesFolder
        openbisController.dssProperties['experiment-name-property-code'] = 'name'
        openbisController.dssProperties['sample-name-property-code'] = 'samplename'
        openbisController.dssProperties['file-name-property-code'] = 'file_name'
        openbisController.dssProperties['data-set-file-name-entity-separator'] = '.'
        openbisController.dssProperties['metabol-database.kind'] = openbisController.databaseKind
        openbisController.allUp()
        self.dropDataSucessfully(openbisController, 'any-file-upload', 4)
        self.dropDataSucessfully(openbisController, 'different-sample-mapping', 3)
        for name in os.listdir(self.getExpectedErrorLogsFolder(openbisController.testName)):
            dataName = name[0:-4]
            self.dropInvalidData(openbisController, dataName)
        self.dropInvalidData(openbisController, 'ignore-empty-dir', expectingEmptyData=True)
        self.dropInvalidData(openbisController, 'ignore-no-index', expectingEmptyData=True)


    def executeInDevMode(self):
        openbisController = self.createOpenbisController(dropDatabases=False)
        openbisController.allUp()
            
    def dropDataSucessfully(self, openbisController, dataName, numberOfDataSets):
        openbisController.dropAndWait(dataName, 'incoming', numberOfDataSets = 3)
        openbisController.assertEmptyFolder('data/incoming');
        openbisController.assertNumberOfDataSets(numberOfDataSets, openbisController.getDataSets())
        

    def dropInvalidData(self, openbisController, dataName, expectingEmptyData = False):
        openbisController.drop(dataName, 'incoming')
        openbisController.waitUntilConditionMatched(util.RegexCondition("File %s not written to faulty paths" % dataName))
        dataFolder = "%s/data/incoming/%s" % (openbisController.installPath, dataName)
        logFile = "%s/error-log.txt" % dataFolder
        try:
            if expectingEmptyData:
                if os.path.exists("%s/_delete_me_after_correcting_errors" % dataFolder):
                    self.fail("Not expecting marker file '_delete_me_after_correcting_errors' in %s." % dataFolder)
                if os.path.exists(logFile):
                    self.fail("Not expecting error log file: %s" % logFile)
            else:
                if not os.path.exists("%s/_delete_me_after_correcting_errors" % dataFolder):
                    self.fail("Expected marker file '_delete_me_after_correcting_errors' doesn't exists in %s." % dataFolder)
                if not os.path.exists(logFile):
                    self.fail("Expected error log doesn't exist: %s" % logFile)
                else:
                    lines = self.getErrorLog(logFile)
                    expectedLines = self.getExpectedLogEntries(openbisController.testName, dataName)
                    failed = False
                    for i, line, expectedLine in zip(range(len(lines)), lines, expectedLines):
                        if not self.assertEquals("%d. line of '%s'" % (i+1, dataName), expectedLine.rstrip(), line.rstrip()):
                            self.printLogEntries(lines)
                            failed = True
                            break
                    if not failed:
                        if not self.assertEquals("number of log entries", len(expectedLines), len(lines)):
                            self.printLogEntries(lines)
                    else:
                        util.printAndFlush("Log file %s contains expected messages" % dataFolder)
        finally:
            util.deleteFolder(dataFolder)
                
    def printLogEntries(self, lines):
        util.printAndFlush("All log entries:")
        for line in lines:
            util.printAndFlush(line.rstrip())

    def doesContainsMessage(self, lines, expectedErrorMessage):
        for line in lines:
            if expectedErrorMessage in line:
                return True
        return False
                
    def getErrorLog(self, logFile):
        lines = []
        with open(logFile, 'r') as handle:
            result = []
            for line in handle:
                if 'WARNING' not in line:
                    result.append(line[29:] if "ERROR" in line else line)
            return result
        
    def getExpectedLogEntries(self, testName, dataName):
        f = "%s/%s.txt" % (self.getExpectedErrorLogsFolder(testName), dataName)
        with open(f, 'r') as handle:
            return handle.readlines()
        
    def getExpectedErrorLogsFolder(self, testName):
        return "%s/%s/expected-error-logs" % (systemtest.testcase.TEMPLATES, testName)
TestCase(settings, __file__).runTest()
