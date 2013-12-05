#!/usr/bin/python
import os
import shutil
import settings
import systemtest.testcase
import systemtest.util as util

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        self.installOpenbis('openbis1')
        self.cloneOpenbisInstance('openbis1', 'openbis2', dataStoreServerOnly=True)
        openbis1 = self.createOpenbisController('openbis1')
        openbis1.setDummyAuthentication()
        openbis1.setDataStoreServerUsername('etlserver1')
        openbis1.createTestDatabase('openbis')
        openbis2 = self.createOpenbisController('openbis2')
        openbis2.setDataStoreServerCode('DSS2')
        openbis2.setDataStoreServerPort('8445')
        openbis2.setDataStoreServerUsername('etlserver2')
        openbis1.allUp()
        openbis2.dssUp()
        datamover1 = self.installDatamover("datamover1")
        datamover1.setPrefixForIncoming('microX_200801011213_')
        datamover1.setTreatIncomingAsRemote(True)
        datamover1.setOutgoingTarget('../openbis1/data/incoming-raw')
        datamover1.setExtraCopyDir('data/extra_local_copy')
        datamover2 = self.installDatamover("datamover2")
        datamover2.setOutgoingTarget('../openbis1/data/incoming-analysis')
        os.makedirs("%s/data/drop-box1" % openbis2.installPath)
        os.makedirs("%s/data/drop-box2" % openbis2.installPath)
        dummyImageAnalyser = self.installScriptBasedServer('dummy-img-analyser', 'dummy-img-analyser')
        datamover1.start()
        datamover2.start()
        dummyImageAnalyser.start()
        
        datamover1.drop("3VCP1")
        openbis1.waitUntilDataSetRegistrationFinished(2)
        
        datamover1.drop("3VCP1")
        openbis1.waitUntilDataSetRegistrationFailed()
        
        datamover1.drop("3VCP3")
        openbis1.waitUntilDataSetRegistrationFinished(3)
        
        datamover1.drop("UnknownPlate")
        openbis1.waitUntilDataSetRegistrationFailed()
        
        with open("%s/data/incoming/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3.txt" % openbis2.installPath, "w") as f:
            f.write("hello world")
        openbis2.waitUntilDataSetRegistrationFinished()
        
        
    def executeInDevMode(self):
        openbis1 = self.createOpenbisController(instanceName='openbis1', dropDatabases=False)
        openbis2 = self.createOpenbisController(instanceName='openbis2', dropDatabases=False)
        datamover1 = self.createDatamoverController("datamover1")
        datamover1.setPrefixForIncoming('microX_200801011213_')
        datamover1.setTreatIncomingAsRemote(True)
        datamover1.setOutgoingTarget('../openbis1/data/incoming-raw')
        datamover1.setExtraCopyDir('data/extra_local_copy')
        datamover2 = self.createDatamoverController("datamover2")
        datamover2.setOutgoingTarget('../openbis1/data/incoming-analysis')
        dummyImageAnalyser = self.createScriptBasedServerController('dummy-img-analyser')
        """
        openbis1.allUp()
        openbis2.dssUp()
        datamover1.start()
        datamover2.start()
        dummyImageAnalyser.start()
        """
        openbis1.assertNumberOfDataSets(6)
        openbis2.assertNumberOfDataSets(1)

        
TestCase(settings, __file__).runTest()