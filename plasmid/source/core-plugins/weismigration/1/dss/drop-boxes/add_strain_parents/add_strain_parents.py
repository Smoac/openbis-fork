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
  sample_id = "/MATERIALS/" + sample_dict['Code']

  yeast_parent_code = sample_dict['derived from']
  yeast_parent_code_split = re.split("and|,|\&|\+|x|/|-|\?|\(|\)", yeast_parent_code)
  print sample_id, yeast_parent_code_split
  parents_code_list=[]
  #e.split("/|-|\?|\(|\)" 
  # insert=sample_dict['Insert']
  # insert_split=re.split("/|-|\?|\(|\|+)", insert)
  # comment=sample_dict['Comment']
  # comment_split = re.split("/|-|\?|\(|\)", comment)


  
  
 


  # if 
  for name in yeast_parent_code_split:
    if not sample_id=="/MATERIALS/KWY5055" and not sample_id=="/MATERIALS/KWY5542" and not sample_id=="/MATERIALS/KWY4260":
        sample = tr.getSample(sample_id)
        sample_for_update = tr.makeSampleMutable(sample)
        if not re.search("Roy Parker", name) and not re.search("ku",name):   
            if re.search ("kwy ", name):
                yeast_parent_id = "/MATERIALS/"+name.replace("kwy ","KWY").strip()
                parents_code_list.append(yeast_parent_id)
                print "PARENTS: ", parents_code_list
                sample_for_update.setParentSampleIdentifiers(parents_code_list)
            elif re.search ("Kwy ", name):
                yeast_parent_id = "/MATERIALS/"+name.replace("Kwy ","KWY").strip()
                parents_code_list.append(yeast_parent_id)
                print "PARENTS: ", parents_code_list
                sample_for_update.setParentSampleIdentifiers(parents_code_list)
            elif re.search ("KWY ", name): 
                yeast_parent_id = "/MATERIALS/"+name.replace("KWY ","KWY").strip()
                parents_code_list.append(yeast_parent_id)
                print "PARENTS: ", parents_code_list
                sample_for_update.setParentSampleIdentifiers(parents_code_list)        
            elif re.search ("pKW", name):
                yeast_parent_id = "/MATERIALS/"+name.replace("pKW","PKW").strip()
                parents_code_list.append(yeast_parent_id)
                print "PARENTS: ", parents_code_list
                sample_for_update.setParentSampleIdentifiers(parents_code_list)               
            elif re.search ("PKW", name):
                yeast_parent_id = "/MATERIALS/"+name
                parents_code_list.append(yeast_parent_id)
                print "PARENTS: ", parents_code_list
                sample_for_update.setParentSampleIdentifiers(parents_code_list)   
            elif re.search ("kwy", name):
                yeast_parent_id = "/MATERIALS/"+name.replace("kwy","KWY").strip()
                parents_code_list.append(yeast_parent_id)
                print "PARENTS: ", parents_code_list
                sample_for_update.setParentSampleIdentifiers(parents_code_list)           
            elif re.match ("KW\d+", name): 
                yeast_parent_id = "/MATERIALS/"+name.replace("KW","KWY").strip()
                parents_code_list.append(yeast_parent_id)
                print "PARENTS: ", parents_code_list
                sample_for_update.setParentSampleIdentifiers(parents_code_list)             
#            elif re.search ("k", name): 
#                yeast_parent_id = "/MATERIALS/"+name.replace("k","KWY").strip()
#                parents_code_list.append(yeast_parent_id)

                sample_for_update.setParentSampleIdentifiers(parents_code_list)        
            elif re.search ("KWYY", name): 
                yeast_parent_id = "/MATERIALS/"+name.replace("KWYY","KWY").strip()
                parents_code_list.append(yeast_parent_id)
                print "PARENTS: ", parents_code_list
                sample_for_update.setParentSampleIdentifiers(parents_code_list)        
            elif re.search ("KWy", name): 
                yeast_parent_id = "/MATERIALS/"+name.replace("KWy","KWY").strip()
                parents_code_list.append(yeast_parent_id)
                print "PARENTS: ", parents_code_list
                sample_for_update.setParentSampleIdentifiers(parents_code_list)   
            elif re.search ("KWY", name): 
                yeast_parent_id = "/MATERIALS/"+name.strip()
                if not yeast_parent_id =="/MATERIALS/KWY":
                    parents_code_list.append(yeast_parent_id)
                    print "PARENTS: ", parents_code_list
                    sample_for_update.setParentSampleIdentifiers(parents_code_list)                        

        

def register_samples_in_openbis(tr, data_rows):
  for sample_dict in data_rows:
    update_sample_with_parents(tr, sample_dict)


def process(tr):
  data_rows = parse_incoming(tr)
  register_samples_in_openbis(tr, data_rows)