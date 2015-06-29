#Dropbox for updating STRAIN samples with strain parents. The input is a txt file downlaoded from openBIS, containing the codes of teh strains already registered, the parents and a property called "derived from".
#we read the property called "derived from" and add these to the list of already existing parents. This has to be done in thi way, and cannot be done when creating the strains, becuase to set strains parenst these have to be already registered in openBIS.
#

import os, re, glob, shutil,csv


print "###################################################"


   
#read txt file
def parse_incoming(tr):
  data_rows = []
  f = open(tr.getIncoming().getAbsolutePath(), 'rU')
  dialect = csv.Sniffer().sniff(f.read(1024))
  f.seek(0)
  reader = csv.DictReader(f, dialect=dialect)
  for row in reader:
    data_rows.append(row)
  f.close()
  return data_rows  

def update_sample_with_parents(tr, sample_dict):
  """ Get the specified sample or register it if necessary """

  space_code = "MATERIALS"
  project_code = "YEASTS"
  # The dictionary keys come from the CSV file
  sample_id = sample_dict['identifier']

  yeast_parent_code = sample_dict['derived from']
  yeast_parent_code_split = re.split("\&|+|x|/|-|\?|\(|\)", yeast_parent_code)
  print sample_id, yeast_parent_code_split
  #e.split("/|-|\?|\(|\)" 
  # insert=sample_dict['Insert']
  # insert_split=re.split("/|-|\?|\(|\|+)", insert)
  # comment=sample_dict['Comment']
  # comment_split = re.split("/|-|\?|\(|\)", comment)


  
  
 


  # if 
  for name in yeast_parent_code_split:
    sample = tr.getSample(sample_id)
    sample_for_update = tr.makeSampleMutable(sample)   
    if re.search ("kwy ", name):
      yeast_parent_id = "/MATERIALS/"+name.replace("kwy ","KWY").strip()
      sample_for_update.setParentSampleIdentifiers([yeast_parent_id])        
    elif re.search ("kwy", name):
      yeast_parent_id = "/MATERIALS/"+name.replace("kwy","KWY").strip()
      sample_for_update.setParentSampleIdentifiers([yeast_parent_id])           
    elif re.search ("KWY ", name): 
      yeast_parent_id = "/MATERIALS/"+name.replace("KWY ","KWY").strip()
      sample_for_update.setParentSampleIdentifiers([yeast_parent_id])        
    elif re.search ("KW", name): 
      yeast_parent_id = "/MATERIALS/"+name.replace("KW","KWY").strip()
      sample_for_update.setParentSampleIdentifiers([yeast_parent_id])             
    elif re.search ("k", name): 
      yeast_parent_id = "/MATERIALS/"+name.replace("k","KWY").strip()
      sample_for_update.setParentSampleIdentifiers([yeast_parent_id])        

        

def register_samples_in_openbis(tr, data_rows):
  for sample_dict in data_rows:
    update_sample_with_parents(tr, sample_dict)


def process(tr):
  data_rows = parse_incoming(tr)
  register_samples_in_openbis(tr, data_rows)