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


class create_sample_sheet(unittest.TestCase):
  
    def setUp(self):
        self.myCode = 'C7GMNANXX'
        self.logger = setUpLogger('log/')
        self.config_dict = readConfig(self.logger)
        
#         self.options = parseOptions(self.logger)
        
        import argparse
        import shlex
        parser = argparse.ArgumentParser()
        
        parser.add_argument('--flowcell')
        parser.add_argument('--lineending')
        parser.add_argument('--outdir')
        
        cmd_string = ['--flowcell', self.myCode, '--lineending', 'win32', '--outdir', '../../targets/playground']
        self.options = parser.parse_args(cmd_string)

        self.service = OpenbisServiceFacadeFactory.tryCreate(self.config_dict['openbisUserName'],
                                                      self.config_dict['openbisPassword'],
                                                      self.config_dict['openbisServer'],
                                                      self.config_dict['connectionTimeout'])
        
        self.flowcell, self.containedSamples = get_flowcell('ILLUMINA_FLOW_CELL',
                                            self.myCode, self.service, self.logger)
        self.flowCellDict = transform_sample_to_dict(self.flowcell)
        self.parentDict, self.samplesPerLaneDict = get_contained_sample_properties(
                                                    self.containedSamples, self.service)
        self.flowCellName = self.flowcell.getCode()
        self.index1Vocabulary = get_vocabulary(self.config_dict['index1Name'], self.service)
        self.index2Vocabulary = get_vocabulary(self.config_dict['index2Name'], self.service)



    def test_get_flowCell (self):
        self.assertEqual(self.flowcell.getCode(), self.myCode)
        self.assertEqual(self.containedSamples.size(), 8)
        
        fcProp = self.flowcell.getProperties()
        self.assertEqual(fcProp['SEQUENCER'], 'D00535')
        self.assertEqual(self.flowCellDict['FLOWCELLTYPE'], 'HiSeq Flow Cell v4')
    
    
    def test_get_contained_sample_properties(self):
        self.assertEqual(self.parentDict['BSSE_QGF_34778_C7GMNANXX_1']['BARCODE'], 'GTCCGC')
        self.assertEqual(self.parentDict['BSSE_QGF_32285_C7GMNANXX_7']['CONTACT_PERSON_EMAIL'], 'yann.bourgeois@unibas.ch')
        self.assertEqual(self.samplesPerLaneDict['2'], 23)


    def test_get_vocabulary(self):        
        self.assertEqual(self.index1Vocabulary['CACTCAA'], 'Illumina A032 CACTCAA')
        self.assertEqual(self.index2Vocabulary['GTAAGGAG'],'Index2 (i5) 505 GTAAGGAG')
    
    
    def test_verify_index_length(self):
        self.index_length_dict = verify_index_length(self.parentDict, self.flowCellDict, self.config_dict, self.logger)
        self.assertDictEqual(self.index_length_dict, {6: [6, 0], 5: [6, 0], 7: [8, 8], 8: [8, 8], 3: [8, 0], 2: [6, 0], 1: [6, 0], 4: [8, 0]})
        
        
    def test_create_sample_sheet_dict(self):
        
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])

        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.model, self.parentDict,
                            self.flowCellDict, self.config_dict, self.index1Vocabulary, self.index2Vocabulary, self.flowCellName, self.logger)
        
        self.ordered_sample_sheet_dict['5_BSSE_QGF_32303_C7GMNANXX_5'] = \
        [u'5,BSSE_QGF_32303_C7GMNANXX_5,BSSE_QGF_32303_C7GMNANXX_5_TR_EG_1_GGCTAC,,,SureSelectXT,GGCTAC,BSSE_QGF_32303_C7GMNANXX_5,']

        self.ordered_sample_sheet_dict['5_BSSE_QGF_36788_C7GMNANXX_5'] = \
        [u'5,BSSE_QGF_36788_C7GMNANXX_5,BSSE_QGF_36788_C7GMNANXX_5_G_33_run2_TAAGGC,,,N701,TAAGGC,BSSE_QGF_36788_C7GMNANXX_5,']
        
        self.ordered_sample_sheet_dict['BSSE_QGF_32281_C7GMNANXX_8']= \
        [u'8,BSSE_QGF_32281_C7GMNANXX_8,BSSE_QGF_32281_C7GMNANXX_8_F2_18_3_P162Nextera_TAGGCATG_CTATTAAG,,,N706,TAGGCATG,518,CTATTAAG,BSSE_QGF_32281_C7GMNANXX_8,']
        
    def test_write_sample_sheet_single_lane(self):
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])

        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.model, self.parentDict,
                            self.flowCellDict, self.config_dict, self.index1Vocabulary, self.index2Vocabulary, self.flowCellName, self.logger)
        
        write_sample_sheet_single_lane(self.ordered_sample_sheet_dict, self.flowCellDict,
                                          self.parentDict, self.config_dict, self.options, self.logger, self.csv_file_name)
    
    def tearDown(self):
        self.service.logout()
        self.logger.info('Logged out')

def main():
    unittest.main()

if __name__ == '__main__':
    main()