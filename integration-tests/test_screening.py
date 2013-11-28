#!/usr/bin/python
import settings
import systemtest.testcase

class TestCase(systemtest.testcase.TestCase):
    def execute(self):
        openbisController = self.installOpenbis(technologies = ['screening'])
        openbisController.createTestDatabase('openbis', 'templates/openBIS-server-screening/test_database.sql')
        openbisController.allUp()
        
        openbisController.allDown()
        
TestCase(settings.REPOSITORY, __file__).runTest()

