#!/usr/bin/python

import settings
import systemtest.testcase
import systemtest.util as util

import hashlib
import os
import os.path
import tarfile
import time

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        self.installOpenbis()
        openbisController = self.createOpenbisController()
        os.makedirs("%s/data/archive/tmp" % openbisController.installPath)
        os.makedirs("%s/data/archive/stage" % openbisController.installPath)
        os.makedirs("%s/data/archive/final" % openbisController.installPath)
        openbisController.createTestDatabase("openbis")
        openbisController.allUp()
        
        openbisController.dropAndWait("data-example", "incoming-simple")
        openbisController.dropAndWait("data-example", "incoming-simple")
        
        self.waitUntilArchived(openbisController)
        self.checkArchiveAndMappingDatabase(openbisController)
    
    def executeInDevMode(self):
#        openbisController = self.createOpenbisController()
        openbisController = self.createOpenbisController(dropDatabases=False)
        
        self.checkArchiveAndMappingDatabase(openbisController)
        
    def checkArchiveAndMappingDatabase(self, openbisController):
        example_file = 'R_inferno.pdf'
        original_file = openbisController.dataFile("data-example/%s" % example_file)
        expected_md5 = self.calculateMD5(original_file)
        data_sets = openbisController.getDataSets()
        data_path = "%s/data" % openbisController.installPath
        container_path = None
        for data_set in data_sets:
            code = data_set.code
            self.assertEquals("Archiving status of data set %s" % code, 'ARCHIVED', data_set.status)
            self.assertEquals("Present in archive flag of data set %s" % code, True, True)
            path_in_store = "%s/store/1/%s" % (data_path, data_set.location)
            if os.path.exists(path_in_store):
                self.testCase.fail("Data set %s still in store: %s" % (code, path_in_store))
            else:
                util.printAndFlush("Data set %s not in store as expected." % code)
            resultSet = openbisController.queryDatabase("multi_dataset_archive", 
                                ("select path,size_in_bytes " + 
                                 "from data_sets ds join containers c on ds.ctnr_id = c.id " +
                                 "where code = '%s'" % code))
            if container_path is None:
                container_path = resultSet[0][0]
            else:
                self.assertEquals("Container path of data set %s" % code, container_path, resultSet[0][0])
            self.assertEquals("Size of data set %s" % code, '947802', resultSet[0][1])
            tar_file = "%s/archive/final/%s" % (data_path, container_path)
            path_in_tar = "%s/original/data-example/%s" % (code, example_file)
            extracted_file = self.extractFromArchive(openbisController, tar_file, path_in_tar)
            self.assertEquals("MD5 of %s" % path_in_tar, expected_md5, self.calculateMD5(extracted_file))
        
    def waitUntilArchived(self, openbisController):
        util.printAndFlush("Waiting for archiving finished.")
        monitor = util.LogMonitor("%s.DSS" % openbisController.instanceName, 
                                  "%s/servers/datastore_server/log/datastore_server_log.txt" % openbisController.installPath)
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.MultiDataSet'))
        monitor.waitUntilEvent(util.RegexCondition('changed status to'))
        
    def extractFromArchive(self, openbisController, tar_file, path_in_tar):
        temp_dir = "%s/data/tmp" % (openbisController.installPath)
        util.printAndFlush("Extract %s from archive %s" % (path_in_tar, tar_file))
        try:
            tf = tarfile.open(tar_file)
            tf.extract(path_in_tar, temp_dir)
        finally:
            tf.close()
        return "%s/%s" % (temp_dir, path_in_tar)
        
    def calculateMD5(self, file_path):
        md5 = hashlib.md5()
        with open(file_path, "rb") as f:
            buffer = f.read(65536)
            while len(buffer) > 0:
                md5.update(buffer)
                buffer = f.read(65536)
        return md5.hexdigest()
        
TestCase(settings, __file__).runTest()