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
        for name in os.listdir(self.getExpectedErrorLogsFolder(openbisController.testName)):
            if name.endswith('.txt'):
                self.dropInvalidData(openbisController, name[0:-4])
        self.dropInvalidData(openbisController, 'ignore-no-index', expectingEmptyData=True)
        self.dropInvalidData(openbisController, 'ignore-empty-dir', expectingEmptyData=True)
        openbisController.assertNumberOfDataSets(2, openbisController.getDataSets())
        self.dropDataSucessfully(openbisController, 'any-file-upload', 4, 6)
        self.dropDataSucessfully(openbisController, 'different-sample-mapping', 3, 9)
        self.dropDataSucessfully(openbisController, 'real-data-small', 4, 13)
        self.dropDataSucessfully(openbisController, 'sample-code-with-experiment', 1, 14)
        self.dropDataSucessfully(openbisController, 'upload-mzxml-to-db', 1, 15)
        self.dropDataSucessfully(openbisController, 'TEST&TEST_PROJECT&EXP_TEST.20090925182754736-36.eicML', 1, 16, 'incoming-eicml')
        self.dropDataSucessfully(openbisController, 'TEST&TEST_PROJECT&EXP_TEST.20090925182754736-36.fiaML', 1, 17, 'incoming-fiaml')
        self.dropDataSucessfully(openbisController, 'TEST&TEST_PROJECT&EXP_TEST.anyText123', 1, 18, 'incoming-quantml')
        self.assertNumberOfFiles(openbisController, 'eicml', 6)
        self.assertNumberOfFiles(openbisController, 'fiaml', 2)
        self.assertNumberOfRowsInMetabolDev(openbisController, 'eic_ms_runs', 2)
        self.assertNumberOfRowsInMetabolDev(openbisController, 'fia_ms_runs', 2)
        self.assertNumberOfRowsInMetabolDev(openbisController, 'mz_ms_runs', 1)
        self.assertNumberOfRowsInMetabolDev(openbisController, 'ms_quantifications', 1)
        self.assertNumberOfRowsInMetabolDev(openbisController, 'ms_quant_concentrations', 2)
        self.assertNumberOfRowsInMetabolDev(openbisController, 'ms_quant_compounds', 3)
        self.assertNumberOfRowsInMetabolDev(openbisController, 'mz_scans', 19, showContentIfFailed = False)
        result = util.executeCommand(['find', "%s/data/store/1" % openbisController.installPath, '-type', 'f'], suppressStdOut=True)
        if not self.assertEquals("number of files in store", 19, len(result)):
            util.printAndFlush("Files in store:")
            for line in result:
                util.printAndFlush(line)
        util.printAndFlush("Check data store code and experiment code of all data sets")
        dataSets = openbisController.getDataSets();
        for dataSet in dataSets:
            self.assertEquals("data store of data set %s" % dataSet.id, 'DSS1', dataSet.dataStore, verbose=False)
            self.assertEquals("experiment code of data set %s" % dataSet.id, 'EXP_TEST', dataSet.experimentCode, verbose=False)

    def executeInDevMode(self):
        openbisController = self.createOpenbisController(dropDatabases=False)
        openbisController.allUp()
        
    def assertNumberOfFiles(self, openbisController, dropboxType, expectedNumberOfFiles):
        count = 0
        drobboxFolder = "%s/data/dropbox-%s" % (openbisController.installPath, dropboxType)
        for f in os.listdir(drobboxFolder):
            if f.startswith('TEST&TEST_PROJECT&EXP_TEST.'):
                count += 1
        self.assertEquals("number of files in '%s'" % drobboxFolder, expectedNumberOfFiles, count)
        
    def dropDataSucessfully(self, openbisController, dataName, numberOfDataSets, totalNumberOfDataSets, dropboxName = 'incoming'):
        openbisController.dropAndWait(dataName, dropboxName, numberOfDataSets = numberOfDataSets)
        openbisController.assertEmptyFolder("data/%s" % dropboxName);
        openbisController.assertNumberOfDataSets(totalNumberOfDataSets, openbisController.getDataSets())
        
    def assertNumberOfRowsInMetabolDev(self, openbisController, tableName, expectedNumberOfRows, showContentIfFailed = True):
        result = openbisController.queryDatabase("metabol", "select * from %s" % tableName, showHeaders = True)
        if not self.assertEquals("number of rows in %s" % tableName, expectedNumberOfRows, len(result) - 2) and showContentIfFailed:
            util.printAndFlush("Actual content of table %s" % tableName);
            for row in result:
                util.printAndFlush(row)

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
                        if not self.assertEquals("%d. line of '%s'" % (i+1, dataName), expectedLine.strip(), line.strip()):
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
