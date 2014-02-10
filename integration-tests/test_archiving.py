#!/usr/bin/python
import settings
import systemtest.testcase
import time
import filecmp
import os
import shutil

ARCHIVE_DIR="/tmp/integration-tests/archiving/rsync-archive"


class TestCase(systemtest.testcase.TestCase):
    
    def execute(self):
        self.installOpenbis()
        self.installDatamover()
        
        self.cleanArchive()
        
        openbisController = self.createOpenbisController()
        openbisController.createTestDatabase("openbis")
        
        openbisController.allUp()
        
        openbisController.dropAndWait("ARCHIVE_DATASET", "incoming-jython")
        time.sleep(15)
        openbisController.dssDown()
        
        archived_data_set_dir = self.asserts_step1(openbisController)
        self.prepare_step_2(openbisController, archived_data_set_dir)
        
        openbisController.dssUp()
        time.sleep(15)
        
        self.asserts_step2( openbisController, archived_data_set_dir)
    
    def cleanArchive(self):
        shutil.rmtree(ARCHIVE_DIR, ignore_errors=True)
   
    def prepare_step_2(self, openbisController, archived_data_set_dir):
        self.damage_archive(archived_data_set_dir)
        self.reconfigureArchiver(openbisController)
        self.unset_presentInArchiveFlag_DB(openbisController)
    
    def unset_presentInArchiveFlag_DB(self, openbisController):
        openbisController.queryDatabase("openbis", "update external_data set present_in_archive=false");
    
    def damage_archive(self, archived_data_set_dir):
        print "Inserting invalid content in the archived dataset copy..."
        path_in_archive = os.path.join(archived_data_set_dir, "archive-me.txt")
        with open(path_in_archive, "a") as f:
            f.write("INVALID CONTENT AT THE END OF ARCHIVE")
        print "updated %s" % path_in_archive

    def reconfigureArchiver(self, openbisController):
        archiver_core_plugin_properties = "openbis/1/dss/maintenance-tasks/auto-archiver/plugin.properties"
        
        src = os.path.join(openbisController.dataFile("core-plugins"), archiver_core_plugin_properties)
        dst = os.path.join(openbisController.installPath, "servers", "core-plugins", archiver_core_plugin_properties)
        
        shutil.copyfile(src, dst)
        
    def asserts_step2(self, openbisController, archived_data_set_dir):
        self.assert_last_dataset_content_in_database(openbisController, "ARCHIVED", "t")
        
        self.assert_the_same_content("Compare assert dataset content", openbisController.dataFile("ARCHIVE_DATASET"), archived_data_set_dir)
        
        dataset_file = self.find_dataset_in_store(openbisController)
        
        if dataset_file is not None:
            self.fail("The dataset file should not be found in the store")
        

    def asserts_step1(self, openbisController):
        self.assert_last_dataset_content_in_database(openbisController, "AVAILABLE", "t")
        
        archived = self.find_directory_in_archive()
        self.assert_the_same_content("Compare assert dataset content", openbisController.dataFile("ARCHIVE_DATASET"), archived)
        
        return archived

    def assert_the_same_content(self, message, path1, path2):
        dc = filecmp.dircmp(path1,path2)
        if dc.left_only or  dc.right_only or dc.diff_files or dc.funny_files:
            dc.report_full_closure()
            self.fail(message)
            
    def find_directory_in_archive(self):
        return self.find_matching_file_in_directory_tree(ARCHIVE_DIR, "ARCHIVE_DATASET")
        
    def find_dataset_in_store(self, openbisController):
        return self.find_matching_file_in_directory_tree(openbisController.storeDirectory(), "ARCHIVE_DATASET")
    
    def find_matching_file_in_directory_tree(self, directory, file_name):
        matches = []
        for root, dirnames, filenames in os.walk(directory):
            if file_name in dirnames:
                return os.path.join(root, file_name)
        return None
    
    def assert_last_dataset_content_in_database(self, openbisController, status, archived):
        print("==== assert correct last dataset content in database with pattern %s, %s ====" % (status, archived))
    
        queryResult = openbisController.queryDatabase("openbis", 
            "select d.code, ed.status, ed.present_in_archive from data as d left join external_data as ed on ed.data_id = d.id where d.id = (select max(id) from data)")
        
        result = queryResult[0]
        
        self.assertEquals("Last dataset pattern %s" % result, status, result[1])
        self.assertEquals("Last dataset pattern %s" % result, archived, result[2])
    
    def executeInDevMode(self):
        print "execute dev mode"
        
        openbisController = self.createOpenbisController(dropDatabases = False)
        self.prepare_step_2(openbisController, archived_data_set_dir)
        
TestCase(settings, __file__).runTest()