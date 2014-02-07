#!/usr/bin/python
import settings
import systemtest.testcase
import time
import filecmp
import os

ARCHIVE_DIR="/tmp/integration-tests/archiving/rsync-archive"

class TestCase(systemtest.testcase.TestCase):
    
    
    def execute(self):
        print "execute"

        self.installOpenbis()
        self.installDatamover()
        
        openbisController = self.createOpenbisController()
        openbisController.createTestDatabase('openbis')
        
        openbisController.allUp()
        
        openbisController.dropAndWait("ARCHIVE_DATASET", 'incoming-jython')
        
        time.sleep(15)

        self.asserts_step1(openbisController)
        
    def asserts_step1(self, openbisController):
        self.assert_last_dataset_content_in_database(openbisController, "AVAILABLE", "t")
        
        archived = self.find_directory_in_archive()
        
        self.assert_the_same_content("Compare assert dataset content", openbisController.dataFile("ARCHIVE_DATASET"), archived)
        
    def assert_the_same_content(self, message, path1, path2):
        dc = filecmp.dircmp(path1,path2)
        if dc.left_only or  dc.right_only or dc.diff_files or dc.funny_files:
            dc.report_full_closure();
            self.fail(message)
            
    def find_directory_in_archive(self):
        matches = []
        for root, dirnames, filenames in os.walk(ARCHIVE_DIR):
            if "ARCHIVE_DATASET" in dirnames:
                return os.path.join(root, "ARCHIVE_DATASET")
        return None
        
    def assert_last_dataset_content_in_database(self, openbisController, status, archived):
        print("==== assert correct last dataset content in database with pattern %s, %s ====" % (status, archived))
    
        queryResult = openbisController.queryDatabase('openbis', 
            "select d.code, ed.status, ed.present_in_archive from data as d left join external_data as ed on ed.data_id = d.id where d.id = (select max(id) from data)")
        
        result = queryResult[0]
        
        self.assertEquals("Last dataset pattern %s" % result, status, result[1])
        self.assertEquals("Last dataset pattern %s" % result, archived, result[2])
    
    def executeInDevMode(self):
        print "execute dev mode"
        
        openbisController = self.createOpenbisController(dropDatabases = False)
        self.asserts_step1(openbisController)
        
TestCase(settings, __file__).runTest()