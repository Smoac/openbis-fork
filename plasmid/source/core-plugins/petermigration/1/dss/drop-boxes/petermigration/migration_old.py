#!/usr/bin/env python


import sys, csv, os, re
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType
from datetime import datetime

print "########################################################"

SPACE_CODE = "INVENTORY"
PROJECT_CODE = "MATERIALS"
PROJECT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s" % vars()
EXPERIMENT_CODE = "ANTIBODY"
EXPERIMENT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s/%(EXPERIMENT_CODE)s" % vars()

# A map that has the sample property codes as the key and the CSV header as the value
CSV_TO_PROPERTY_MAPPING = {
  # PROPERTY_CODE : (CSV_HEADER, FORMAT)
  'NAME'  : ('Antibody ID', DataType.VARCHAR),
  'ANTIBODY_ID_NR': ('antibody_id_nr', DataType.VARCHAR),  
  'ANTIGEN ': ('antigen', DataType.VARCHAR),
  'BARCODE' : ('barcode', DataType.VARCHAR),
  'BARCODE_LABEL' : ('barcode', DataType.VARCHAR),
  'ANTIBODY_FACS_BLOCK' : ('block facs', DataType.VARCHAR),
  'ANTIBODY_IF_BLOCK' : ('block IFF', DataType.VARCHAR),
  'ANTIBODY_WB_BLOCK' : ('block western', DataType.VARCHAR),
  'BOX' : ('box', DataType.VARCHAR),
  'CATALOGUE_NUMBER' : ('catalog id #', DataType.VARCHAR),
  'CLASS' : ('class', DataType.VARCHAR),
  'CLONE' : ('clone', DataType.VARCHAR),
  'ANTIBODY_FACS_CONC' : ('conc facs', DataType.VARCHAR), 
  'ANTIBODY_IF_CONC'  : ('conc IFF', DataType.VARCHAR),
  'ANTIBODY_IP_CONC' : ('conc ip', DataType.VARCHAR),
  'ANTIBODY_WB_CONC' : ('ANTIBODY_WB_CONC', DataType.VARCHAR), 
  'ANTIBODY_CONCENTRATION' : ('concentration', DataType.VARCHAR),   
  'ANTIBODY_CROSSREACTIVITY'  : ('crossreactivity', DataType.VARCHAR),  
  'ANTIBODY_FACS_FIX' : ('fix facs', DataType.VARCHAR),
  'ANTIBODY_IF_FIX' : ('fix IFF', DataType.VARCHAR),
  'ANTIBODY_WB_FIX' : ('fix western', DataType.VARCHAR),
  'COMMENTS'  : ('info', DataType.VARCHAR),
  'LOTNUMBER' : ('lotnumber', DataType.VARCHAR),
  'MODIFIED_BY': ('modified by', DataType.VARCHAR), 
  'ANTIBODY_FACS_NOTES' : ('notes FACS', DataType.VARCHAR),
  'ANTIBODY_IF_NOTES' : ('notes IFF', DataType.VARCHAR),
  'ANTIBODY_IP_NOTES': ('notes IP', DataType.VARCHAR),
  'ANTIBODY_WB_NOTES' : ('notes western', DataType.VARCHAR),
  'RACK' : ('rack', DataType.VARCHAR),
  'PUBLISHED' : ('reference', DataType.VARCHAR),
  'SERIAL_NUMBER' : ('serial number', DataType.INTEGER),
  'ANTIGEN_SIZE'  : ('size', DataType.VARCHAR),
  'BOX_NUMBER' : ('antibody ID data box::box label', DataType.VARCHAR),
  'FROZEN_BY' : ('antibody ID data box::frozen by', DataType.VARCHAR),
  'FREEZER_NAME'  : ('antibody ID data box::location', DataType.CONTROLLEDVOCABULARY),
  'BOX_POSITION': ('antibody ID data box::position', DataType.VARCHAR),

}

