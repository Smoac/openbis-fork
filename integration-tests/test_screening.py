#!/usr/bin/python
import settings
import systemtest.testcase

class TestCase(systemtest.testcase.TestCase):
    
    def execute(self):
        self.installOpenbis(technologies = ['screening'])
        openbisController = self.createOpenbisController()
        openbisController.createTestDatabase('openbis')
        openbisController.allUp()
        openbisController.drop('data-incoming-analysis.zip', 'incoming-analysis')
        self.assertSpotSizes(openbisController, [['24', '16']])
        openbisController.drop('data-incoming-images-merged-channels.zip', 'incoming-images-merged-channels')
        openbisController.drop('data-incoming-images-split-channels.zip', 'incoming-images-split-channels')
        openbisController.assertEmptyFolder('data/incoming-analysis');
        openbisController.assertEmptyFolder('data/incoming-images-merged-channels');
        openbisController.assertEmptyFolder('data/incoming-images-split-channels');
        openbisController.assertNumberOfDataSets(3, openbisController.getDataSets())
        self.assertSpotSizes(openbisController, [['24', '16'], ['24', '16']])
        self.assertFeatureVectorLabel(openbisController, 'HITRATE', 'Hit Rate')
        self.assertFeatureVectorLabel(openbisController, 'CELLNUMBER', 'cellNumber')
        client = self.installScreeningTestClient()
        log = '\n'.join(client.run())
        self.assertPatternInLog(log, "Experiments: \[/DEMO/DEMO_PROJECT/DEMO_EXPERIMENT \[20100623121102843-1\]\]")
        self.assertPatternInLog(log, "Plates: \[/DEMO/PLATE1 \[20100624113752213-5\]")
        self.assertPatternInLog(log, "Image datasets: \[[0-9]*-[0-9]* \(plate: /DEMO/PLATE3")
        self.assertPatternInLog(log, "Feature vector datasets: \[[0-9]*-[0-9]* \(plate: /DEMO/PLATE2 \[20100624113756254-6\]")
        self.assertPatternInLog(log, "Feature codes: \[CELLNUMBER, FEATRUE1, FEATRUE10, FEATRUE11, FEATRUE12, FEATRUE13, "
                                     + "FEATRUE14, FEATRUE15, FEATRUE16, FEATRUE2, FEATRUE3, FEATRUE4, FEATRUE5, FEATRUE6, "
                                     + "FEATRUE7, FEATRUE8, FEATRUE9, HITRATE, STD1, STD10, STD11, STD12, STD13, "
                                     + "STD14, STD15, STD16, STD2, STD3, STD4, STD5, STD6, STD7, STD8, STD9\]")
        self.assertPatternInLog(log, "Loaded feature datasets: 1")
        self.assertPatternInLog(log, "features labels: \[cellNumber, featrue1, featrue10, featrue11, featrue12, "
                                     + "featrue13, featrue14, featrue15, featrue16, featrue2, featrue3, featrue4, "
                                     + "featrue5, featrue6, featrue7, featrue8, featrue9, Hit Rate, std1, std10, "
                                     + "std11, std12, std13, std14, std15, std16, std2, std3, std4, std5, std6, "
                                     + "std7, std8, std9\]")
        self.assertPatternInLog(log, "Features of the first dataset: datasetCode: [0-9]*-[0-9]*")
        self.assertPatternInLog(log, "wellPosition: \[1, 2\], values: \[48.0, 0.0051865")
        self.assertPatternInLog(log, "Image metadata: \[Dataset [0-9]*-[0-9]* \(plate: /DEMO/PLATE3 "
                                     + "\[20100624113759640-7\]\) has \[\[DAPI, GFP\]\] channels, 9 tiles\. "
                                     + "Image resolution: 720x468")
        openbisController.drop('PLATE1', 'incoming-hcs', numberOfDataSets = 3)
        openbisController.assertNumberOfDataSets(6, openbisController.getDataSets())
        self.assertSpotSizes(openbisController, [['24', '16']]*3)
    
    def executeInDevMode(self):
        openbisController = self.createOpenbisController(dropDatabases = False)
#        openbisController.allUp()
#        openbisController.drop('PLATE1', 'incoming-hcs', numberOfDataSets = 3)
        openbisController.assertNumberOfDataSets(6, openbisController.getDataSets())
        self.assertSpotSizes(openbisController, [['24', '16']]*3)
        
    def assertSpotSizes(self, openbisController, expected):
        actual = openbisController.queryDatabase('imaging', 
                            'select spots_width,spots_height from containers order by spots_width')
        self.assertEquals('spot sizes', expected, actual)
        
    def assertFeatureVectorLabel(self, openbisController, featureCode, expectedFeatureLabel):
        data = openbisController.queryDatabase('imaging', 
                                               "select distinct label from feature_defs where code = '%s'" % featureCode);
        self.assertEquals("label of feature %s" % featureCode, [[expectedFeatureLabel]], data)

TestCase(settings, __file__).runTest()

