import unittest
import re
# import pytest
from createSampleSheet_bcl2fastq import *

def getTodayDate():
    from datetime import date
    d = date.today()
    return d.strftime("%m/%d/%Y")


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
 
 
 
class create_sample_sheet_C7GMNANXX(unittest.TestCase):
    """
    HiSeq 2500, PAIRED_END, Dual Index
    
    """
 
    def setUp(self):
        self.myCode = 'C7GMNANXX'
        self.logger = setUpLogger('log/')
        self.config_dict = readConfig(self.logger)
         
        import argparse
        import shlex
        parser = argparse.ArgumentParser()
         
        parser.add_argument('--flowcell')
        parser.add_argument('--lineending')
        parser.add_argument('--outdir')
        parser.add_argument('--verbose')
         
        cmd_string = ['--flowcell', self.myCode, '--lineending', 'win32', '--outdir', '../../targets/playground', '--verbose', 'false']
        self.options = parser.parse_args(cmd_string)
 
        self.service = OpenbisServiceFacadeFactory.tryCreate(self.config_dict['openbisUserName'],
                                                      self.config_dict['openbisPassword'],
                                                      self.config_dict['openbisServer'],
                                                      self.config_dict['connectionTimeout'])
         
        self.flowcell, self.containedSamples = get_flowcell('ILLUMINA_FLOW_CELL',
                                            self.myCode, self.service, self.logger)
        self.flowCellDict = transform_sample_to_dict(self.flowcell)
        self.parentDict, self.samplesPerLaneDict, self.barcodesPerLaneDict = get_contained_sample_properties(
                                                    self.containedSamples, self.service, self.config_dict)
        self.flowCellName = self.flowcell.getCode()
        self.index1Vocabulary = get_vocabulary(self.config_dict['index1Name'], self.service)
        self.index2Vocabulary = get_vocabulary(self.config_dict['index2Name'], self.service)
        self.index_length_dict = verify_index_length(self.parentDict, self.flowCellDict, self.config_dict, self.logger)
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
 
 
    def test_get_flowCell (self):
        self.assertEqual(self.flowcell.getCode(), self.myCode)
        self.assertEqual(self.containedSamples.size(), 8)
         
        fcProp = self.flowcell.getProperties()
        self.assertEqual(fcProp['SEQUENCER'], 'D00535')
        self.assertEqual(self.flowCellDict['FLOWCELLTYPE'], 'HiSeq Flow Cell v4')
     
     
    def test_get_contained_sample_properties(self):
        self.assertEqual(self.parentDict['BSSE_QGF_34778_C7GMNANXX_1']['BARCODE'], 'GTCCGC')
        self.assertEqual(self.samplesPerLaneDict['2'], 23)
 
 
    def test_get_vocabulary(self):        
        self.assertEqual(self.index1Vocabulary['CACTCAA'], 'Illumina A032 CACTCAA')
        self.assertEqual(self.index2Vocabulary['GTAAGGAG'],'Index2 (i5) 505 GTAAGGAG')
 
 
    def test_verify_index_length(self):
        self.assertDictEqual(self.index_length_dict, {6: [6, 0], 5: [6, 0], 7: [8, 8], 8: [8, 8], 3: [8, 0], 2: [6, 0], 1: [6, 0], 4: [8, 0]})
         
         
    def test_create_sample_sheet_dict(self):
         
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
 
        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)
         
        self.ordered_sample_sheet_dict['5_BSSE_QGF_32303_C7GMNANXX_5'] = \
        [u'5,BSSE_QGF_32303_C7GMNANXX_5,BSSE_QGF_32303_C7GMNANXX_5_TR_EG_1_GGCTAC,,,SureSelectXT,GGCTAC,BSSE_QGF_32303_C7GMNANXX_5,']
 
        self.ordered_sample_sheet_dict['5_BSSE_QGF_36788_C7GMNANXX_5'] = \
        [u'5,BSSE_QGF_36788_C7GMNANXX_5,BSSE_QGF_36788_C7GMNANXX_5_G_33_run2_TAAGGC,,,N701,TAAGGC,BSSE_QGF_36788_C7GMNANXX_5,']
         
        self.ordered_sample_sheet_dict['BSSE_QGF_32281_C7GMNANXX_8']= \
        [u'8,BSSE_QGF_32281_C7GMNANXX_8,BSSE_QGF_32281_C7GMNANXX_8_F2_18_3_P162Nextera_TAGGCATG_CTATTAAG,,,N706,TAGGCATG,518,CTATTAAG,BSSE_QGF_32281_C7GMNANXX_8,']
         
         
    def test_create_header_section(self):
         
        self.date = getTodayDate()
        self.create_header_section = create_header_section(self.model, self.config_dict, self.parentDict, self.flowCellDict, self.index_length_dict, 5)
        self.assertListEqual(self.create_header_section, ['[Header]', 'IEMFileVersion,4', 'Investigator Name,ETHZ_D-BSSE',
                                                          'Project Name,Genomics Facility Basel', u'Experiment Name,C7GMNANXX',
                                                          'Date,'+ self.date, 'Workflow,GenerateFASTQ', 'Application,FASTQ Only',
                                                          'Assay,', u'Description,PAIRED_END_126', 'Chemistry,Default', '', '[Reads]',
                                                          u'126', u'126', '', '[Settings]', '', '[Data]',
                                                         'Lane,Sample_ID,Sample_Name,Sample_Plate,Sample_Well,I7_Index_ID,index,Sample_Project,Description'])
         
        self.create_header_section = create_header_section(self.model, self.config_dict, self.parentDict, self.flowCellDict, self.index_length_dict, 8)
        self.assertListEqual(self.create_header_section, ['[Header]', 'IEMFileVersion,4', 'Investigator Name,ETHZ_D-BSSE', 'Project Name,Genomics Facility Basel',
                                                          u'Experiment Name,C7GMNANXX', 'Date,'+ self.date, 'Workflow,GenerateFASTQ', 'Application,FASTQ Only',
                                                          'Assay,', u'Description,PAIRED_END_126', 'Chemistry,Default', '', '[Reads]', u'126', u'126', '',
                                                          '[Settings]', '', '[Data]', 'Lane,Sample_ID,Sample_Name,Sample_Plate,Sample_Well,I7_Index_ID,index,I5_Index_ID,index2,Sample_Project,Description'])
         
         
    def test_write_sample_sheet_single_lane(self):
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
 
        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)
         
        write_sample_sheet_single_lane(self.model, self.ordered_sample_sheet_dict, self.flowCellDict, self.index_length_dict,
                                          self.parentDict, self.config_dict, self.options, self.logger, self.csv_file_name)
     
    def tearDown(self):
        self.service.logout()
        self.logger.info('Logged out')
 
 
 
 
 
 
