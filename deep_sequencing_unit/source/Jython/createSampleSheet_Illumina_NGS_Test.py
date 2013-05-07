import unittest
import re 
from createSampleSheet_Illumina_NGS import *

class TestsanitizeString(unittest.TestCase):

  def testDefault(self):
    self.assertEqual(sanitizeString('abc#a$v%c^D&P-'), 'abc_a_v_c_D_P_')

  def testOnlySpecialChars(self):         
    self.assertEqual(sanitizeString('@#$%^&*('), '_')


class TestGetFlowCell(unittest.TestCase):
  
  def setUp(self):
    self.logger = setUpLogger('log/')
    configDict = readConfig(self.logger)
    self.service = OpenbisServiceFacadeFactory.tryCreate(configDict['openbisUserName'],
                                                  configDict['openbisPassword'],
                                                  configDict['openbisServer'],
                                                  configDict['connectionTimeout'])
    print('Ran SetUp...')

  
  def testGetFlowCell (self):

    myCode = 'D1W0VACXX'
    foundFlowCell, containedSamples = getFlowCell ('ILLUMINA_FLOW_CELL', myCode, self.service, self.logger)
    
    self.assertEqual(foundFlowCell.getCode(), myCode)
    self.assertEqual(containedSamples.size(), 8)
    
    fcProp = foundFlowCell.getProperties()
    self.assertEqual(fcProp['SEQUENCER'], 'SN792')
    
  def tearDown(self):
    self.service.logout()
    self.logger.info('Logged out') 

def main():
    unittest.main()

if __name__ == '__main__':
    main()