#!/usr/bin/python
import settings
import systemtest.testcase

class TestCase(systemtest.testcase.TestCase):
    def execute(self):
        openbisController = self.installOpenbis(technologies = ['screening'])
        print openbisController.asProperties
        
TestCase(settings.REPOSITORY, __file__).runTest()

