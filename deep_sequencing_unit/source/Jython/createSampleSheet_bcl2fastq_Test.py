import unittest
import re 
from createSampleSheet_bcl2fastq import *

class test_sanitize_string(unittest.TestCase):


    def testDefault(self):
        self.assertEqual(sanitize_string('abc#a$v%c^D&P-'), 'abc_a_v_c_D_P_')


    def testOnlySpecialChars(self):         
        self.assertEqual(sanitize_string('@#$%^&*('), '_')


class test_get_model(unittest.TestCase):
    
    def test_HiseqX(self):
        self.assertEqual(get_model('141121_ST-E00107_0356_AH00C3CCXX'), Sequencers.HISEQ_X)
    
    def test_expectError(self):
        self.assertNotEqual(get_model('150724_J00121_0017_AH2VYMBBXX'), Sequencers.NEXTSEQ_500)


class test_get_reverse_complement(unittest.TestCase):
    def test_happyCase(self):
        self.assertEqual(get_reverse_complement('ACTGAATTTT'), 'AAAATTCAGT', 'Reverse complement is faulty')
        
    def test_failingCase(self):
        self.assertNotEqual(get_reverse_complement('ACTG'), 'CAGA')


class test_get_flowCell(unittest.TestCase):
  
    def setUp(self):
        self.myCode = 'C7GMNANXX'
        self.logger = setUpLogger('log/')
        configDict = readConfig(self.logger)
        self.service = OpenbisServiceFacadeFactory.tryCreate(configDict['openbisUserName'],
                                                      configDict['openbisPassword'],
                                                      configDict['openbisServer'],
                                                      configDict['connectionTimeout'])
        
        self.flowcell, self.containedSamples = get_flowcell('ILLUMINA_FLOW_CELL',
                                            self.myCode, self.service, self.logger)


    def test_get_flowCell (self):

        self.assertEqual(self.flowcell.getCode(), self.myCode)
        self.assertEqual(self.containedSamples.size(), 8)
        
        fcProp = self.flowcell.getProperties()
        self.assertEqual(fcProp['SEQUENCER'], 'D00535')
        self.flowCellDict = transform_sample_to_dict(self.flowcell)
        self.assertEqual(self.flowCellDict['FLOWCELLTYPE'], 'HiSeq Flow Cell v4')

    
    def test_get_contained_sample_properties(self):
        self.parentDict, self.samplesPerLaneDict = get_contained_sample_properties(
                                                    self.containedSamples, self.service)
        self.assertEqual(self.parentDict['BSSE_QGF_34778_C7GMNANXX_1']['BARCODE'], 'GTCCGC')
        self.assertEqual(self.parentDict['BSSE_QGF_32285_C7GMNANXX_7']['CONTACT_PERSON_EMAIL'], 'yann.bourgeois@unibas.ch')
        self.assertEqual(self.samplesPerLaneDict['2'], 23)

    
    def tearDown(self):
        self.service.logout()
        self.logger.info('Logged out') 


# class test_get_contained_sample_properties(unittest.TestCase):
#     def setUp(self):
#         self.flowcell = test_get_flowCell.setUp(self)
#         





# class test_verify_index_length(test_get_flowCell):
#     
#     def setUp(self):
#         foundFlowCell, containedSamples = test_get_flowCell()
#     
#     
#     def test_verify_index_length(self):
#         foundFlowCell

def main():
    unittest.main()

if __name__ == '__main__':
    main()