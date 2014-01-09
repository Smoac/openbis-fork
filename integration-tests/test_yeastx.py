#!/usr/bin/python
import settings
import systemtest.testcase

class TestCase(systemtest.testcase.TestCase):
    def execute(self):
        self.installOpenbis()
        openbisController = self.createOpenbisController()
        openbisController.dropDatabase("metabol")
        openbisController.dssProperties['gain-write-access-script'] = "%s/takeCifsOwnershipRecursive.sh" % openbisController.templatesFolder
        openbisController.dssProperties['experiment-name-property-code'] = 'name'
        openbisController.dssProperties['sample-name-property-code'] = 'samplename'
        openbisController.dssProperties['file-name-property-code'] = 'file_name'
        openbisController.dssProperties['data-set-file-name-entity-separator'] = '.'
        openbisController.dssProperties['metabol-database.kind'] = openbisController.databaseKind
        openbisController.allUp()

    def executeInDevMode(self):
        pass
        
TestCase(settings, __file__).runTest()
