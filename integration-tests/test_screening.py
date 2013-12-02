#!/usr/bin/python
import settings
import systemtest.testcase

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        """
        openbisController = self.installOpenbis(technologies = ['screening'])
        openbisController.createTestDatabase('openbis')
        openbisController.allUp()
        openbisController.drop('data-incoming-analysis.zip', 'incoming-analysis')
        self.assertEquals('spot sizes', [['24', '16']], self.getSpotSizes(openbisController))
        openbisController.drop('data-incoming-images-merged-channels.zip', 'incoming-images-merged-channels')
        openbisController.drop('data-incoming-images-split-channels.zip', 'incoming-images-split-channels')
#        """
        openbisController = self.createOpenbisController()
        self.assertEquals('spot sizes', [['24', '16']], self.getSpotSizes(openbisController))
#        openbisController.allDown()
        
    def getSpotSizes(self, openbisController):
        return openbisController.queryDatabase('imaging', 
                            'select spots_width,spots_height from containers order by spots_width')

TestCase(settings, __file__).runTest()

