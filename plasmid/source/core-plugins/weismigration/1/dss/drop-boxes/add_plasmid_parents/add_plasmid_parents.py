#Dropbox for updating PLASMID samples with Plasmid parents. The input is a txt file downlaoded from openBIS, containing the codes of teh plasmdis already registered, the parents and a property called Plasmid Parents.
#we read the property called plasmid parents and add these to the list of already existing parents. This has to be done in thi way, and cannot be done when creating the plasmids, becuase to set plasmid parenst these have to be already registered in openBIS.
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
  project_code = "PLASMIDS"
  # The dictionary keys come from the CSV file
  sample_code = sample_dict['Code']
  parents_code=sample_dict['Parents']
  parents_code_list=re.split(" ", parents_code)
  plasmid_parent_code = sample_dict['Parent plasmid']
  plasmid_parent_code_split = re.split("/|-|\?|\(|\)", plasmid_parent_code)
  print sample_code, plasmid_parent_code_split
  # insert=sample_dict['Insert']
  # insert_split=re.split("/|-|\?|\(|\|+)", insert)
  # comment=sample_dict['Comment']
  # comment_split = re.split("/|-|\?|\(|\)", comment)


  
  sample_id = "/%(space_code)s/%(sample_code)s" % vars()
  
 


  # if 
  for name in plasmid_parent_code_split:
    sample = tr.getSample(sample_id)
    sample_for_update = tr.makeSampleMutable(sample)   
    if re.search ("pKW ", name):
      plasmid_parent_id = "/MATERIALS/"+name.replace("pKW ","PKW").strip()
      if not parents_code_list ==['']:
        parents_code_list.append(plasmid_parent_id)
        print "P1", parents_code_list
        sample_for_update.setParentSampleIdentifiers(parents_code_list)
      else:
        sample_for_update.setParentSampleIdentifiers([plasmid_parent_id])        
    elif re.search ("pKW", name): 
      plasmid_parent_id = "/MATERIALS/"+name.replace("pKW","PKW").strip()
      if not parents_code_list ==['']:
        parents_code_list.append(plasmid_parent_id)
        print "P2", parents_code_list
        sample_for_update.setParentSampleIdentifiers(parents_code_list)
      else:
        sample_for_update.setParentSampleIdentifiers([plasmid_parent_id])        
    

def register_samples_in_openbis(tr, data_rows):
  for sample_dict in data_rows:
    update_sample_with_parents(tr, sample_dict)


def process(tr):
  data_rows = parse_incoming(tr)
  register_samples_in_openbis(tr, data_rows)