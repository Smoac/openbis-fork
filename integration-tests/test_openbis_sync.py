#!/usr/bin/python
import os
import shutil
import settings
import systemtest.testcase
import systemtest.util as util
from systemtest.testcase import TEST_DATA

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        self.installOpenbis(instanceName ='openbis1', technologies = ['screening'])
        openbis_datasource = self.createOpenbisController('openbis1')
        openbis_datasource.setDummyAuthentication()
        openbis_datasource.setDataStoreServerUsername('etlserver')
        openbis_datasource.createTestDatabase('openbis')
        openbis_datasource.createTestDatabase('pathinfo')
        openbis_datasource.createTestDatabase('imaging')
        

    def executeInDevMode(self):
        openbis_datasource = self.createOpenbisController(instanceName = 'openbis1', dropDatabases=False)
        openbis_datasource.allDown()
        openbis_datasource.setDataStoreServerUsername('etlserver')
        openbis_datasource.allUp()
        client = self.installScreeningTestClient()
        client.run()

TestCase(settings, __file__).runTest()