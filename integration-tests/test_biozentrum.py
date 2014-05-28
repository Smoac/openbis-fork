#!/usr/bin/python
import os

import settings
import systemtest.testcase
import systemtest.util as util

NOTIFICATION_TEMPLATE = "storage_provider.storage.status = STORAGE_SUCCESSFUL;storage_provider.dataset.id = ${data-set-code};ibrain2.dataset.id = ${property:ibrain2.dataset.id}"

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        self.installOpenbis(technologies = ['screening'])
        openbisController = self.createOpenbisController()
        openbisController.dssProperties['scripts-dir'] = "../../../../../../%s/scripts" % openbisController.templatesFolder
        post_registration_tasks = openbisController.dssProperties['post-registration.post-registration-tasks'] 
        openbisController.dssProperties['post-registration.post-registration-tasks'] = "%s, eager-shuffling, notifying" % post_registration_tasks
        openbisController.dssProperties['post-registration.eager-shuffling.class'] = "ch.systemsx.cisd.etlserver.postregistration.EagerShufflingTask"
        openbisController.dssProperties['post-registration.eager-shuffling.data-store-server-code'] = "${data-store-server-code}"
        openbisController.dssProperties['post-registration.eager-shuffling.storeroot-dir'] = "${storeroot-dir}"
        openbisController.dssProperties['post-registration.eager-shuffling.share-finder.class'] = "ch.systemsx.cisd.etlserver.postregistration.SimpleShareFinder"
        openbisController.dssProperties['post-registration.notifying.class'] = "ch.systemsx.cisd.etlserver.postregistration.NotifyingTask"
        openbisController.dssProperties['post-registration.notifying.message-template'] = NOTIFICATION_TEMPLATE
        openbisController.dssProperties['post-registration.notifying.destination-path-template'] = '${root-dir}/../registration-status/ibrain2_dataset_id2_${property:ibrain2.dataset.id}.properties'
        openbisController.createTestDatabase('openbis')
        registrationStatusFolder = "%s/registration-status" % openbisController.installPath
        os.makedirs(registrationStatusFolder)
        openbisController.allUp()
        # Register image data set
        self.dropWaitAndCheck(openbisController, 32, 'incoming-hcs-image-raw', 
                              'HCS_IMAGE_CONTAINER_RAW', numberOfDataSets = 2)
        # Register derived data sets
        parentDataSet = self.getImageDataSetCode(openbisController, registrationStatusFolder)
        self.dropWaitAndCheck(openbisController, 99, 'incoming-hcs-image-segmentation', 
                              'HCS_IMAGE_CONTAINER_SEGMENTATION', parentDataSet, 2)
        self.dropWaitAndCheck(openbisController, 77, 'incoming-hcs-analysis-well-results-summaries', 
                              'HCS_ANALYSIS_CONTAINER_WELL_RESULTS_SUMMARIES', parentDataSet)
        openbisController.assertFeatureVectorLabel('INTERPHASEINVASOMEINFECTION_INDEX', 'InterphaseInvasomeInfection_Index')
        openbisController.assertFeatureVectorLabel('COUNT_BACTERIA', 'Count_Bacteria')
        self.dropWaitAndCheck(openbisController, 47, 'incoming-hcs-analysis-well-quality-summary', 
                              'HCS_ANALYSIS_CONTAINER_WELL_QUALITY_SUMMARY', parentDataSet)
        openbisController.assertFeatureVectorLabel('FOCUS_SCORE', 'Focus_Score')
        self.dropWaitAndCheck(openbisController, 58, 'incoming-hcs-analysis-cell-features-cc-mat', 
                              'HCS_ANALYSIS_CELL_FEATURES_CC_MAT', parentDataSet)
        openbisController.assertNumberOfDataSets(7, openbisController.getDataSets())
        
    def executeInDevMode(self):
        openbisController = self.createOpenbisController(dropDatabases=False)
        openbisController.allUp()

    def dropWaitAndCheck(self, openbisController, id, dropBoxName, expectedDataSetType, parentDataSet = None, numberOfDataSets = 1):
        dataName = "ibrain2_dataset_id_%s" % id
        openbisController.drop(dataName, dropBoxName)
        if parentDataSet is not None:
            with open("%s/data/%s/%s/metadata.properties" % (openbisController.installPath, dropBoxName, dataName), "a") as f:
                f.write("storage_provider.parent.dataset.id = %s\n" % parentDataSet)
        markerFile = "%s/data/%s/.MARKER_is_finished_%s" % (openbisController.installPath, dropBoxName, dataName)
        open(markerFile, 'a').close()
        openbisController.waitUntilDataSetRegistrationFinished(numberOfDataSets = numberOfDataSets)
        statusProps = util.readProperties("%s/registration-status/%s.properties" % (openbisController.installPath, dataName))
        self.assertPropertyValue('STORAGE_SUCCESSFUL', statusProps, 'storage_provider.storage.status')
        self.assertPropertyValue(str(id), statusProps, 'ibrain2.dataset.id')
        dataSetCode = statusProps['storage_provider.dataset.id']
        data = openbisController.queryDatabase('openbis', 
                    "select t.code from data join data_set_types as t on data.dsty_id = t.id where data.code = '%s'" % dataSetCode)
        self.assertEquals("Type of data set %s" % dataSetCode, [[expectedDataSetType]], data)
        dropBoxFolder = "%s/data/%s" % (openbisController.installPath, dropBoxName)
        self.assertEquals("Files in '%s'" % dropBoxFolder, '[]', str(os.listdir(dropBoxFolder)))
        
    def assertPropertyValue(self, expectedValue, properties, key):
        self.assertEquals("Value of '%s'" % key, expectedValue, properties[key])

    def getImageDataSetCode(self, openbisController, registrationStatusFolder):
        registrationStatusFolder = "%s/registration-status" % openbisController.installPath
        registrationProps = util.readProperties("%s/ibrain2_dataset_id_32.properties" % registrationStatusFolder)
        parentDataSetKey = 'storage_provider.dataset.id'
        parentDataSet = registrationProps[parentDataSetKey]
        if parentDataSet is None:
            raise Exception("Undefined property '%s' in '%s':\n%s" % (parentDataSet, registrationStatusFolder, registrationProps))
        util.printAndFlush("Parent data set code: %s" % parentDataSet)
        return parentDataSet
    
TestCase(settings, __file__).runTest()