#create a space if it does not exist
def create_space_if_needed(tr):
  space = tr.getSpace(SPACE_CODE)
  if None == space:
    space = tr.createNewSpace(SPACE_CODE, None)
    space.setDescription("Inventory space")

#create a project if it does not exist
def create_project_if_needed(tr):
  project = tr.getProject(PROJECT_ID)
  if None == project:
    create_space_if_needed(tr)
    project = tr.createNewProject(PROJECT_ID)
    project.setDescription("Materials purchased or produced in the lab")

#create an experiment if it does not exist
def create_experiment_if_needed(tr):
  """ Get the specified experiment or register it if necessary """
  exp = tr.getExperiment(EXPERIMENT_ID)
  if None == exp:
    create_project_if_needed(tr)
    exp = tr.createNewExperiment(EXPERIMENT_ID, 'DEFAULT_EXPERIMENT')
    
  return exp

#parse the csv file exported form Filemaker
def parse_incoming(tr,exp): 
  data_rows = []
  f = open(tr.getIncoming().getAbsolutePath(), 'rU')
  dialect = csv.Sniffer().sniff(f.read())
  f.seek(0)
  reader = csv.DictReader(f,  dialect = dialect)
  
  ##
  ## 0. Current sample and storage properties being used
  ##
  storageProperties = [{
    "FREEZER_NAME" : "FREEZER_NAME",
    "ROW" : "ROW",
    "COLUMN" : "COLUMN",
    "BOX_NUMBER" : "BOX_NUMBER",
    "BOX_POSITION" : "BOX_POSITION",
    "USER_ID" : "USER_PROPERTY"
  }, {
    "FREEZER_NAME" : "FREEZER_NAME_2",
    "ROW" : "ROW_2",
    "COLUMN" : "COLUMN_2",
    "BOX_NUMBER" : "BOX_NUMBER_2",
    "BOX_POSITION" : "BOX_POSITION_2",
    "USER_ID" : "USER_PROPERTY_2"
  }, {
    "FREEZER_NAME" : "FREEZER_NAME_3",
    "ROW" : "ROW_3",
    "COLUMN" : "COLUMN_3",
    "BOX_NUMBER" : "BOX_NUMBER_3",
    "BOX_POSITION" : "BOX_POSITION_3",
    "USER_ID" : "USER_PROPERTY_3"
  }, {
    "FREEZER_NAME" : "FREEZER_NAME_4",
    "ROW" : "ROW_4",
    "COLUMN" : "COLUMN_4",
    "BOX_NUMBER" : "BOX_NUMBER_4",
    "BOX_POSITION" : "BOX_POSITION_4",
    "USER_ID" : "USER_PROPERTY_4"
  }, {
    "FREEZER_NAME" : "FREEZER_NAME_5",
    "ROW" : "ROW_5",
    "COLUMN" : "COLUMN_5",
    "BOX_NUMBER" : "BOX_NUMBER_5",
    "BOX_POSITION" : "BOX_POSITION_5",
    "USER_ID" : "USER_PROPERTY_5"
  }, {
    "FREEZER_NAME" : "FREEZER_NAME_6",
    "ROW" : "ROW_6",
    "COLUMN" : "COLUMN_6",
    "BOX_NUMBER" : "BOX_NUMBER_6",
    "BOX_POSITION" : "BOX_POSITION_6",
    "USER_ID" : "USER_PROPERTY_6"
  }, {
    "FREEZER_NAME" : "FREEZER_NAME_7",
    "ROW" : "ROW_7",
    "COLUMN" : "COLUMN_7",
    "BOX_NUMBER" : "BOX_NUMBER_7",
    "BOX_POSITION" : "BOX_POSITION_7",
    "USER_ID" : "USER_PROPERTY_7"
  }, {
    "FREEZER_NAME" : "FREEZER_NAME_8",
    "ROW" : "ROW_8",
    "COLUMN" : "COLUMN_8",
    "BOX_NUMBER" : "BOX_NUMBER_8",
    "BOX_POSITION" : "BOX_POSITION_8",
    "USER_ID" : "USER_PROPERTY_8"
  }, {
    "FREEZER_NAME" : "FREEZER_NAME_9",
    "ROW" : "ROW_9",
    "COLUMN" : "COLUMN_9",
    "BOX_NUMBER" : "BOX_NUMBER_9",
    "BOX_POSITION" : "BOX_POSITION_9",
    "USER_ID" : "USER_PROPERTY_9"
  }, {
    "FREEZER_NAME" : "FREEZER_NAME_10",
    "ROW" : "ROW_10",
    "COLUMN" : "COLUMN_10",
    "BOX_NUMBER" : "BOX_NUMBER_10",
    "BOX_POSITION" : "BOX_POSITION_10",
    "USER_ID" : "USER_PROPERTY_10"
  }
  ];

  
  currentSample = None
  storagePropertiesIndex = 0

  for row in reader:
    ##
    ## 1. Check if there is an entity with all information or only new storage data
    ##
    onlyStorage = None
    
    space_code = SPACE_CODE
    sample_code = row.get('ANTIBODY_ID_NR')
    sample_id = "/%(space_code)s/%(sample_code)s" % vars()
    
    if sample_code != None and sample_code != '':
      storagePropertiesIndex = 0
      onlyStorage = False
    else:
      onlyStorage = True
      storagePropertiesIndex += 1

     
    ##
    ## 2. Depending on the outcome you set all fields or only new storage fields
    ##
    if not onlyStorage:
      # 1. Set all properties

      currentSample = tr.getSample(sample_id)
      if not currentSample:
        currentSample = tr.createNewSample(sample_id, 'ANTIBODY')
      currentSample.setExperiment(exp)
      
      for key in CSV_TO_PROPERTY_MAPPING:
        prop_value = row[CSV_TO_PROPERTY_MAPPING[key][0]]
        if prop_value == '' or prop_value == None:
          prop_value = 'n.a.'
        currentSample.setPropertyValue(CSV_TO_PROPERTY_MAPPING[key][0],prop_value)
    
    else:
      # 2. Set always the storage properties
    

      freezer_name_label =storageProperties[storagePropertiesIndex]['FREEZER_NAME']
      freezer_name_value =row[CSV_TO_PROPERTY_MAPPING['FREEZER_NAME'][0]]
      box_number_label =storageProperties[storagePropertiesIndex]['BOX_NUMBER']
      box_number_value =row[CSV_TO_PROPERTY_MAPPING['BOX_NUMBER'][0]]
      box_position_label = storageProperties[storagePropertiesIndex]['BOX_POSITION']
      box_position_value = row[CSV_TO_PROPERTY_MAPPING['BOX_POSITION'][0]]
  
     # row_label =storageProperties[storagePropertiesIndex]['ROW']
     # row_value =row[CSV_TO_PROPERTY_MAPPING['ROW'][0]]
    #  column_label =storageProperties[storagePropertiesIndex]['COLUMN']
     # column_value =row[CSV_TO_PROPERTY_MAPPING['COLUMN'][0]]
     # user_id_label =storageProperties[storagePropertiesIndex]['USER_ID']
    #  user_id_value = row[CSV_TO_PROPERTY_MAPPING['USER_ID'][0]]


      currentSample.setPropertyValue(freezer_name_label, freezer_name_value)      
      currentSample.setPropertyValue(box_number_label, box_number_value)      
      currentSample.setPropertyValue(box_position_label, box_position_value)      
      #currentSample.setPropertyValue(row_label, row_value)      
     # currentSample.setPropertyValue(column_label, column_value)     
     # currentSample.setPropertyValue(user_id_label, user_id_value)    
   

  f.close()
  return data_rows


def process(tr):
  exp = create_experiment_if_needed(tr)
  data_rows = parse_incoming(tr,exp)


  
 

