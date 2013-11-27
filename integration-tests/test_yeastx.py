#!/usr/bin/python
import settings
import systemtest.testcase

class TestCase(systemtest.testcase.TestCase):
    def execute(self):
        self.installOpenbis()
        
TestCase(settings.REPOSITORY, __file__).runTest()