class create_sample_sheet_C7P5KANXX(unittest.TestCase):
    """
    HiSeq 2500, SINGLE_READ, Dual Index 
    
    """
 
 
    def setUp(self):
        self.myCode = 'C7P5KANXX'
        self.logger = setUpLogger('log/')
        self.config_dict = readConfig(self.logger)
         
        import argparse
        import shlex
        parser = argparse.ArgumentParser()
         
        parser.add_argument('--flowcell')
        parser.add_argument('--lineending')
        parser.add_argument('--outdir')
        parser.add_argument('--verbose')

         
        cmd_string = ['--flowcell', self.myCode, '--lineending', 'win32', '--outdir', '../../targets/playground', '--verbose', 'false']
        self.options = parser.parse_args(cmd_string)
 
        self.service = OpenbisServiceFacadeFactory.tryCreate(self.config_dict['openbisUserName'],
                                                      self.config_dict['openbisPassword'],
                                                      self.config_dict['openbisServer'],
                                                      self.config_dict['connectionTimeout'])
         
        self.flowcell, self.containedSamples = get_flowcell('ILLUMINA_FLOW_CELL',
                                            self.myCode, self.service, self.logger)
        self.flowCellDict = transform_sample_to_dict(self.flowcell)
        self.parentDict, self.samplesPerLaneDict, self.barcodesPerLaneDict = get_contained_sample_properties(
                                                    self.containedSamples, self.service, self.config_dict)
        self.flowCellName = self.flowcell.getCode()
        self.index1Vocabulary = get_vocabulary(self.config_dict['index1Name'], self.service)
        self.index2Vocabulary = get_vocabulary(self.config_dict['index2Name'], self.service)
        self.index_length_dict = verify_index_length(self.parentDict, self.flowCellDict, self.config_dict, self.logger)
 
 
    def test_get_flowCell (self):
        self.assertEqual(self.flowcell.getCode(), self.myCode)
        self.assertEqual(self.containedSamples.size(), 8)
         
        fcProp = self.flowcell.getProperties()
        self.assertEqual(fcProp['SEQUENCER'], 'D00404')
        self.assertEqual(self.flowCellDict['FLOWCELLTYPE'], 'HiSeq Flow Cell v4')
     
     
    def test_get_contained_sample_properties(self):
        self.assertEqual(self.parentDict['BSSE_QGF_36781_C7P5KANXX_8']['BARCODE'], 'CTTGTAA')
        self.assertEqual(self.parentDict['BSSE_QGF_36779_C7P5KANXX_8']['NCBI_ORGANISM_TAXONOMY'], '10090')
        self.assertEqual(self.samplesPerLaneDict['8'], 6)
  
    def test_verify_index_length(self):
        self.assertDictEqual(self.index_length_dict, {6: [8, 8], 5: [8, 8], 7: [8, 8], 8: [7, 7], 3: [8, 8], 2: [8, 8], 1: [8, 8], 4: [8, 8]})
          
          
    def test_create_sample_sheet_dict(self):
          
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
  
        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)
         
        self.assertDictEqual(self.ordered_sample_sheet_dict,
             {u'8_BSSE_QGF_36781_C7P5KANXX_8': [u'8,BSSE_QGF_36781_C7P5KANXX_8,BSSE_QGF_36781_C7P5KANXX_8_Ribomethseq_mousecerebellum_comparison_CTTGTAA_NOINDEX,,,A012,CTTGTAA,(NoIndex),NOINDEX,BSSE_QGF_36781_C7P5KANXX_8,'],
              u'8_BSSE_QGF_36780_C7P5KANXX_8': [u'8,BSSE_QGF_36780_C7P5KANXX_8,BSSE_QGF_36780_C7P5KANXX_8_Ribomethseq_HEK_comparison_GCCAATA_NOINDEX,,,A006,GCCAATA,(NoIndex),NOINDEX,BSSE_QGF_36780_C7P5KANXX_8,'], 
              u'8_BSSE_QGF_36552_C7P5KANXX_8': [u'8,BSSE_QGF_36552_C7P5KANXX_8,BSSE_QGF_36552_C7P5KANXX_8_CLIP_444_1_TGACCAA_NOINDEX,,,A004,TGACCAA,(NoIndex),NOINDEX,BSSE_QGF_36552_C7P5KANXX_8,'], 
              u'8_BSSE_QGF_36779_C7P5KANXX_8': [u'8,BSSE_QGF_36779_C7P5KANXX_8,BSSE_QGF_36779_C7P5KANXX_8_HITS_CLIP_Fibrillarin_mouseNeurons_2_CAGATCA_NOINDEX,,,A007,CAGATCA,(NoIndex),NOINDEX,BSSE_QGF_36779_C7P5KANXX_8,']})
 
          
    def test_create_header_section(self):
        self.date = getTodayDate()
 
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
        self.create_header_section = create_header_section(self.model, self.config_dict, self.parentDict, self.flowCellDict, self.index_length_dict, 5)
        self.assertListEqual(self.create_header_section, ['[Header]', 'IEMFileVersion,4', 'Investigator Name,ETHZ_D-BSSE',
                                                          'Project Name,Genomics Facility Basel', u'Experiment Name,C7P5KANXX',
                                                          'Date,'+ self.date, 'Workflow,GenerateFASTQ', 'Application,FASTQ Only',
                                                          'Assay,', u'Description,SINGLE_READ_51', 'Chemistry,Default', '',
                                                          '[Reads]', u'51', '', '[Settings]', '', '[Data]',
                                                          'Lane,Sample_ID,Sample_Name,Sample_Plate,Sample_Well,I7_Index_ID,index,I5_Index_ID,index2,Sample_Project,Description'])
          
    def test_write_sample_sheet_single_lane(self):
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
  
        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)
         
        write_sample_sheet_single_lane(self.model, self.ordered_sample_sheet_dict, self.flowCellDict, self.index_length_dict,
                                          self.parentDict, self.config_dict, self.options, self.logger, self.csv_file_name)
     
    def tearDown(self):
        self.service.logout()
        self.logger.info('Logged out')


class create_sample_sheet_HJWC3BGXX(unittest.TestCase):
    """
    NextSeq, SINGLE_READ, Single index
    """
     
     
    def setUp(self):
        self.myCode = 'HJWC3BGXX'
        self.logger = setUpLogger('log/')
        self.config_dict = readConfig(self.logger)
         
        import argparse
        import shlex
        parser = argparse.ArgumentParser()
         
        parser.add_argument('--flowcell')
        parser.add_argument('--lineending')
        parser.add_argument('--outdir')
        parser.add_argument('--verbose')
         
        cmd_string = ['--flowcell', self.myCode, '--lineending', 'win32', '--outdir', '../../targets/playground', '--verbose', 'false']
        self.options = parser.parse_args(cmd_string)
 
        self.service = OpenbisServiceFacadeFactory.tryCreate(self.config_dict['openbisUserName'],
                                                      self.config_dict['openbisPassword'],
                                                      self.config_dict['openbisServer'],
                                                      self.config_dict['connectionTimeout'])
         
        self.flowcell, self.containedSamples = get_flowcell('ILLUMINA_FLOW_CELL',
                                            self.myCode, self.service, self.logger)
        self.flowCellDict = transform_sample_to_dict(self.flowcell)
        self.parentDict, self.samplesPerLaneDict, self.barcodesPerLaneDict = get_contained_sample_properties(
                                                    self.containedSamples, self.service, self.config_dict)
        self.flowCellName = self.flowcell.getCode()
        self.index1Vocabulary = get_vocabulary(self.config_dict['index1Name'], self.service)
        self.index2Vocabulary = get_vocabulary(self.config_dict['index2Name'], self.service)
        self.index_length_dict = verify_index_length(self.parentDict, self.flowCellDict, self.config_dict, self.logger)
 
 
    def test_get_flowCell (self):
        self.assertEqual(self.flowcell.getCode(), self.myCode)
        self.assertEqual(self.containedSamples.size(), 1)
         
        fcProp = self.flowcell.getProperties()
        self.assertEqual(fcProp['SEQUENCER'], 'NS500318')
        self.assertEqual(self.flowCellDict['ILLUMINA_PIPELINE_VERSION'], '2.4.6')
     
     
    def test_get_contained_sample_properties(self):
        self.assertEqual(self.parentDict['BSSE_QGF_37091_HJWC3BGXX_1']['BARCODE'], 'AGTCAAC')
        self.assertEqual(self.parentDict['BSSE_QGF_37100_HJWC3BGXX_1']['NCBI_ORGANISM_TAXONOMY'], '9606')
        self.assertEqual(self.samplesPerLaneDict['1'], 18)
   
    def test_verify_index_length(self):
        self.assertDictEqual(self.index_length_dict, {3: [6, 0], 2: [6, 0], 1: [6, 0], 4: [6, 0]})
 
 
    def test_create_sample_sheet_dict(self):
           
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
   
        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)
          
        self.assertDictEqual(self.ordered_sample_sheet_dict,
                            {u'1_BSSE_QGF_37098_HJWC3BGXX_1': [u'BSSE_QGF_37098_HJWC3BGXX_1,BSSE_QGF_37098_HJWC3BGXX_1_TB_358_PQR_2_0_GTTTCG,,,A021,GTTTCG,BSSE_QGF_37098_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37093_HJWC3BGXX_1': [u'BSSE_QGF_37093_HJWC3BGXX_1,BSSE_QGF_37093_HJWC3BGXX_1_TB_356_PQR_1_0_ATGTCA,,,A015,ATGTCA,BSSE_QGF_37093_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37094_HJWC3BGXX_1': [u'BSSE_QGF_37094_HJWC3BGXX_1,BSSE_QGF_37094_HJWC3BGXX_1_TB_357_BKM_1_0_CCGTCC,,,A016,CCGTCC,BSSE_QGF_37094_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37095_HJWC3BGXX_1': [u'BSSE_QGF_37095_HJWC3BGXX_1,BSSE_QGF_37095_HJWC3BGXX_1_TB_356_MTD_1_0_GTCCGC,,,A018,GTCCGC,BSSE_QGF_37095_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37087_HJWC3BGXX_1': [u'BSSE_QGF_37087_HJWC3BGXX_1,BSSE_QGF_37087_HJWC3BGXX_1_TB_355_DMSO1_GATCAG,,,A009,GATCAG,BSSE_QGF_37087_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37096_HJWC3BGXX_1': [u'BSSE_QGF_37096_HJWC3BGXX_1,BSSE_QGF_37096_HJWC3BGXX_1_TB_358_GDC_1_0_GTGAAA,,,A019,GTGAAA,BSSE_QGF_37096_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37092_HJWC3BGXX_1': [u'BSSE_QGF_37092_HJWC3BGXX_1,BSSE_QGF_37092_HJWC3BGXX_1_TB_357_COL_0_05_AGTTCC,,,A014,AGTTCC,BSSE_QGF_37092_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37088_HJWC3BGXX_1': [u'BSSE_QGF_37088_HJWC3BGXX_1,BSSE_QGF_37088_HJWC3BGXX_1_TB_355_PQR_0_5_TAGCTT,,,A010,TAGCTT,BSSE_QGF_37088_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37089_HJWC3BGXX_1': [u'BSSE_QGF_37089_HJWC3BGXX_1,BSSE_QGF_37089_HJWC3BGXX_1_TB_359_BKM_0_5_1_GGCTAC,,,A011,GGCTAC,BSSE_QGF_37089_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37101_HJWC3BGXX_1': [u'BSSE_QGF_37101_HJWC3BGXX_1,BSSE_QGF_37101_HJWC3BGXX_1_TB_357_GDC_2_0_ACTGAT,,,A025,ACTGAT,BSSE_QGF_37101_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37090_HJWC3BGXX_1': [u'BSSE_QGF_37090_HJWC3BGXX_1,BSSE_QGF_37090_HJWC3BGXX_1_TB_358_MTD_0_5_CTTGTA,,,A012,CTTGTA,BSSE_QGF_37090_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37099_HJWC3BGXX_1': [u'BSSE_QGF_37099_HJWC3BGXX_1,BSSE_QGF_37099_HJWC3BGXX_1_TB_360_BKM_2_0_1_CGTACG,,,A022,CGTACG,BSSE_QGF_37099_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37091_HJWC3BGXX_1': [u'BSSE_QGF_37091_HJWC3BGXX_1,BSSE_QGF_37091_HJWC3BGXX_1_TB_358_GDC_0_5_AGTCAA,,,A013,AGTCAA,BSSE_QGF_37091_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37102_HJWC3BGXX_1': [u'BSSE_QGF_37102_HJWC3BGXX_1,BSSE_QGF_37102_HJWC3BGXX_1_TB_361_COL_0_2_1_ATTCCT,,,A027,ATTCCT,BSSE_QGF_37102_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37100_HJWC3BGXX_1': [u'BSSE_QGF_37100_HJWC3BGXX_1,BSSE_QGF_37100_HJWC3BGXX_1_TB_357_MTD_2_0_GAGTGG,,,A023,GAGTGG,BSSE_QGF_37100_HJWC3BGXX_1,'],
                             u'1_BSSE_QGF_37097_HJWC3BGXX_1': [u'BSSE_QGF_37097_HJWC3BGXX_1,BSSE_QGF_37097_HJWC3BGXX_1_TB_358_COL_0_1_GTGGCC,,,A020,GTGGCC,BSSE_QGF_37097_HJWC3BGXX_1,']})
           
    def test_create_header_section(self):
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
        lane = 1
        self.date = getTodayDate()
        self.create_header_section = create_header_section(self.model, self.config_dict, self.parentDict, self.flowCellDict, self.index_length_dict, lane)
        self.assertListEqual(self.create_header_section, ['[Header]', 'IEMFileVersion,4', 'Investigator Name,ETHZ_D-BSSE', 'Project Name,Genomics Facility Basel',
                                                          u'Experiment Name,HJWC3BGXX', 'Date,' + self.date, 'Workflow,GenerateFASTQ', 'Application,FASTQ Only',
                                                          'Assay,', u'Description,SINGLE_READ_81', 'Chemistry,Default', '', '[Reads]', u'81', '', '[Settings]', '',
                                                          '[Data]', 'Sample_ID,Sample_Name,Sample_Plate,Sample_Well,I7_Index_ID,index,Sample_Project,Description'])
           
    def test_write_sample_sheet_single_lane(self):
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
   
        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)
          
        write_sample_sheet_single_lane(self.model, self.ordered_sample_sheet_dict, self.flowCellDict, self.index_length_dict,
                                          self.parentDict, self.config_dict, self.options, self.logger, self.csv_file_name)
     
    def tearDown(self):
        self.service.logout()
        self.logger.info('Logged out')


class create_sample_sheet_000000000_AH5W3(unittest.TestCase):
    """
    MiSeq, PAIRED_END, Single Index
    """
    
    
    def setUp(self):
        self.myCode = '000000000-AH5W3'
        self.logger = setUpLogger('log/')
        self.config_dict = readConfig(self.logger)
        
        import argparse
        import shlex
        parser = argparse.ArgumentParser()
        
        parser.add_argument('--flowcell')
        parser.add_argument('--lineending')
        parser.add_argument('--outdir')
        parser.add_argument('--verbose')

        
        cmd_string = ['--flowcell', self.myCode, '--lineending', 'win32', '--outdir', '../../targets/playground', '--verbose', 'true']
        self.options = parser.parse_args(cmd_string)

        self.service = OpenbisServiceFacadeFactory.tryCreate(self.config_dict['openbisUserName'],
                                                      self.config_dict['openbisPassword'],
                                                      self.config_dict['openbisServer'],
                                                      self.config_dict['connectionTimeout'])
        
        self.flowcell, self.containedSamples = get_flowcell('ILLUMINA_FLOW_CELL',
                                            self.myCode, self.service, self.logger)
        self.flowCellDict = transform_sample_to_dict(self.flowcell)
        self.parentDict, self.samplesPerLaneDict, self.barcodesPerLaneDict = get_contained_sample_properties(
                                                    self.containedSamples, self.service, self.config_dict)
        self.flowCellName = self.flowcell.getCode()
        self.index1Vocabulary = get_vocabulary(self.config_dict['index1Name'], self.service)
        self.index2Vocabulary = get_vocabulary(self.config_dict['index2Name'], self.service)
        self.index_length_dict = verify_index_length(self.parentDict, self.flowCellDict, self.config_dict, self.logger)


    def test_get_flowCell (self):
        self.assertEqual(self.flowcell.getCode(), self.myCode)
        self.assertEqual(self.containedSamples.size(), 1)
        
        fcProp = self.flowcell.getProperties()
        self.assertEqual(fcProp['SEQUENCER'], 'M01761')
        self.assertEqual(self.flowCellDict['ILLUMINA_PIPELINE_VERSION'], '1.18.54')
    
    
    def test_get_contained_sample_properties(self):
        self.assertEqual(self.parentDict['BSSE_QGF_36763_000000000_AH5W3_1']['BARCODE'], 'ATGTCAG')
        self.assertEqual(self.parentDict['BSSE_QGF_36761_000000000_AH5W3_1']['NCBI_ORGANISM_TAXONOMY'], '10090')
        self.assertEqual(self.samplesPerLaneDict['1'], 10)
   
    def test_verify_index_length(self):
        self.assertDictEqual(self.index_length_dict,{1: [6, 0]})
 
 
    def test_create_sample_sheet_dict(self):
           
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
   
        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)
          
        
        self.assertDictEqual(self.ordered_sample_sheet_dict,
                {u'1_BSSE_QGF_36763_000000000_AH5W3_1': [u'1,BSSE_QGF_36763_000000000_AH5W3_1,BSSE_QGF_36763_000000000_AH5W3_1_H7_PRE_Idx_15_ATGTCA,,,A015,ATGTCA,BSSE_QGF_36763_000000000_AH5W3_1,'],
                 u'1_BSSE_QGF_36765_000000000_AH5W3_1': [u'1,BSSE_QGF_36765_000000000_AH5W3_1,BSSE_QGF_36765_000000000_AH5W3_1_H8_NBC_Idx_16_CCGTCC,,,A016,CCGTCC,BSSE_QGF_36765_000000000_AH5W3_1,'],
                 u'1_BSSE_QGF_36768_000000000_AH5W3_1': [u'1,BSSE_QGF_36768_000000000_AH5W3_1,BSSE_QGF_36768_000000000_AH5W3_1_H8_BMPC_Idx_19_GTGAAA,,,A019,GTGAAA,BSSE_QGF_36768_000000000_AH5W3_1,'],
                 u'1_BSSE_QGF_36767_000000000_AH5W3_1': [u'1,BSSE_QGF_36767_000000000_AH5W3_1,BSSE_QGF_36767_000000000_AH5W3_1_H8_PRE_Idx_20_GTGGCC,,,A020,GTGGCC,BSSE_QGF_36767_000000000_AH5W3_1,'],
                 u'1_BSSE_QGF_36766_000000000_AH5W3_1': [u'1,BSSE_QGF_36766_000000000_AH5W3_1,BSSE_QGF_36766_000000000_AH5W3_1_H8_SPC_Idx_18_GTCCGC,,,A018,GTCCGC,BSSE_QGF_36766_000000000_AH5W3_1,'],
                 u'1_BSSE_QGF_36761_000000000_AH5W3_1': [u'1,BSSE_QGF_36761_000000000_AH5W3_1,BSSE_QGF_36761_000000000_AH5W3_1_H7_NBC_Idx_12_CTTGTA,,,A012,CTTGTA,BSSE_QGF_36761_000000000_AH5W3_1,'],
                 u'1_BSSE_QGF_36762_000000000_AH5W3_1': [u'1,BSSE_QGF_36762_000000000_AH5W3_1,BSSE_QGF_36762_000000000_AH5W3_1_H7_SPC_Idx_13_AGTCAA,,,A013,AGTCAA,BSSE_QGF_36762_000000000_AH5W3_1,'],
                 u'1_BSSE_QGF_36764_000000000_AH5W3_1': [u'1,BSSE_QGF_36764_000000000_AH5W3_1,BSSE_QGF_36764_000000000_AH5W3_1_H7_BMPC_Idx_14_AGTTCC,,,A014,AGTTCC,BSSE_QGF_36764_000000000_AH5W3_1,']})


    def test_create_header_section(self):
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
        lane = 1
        self.date = getTodayDate()
        self.create_header_section = create_header_section(self.model, self.config_dict, self.parentDict, self.flowCellDict, self.index_length_dict, lane)
        self.assertListEqual(self.create_header_section, ['[Header]', 'IEMFileVersion,4', 'Investigator Name,ETHZ_D-BSSE', 'Project Name,Genomics Facility Basel',
                                                          u'Experiment Name,000000000-AH5W3', 'Date,' + self.date, 'Workflow,GenerateFASTQ', 'Application,FASTQ Only',
                                                          'Assay,', u'Description,PAIRED_END_301', 'Chemistry,Default', '', '[Reads]', u'301', u'301', '', '[Settings]', '',
                                                          '[Data]', 'Lane,Sample_ID,Sample_Name,Sample_Plate,Sample_Well,I7_Index_ID,index,Sample_Project,Description'])
           
    def test_write_sample_sheet_single_lane(self):
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])

        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)

        write_sample_sheet_single_lane(self.model, self.ordered_sample_sheet_dict, self.flowCellDict, self.index_length_dict,
                                          self.parentDict, self.config_dict, self.options, self.logger, self.csv_file_name)
    
    def tearDown(self):
        self.service.logout()
        self.logger.info('Logged out')



class create_sample_sheet_H32NFBCXX(unittest.TestCase):
    """
    HiSeq 2500 Rapid Run with no indices
    """
    
    
    def setUp(self):
        self.myCode = 'H32NFBCXX'
        self.logger = setUpLogger('log/')
        self.config_dict = readConfig(self.logger)
        
        import argparse
        import shlex
        parser = argparse.ArgumentParser()
        
        parser.add_argument('--flowcell')
        parser.add_argument('--lineending')
        parser.add_argument('--outdir')
        parser.add_argument('--verbose')
        
        cmd_string = ['--flowcell', self.myCode, '--lineending', 'win32', '--outdir', '../../targets/playground', '--verbose', 'false']
        self.options = parser.parse_args(cmd_string)

        self.service = OpenbisServiceFacadeFactory.tryCreate(self.config_dict['openbisUserName'],
                                                      self.config_dict['openbisPassword'],
                                                      self.config_dict['openbisServer'],
                                                      self.config_dict['connectionTimeout'])
        
        self.flowcell, self.containedSamples = get_flowcell('ILLUMINA_FLOW_CELL',
                                            self.myCode, self.service, self.logger)
        self.flowCellDict = transform_sample_to_dict(self.flowcell)
        self.parentDict, self.samplesPerLaneDict, self.barcodesPerLaneDict = get_contained_sample_properties(
                                                    self.containedSamples, self.service, self.config_dict)
        self.flowCellName = self.flowcell.getCode()
        self.index1Vocabulary = get_vocabulary(self.config_dict['index1Name'], self.service)
        self.index2Vocabulary = get_vocabulary(self.config_dict['index2Name'], self.service)
        self.index_length_dict = verify_index_length(self.parentDict, self.flowCellDict, self.config_dict, self.logger)


    def test_get_flowCell (self):
        self.assertEqual(self.flowcell.getCode(), self.myCode)
        self.assertEqual(self.containedSamples.size(), 2)
        
        fcProp = self.flowcell.getProperties()
        self.assertEqual(fcProp['SEQUENCER'], u'D00535')
        self.assertEqual(self.flowCellDict['ILLUMINA_PIPELINE_VERSION'], '1.18.64')
    
    
    def test_get_contained_sample_properties(self):
        self.assertNotIn(self.parentDict['BSSE_QGF_29352_H32NFBCXX_1'],['BARCODE'])
#         self.assertEqual(self.parentDict['BSSE_QGF_29352_H32NFBCXX_1']['NCBI_ORGANISM_TAXONOMY'], '32644')
#         self.assertEqual(self.samplesPerLaneDict['1'], 2)
   
    def test_verify_index_length(self):
        self.assertDictEqual(self.index_length_dict,{1: [7, 0], 2:[7, 0]})
  
  
    def test_create_sample_sheet_dict(self):
            
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
    
        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)
           
        self.assertDictEqual(self.ordered_sample_sheet_dict,
                {u'1_BSSE_QGF_29352_H32NFBCXX_1': [u'1,BSSE_QGF_29352_H32NFBCXX_1,BSSE_QGF_29352_H32NFBCXX_1_Lib_Amph_4,,,,,BSSE_QGF_29352_H32NFBCXX_1,'],
                 u'2_BSSE_QGF_29352_H32NFBCXX_2': [u'2,BSSE_QGF_29352_H32NFBCXX_2,BSSE_QGF_29352_H32NFBCXX_2_Lib_Amph_4,,,,,BSSE_QGF_29352_H32NFBCXX_2,']})
 
    def test_create_header_section(self):
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
        lane = 1
        self.date = getTodayDate()
        self.create_header_section = create_header_section(self.model, self.config_dict, self.parentDict, self.flowCellDict, self.index_length_dict, lane)
        self.assertListEqual(self.create_header_section, ['[Header]', 'IEMFileVersion,4', 'Investigator Name,ETHZ_D-BSSE', 'Project Name,Genomics Facility Basel',
                                                          u'Experiment Name,H32NFBCXX', 'Date,' + self.date, 'Workflow,GenerateFASTQ', 'Application,FASTQ Only',
                                                          'Assay,', u'Description,SINGLE_READ_201', 'Chemistry,Default', '', '[Reads]', u'201', '', '[Settings]', '',
                                                          '[Data]', 'Lane,Sample_ID,Sample_Name,Sample_Plate,Sample_Well,I7_Index_ID,index,Sample_Project,Description'])
            
    def test_write_sample_sheet_single_lane(self):
        self.model = get_model(self.flowCellDict['RUN_NAME_FOLDER'])
 
        self.ordered_sample_sheet_dict, self.csv_file_name = create_sample_sheet_dict(self.service, self.barcodesPerLaneDict, self.containedSamples, self.samplesPerLaneDict, self.model, self.parentDict,
                                                                                      self.index_length_dict, self.flowCellDict, self.config_dict, self.index1Vocabulary,
                                                                                      self.index2Vocabulary, self.flowCellName, self.logger)
 
        write_sample_sheet_single_lane(self.model, self.ordered_sample_sheet_dict, self.flowCellDict, self.index_length_dict,
                                          self.parentDict, self.config_dict, self.options, self.logger, self.csv_file_name)
     
    def tearDown(self):
        self.service.logout()
        self.logger.info('Logged out')


def main():
    unittest.main()

if __name__ == '__main__':
    main()