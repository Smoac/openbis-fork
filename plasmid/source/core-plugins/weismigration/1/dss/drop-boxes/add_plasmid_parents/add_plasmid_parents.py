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
  plasmid_list=[]
  plasmid_parent_code = sample_dict['Parent plasmid']
  plasmid_parent_code_split = re.split("/|\+|-|\?|\(|\)", plasmid_parent_code)
  print sample_code, plasmid_parent_code_split
 


  
  sample_id = "/%(space_code)s/%(sample_code)s" % vars()

  for name in plasmid_parent_code_split:
    sample = tr.getSample(sample_id)
    sample_for_update = tr.makeSampleMutable(sample)
    if sample_id == "/MATERIALS/PKW3386":
        sample_for_update.setParentSampleIdentifiers(['/MATERIALS/PKW3363', '/MATERIALS/PKW3355'])
    if not sample_id =="/MATERIALS/PKW581" and not sample_id == "/MATERIALS/PKW3386" and not sample_id == "/MATERIALS/PKW3158":   
      if re.search ("pKW ", name):
        plasmid_parent_id = "/MATERIALS/"+name.replace("pKW ","PKW").strip()
        print "P1 : ", plasmid_parent_id
        if not parents_code_list ==['']:
            parents_code_list.append(plasmid_parent_id)
            print "P1 : ", parents_code_list
            sample_for_update.setParentSampleIdentifiers(parents_code_list)
        else:
            plasmid_list.append(plasmid_parent_id)
            print "P1 : ", plasmid_list
            sample_for_update.setParentSampleIdentifiers(plasmid_list)           
      elif re.search ("pKW467 pRS CRM1", name): 
        plasmid_parent_id = "/MATERIALS/"+name.replace("pKW467 pRS CRM1","PKW467").strip()
        print "P2 : ", plasmid_parent_id
        if not parents_code_list ==['']:
            parents_code_list.append(plasmid_parent_id)
            print "P2 : ", parents_code_list
            sample_for_update.setParentSampleIdentifiers(parents_code_list)       
        else:
            plasmid_list.append(plasmid_parent_id)
            print "P2 : ", plasmid_list
            sample_for_update.setParentSampleIdentifiers(plasmid_list)      
      elif re.search ("pKW468 pRS CRM1", name): 
        plasmid_parent_id = "/MATERIALS/"+name.replace("pKW468 pRS CRM1","PKW468").strip()
        print "P2 : ", plasmid_parent_id
        if not parents_code_list ==['']:
            parents_code_list.append(plasmid_parent_id)
            print "P2 : ", parents_code_list
            sample_for_update.setParentSampleIdentifiers(parents_code_list)       
        else:
            plasmid_list.append(plasmid_parent_id)
            print "P2 : ", plasmid_list
            sample_for_update.setParentSampleIdentifiers(plasmid_list)      
      elif re.search ("HA pKW2682", name): 
        plasmid_parent_id = "/MATERIALS/"+name.replace("HA pKW2682","PKW2682").strip()
        print "P2 : ", plasmid_parent_id
        if not parents_code_list ==['']:
            parents_code_list.append(plasmid_parent_id)
            print "P2 : ", parents_code_list
            sample_for_update.setParentSampleIdentifiers(parents_code_list)       
        else:
            plasmid_list.append(plasmid_parent_id)
            print "P2 : ", plasmid_list
            sample_for_update.setParentSampleIdentifiers(plasmid_list)  
      elif re.search ("pKW639 pCFP", name): 
        plasmid_parent_id = "/MATERIALS/"+name.replace("pKW639 pCFP","PKW639").strip()
        print "P2 : ", plasmid_parent_id
        if not parents_code_list ==['']:
            parents_code_list.append(plasmid_parent_id)
            print "P2 : ", parents_code_list
            sample_for_update.setParentSampleIdentifiers(parents_code_list)       
        else:
            plasmid_list.append(plasmid_parent_id)
            print "P2 : ", plasmid_list
            sample_for_update.setParentSampleIdentifiers(plasmid_list)
      elif re.search ("pKW001", name): 
        plasmid_parent_id = "/MATERIALS/"+name.replace("pKW001","PKW1").strip()
        if not parents_code_list ==['']:
            parents_code_list.append(plasmid_parent_id)
            print "P3 : ", parents_code_list
            sample_for_update.setParentSampleIdentifiers(parents_code_list)
        else:
            plasmid_list.append(plasmid_parent_id)
            sample_for_update.setParentSampleIdentifiers(plasmid_list)              
      elif re.search ("PKW001", name): 
        plasmid_parent_id = "/MATERIALS/"+name.replace("PKW001","PKW1").strip()
        print "P3 : ", plasmid_parent_id
        if not parents_code_list ==['']:
            parents_code_list.append(plasmid_parent_id)
            print "P3 : ", parents_code_list
            sample_for_update.setParentSampleIdentifiers(parents_code_list)
        else:
            plasmid_list.append(plasmid_parent_id)
            print "P3 : ", plasmid_list
            sample_for_update.setParentSampleIdentifiers(plasmid_list)  
      elif re.search ("pKW", name): 
        plasmid_parent_id = "/MATERIALS/"+name.replace("pKW","PKW").strip()
        print "P2 : ", plasmid_parent_id
        if not parents_code_list ==['']:
            parents_code_list.append(plasmid_parent_id)
            print "P2 : ", parents_code_list
            sample_for_update.setParentSampleIdentifiers(parents_code_list)       
        else:
            plasmid_list.append(plasmid_parent_id)
            print "P2 : ", plasmid_list
            sample_for_update.setParentSampleIdentifiers(plasmid_list)      
      elif re.search ("pkw", name): 
        plasmid_parent_id = "/MATERIALS/"+name.replace("pkw","PKW").strip()
        print "P2 : ", plasmid_parent_id
        if not parents_code_list ==['']:
            parents_code_list.append(plasmid_parent_id)
            print "P2 : ", parents_code_list
            sample_for_update.setParentSampleIdentifiers(parents_code_list)       
        else:
            plasmid_list.append(plasmid_parent_id)
            print "P2 : ", plasmid_list
            sample_for_update.setParentSampleIdentifiers(plasmid_list)      

  #=================================================================================================
  # for name in plasmid_parent_code_split:
  #   sample = tr.getSample(sample_id)
  #   sample_for_update = tr.makeSampleMutable(sample)
  #   if not sample_id =="/MATERIALS/PKW581":   
  #     if re.search ("pKW ", name):
  #       plasmid_parent_id = "/MATERIALS/"+name.replace("pKW ","PKW").strip()
  #       if not parents_code_list ==['']:
  #         parents_code_list.append(plasmid_parent_id)
  #         print "P1.1", parents_code_list
  #         sample_for_update.setParentSampleIdentifiers(parents_code_list)
  #       else:
  #         print "P1.2", plasmid_parent_id
  #         sample_for_update.setParentSampleIdentifiers([plasmid_parent_id])        
  #     elif re.search ("pKW", name): 
  #       plasmid_parent_id = "/MATERIALS/"+name.replace("pKW","PKW").strip()
  #       if not parents_code_list ==['']:
  #         parents_code_list.append(plasmid_parent_id)
  #         print "P2.1", parents_code_list
  #         sample_for_update.setParentSampleIdentifiers(parents_code_list)
  #       else:
  #         print "P2.2", plasmid_parent_id  
  #         sample_for_update.setParentSampleIdentifiers([plasmid_parent_id])            
  #     elif re.search ("pKW001", name): 
  #       plasmid_parent_id = "/MATERIALS/"+name.replace("pKW001","PKW1").strip()
  #       if not parents_code_list ==['']:
  #         parents_code_list.append(plasmid_parent_id)
  #         print "P3.1", parents_code_list
  #         sample_for_update.setParentSampleIdentifiers(parents_code_list)        
  #       else:
  #         print "P3.2", plasmid_parent_id  
  #         sample_for_update.setParentSampleIdentifiers([plasmid_parent_id])        
  #   
  #=================================================================================================

def register_samples_in_openbis(tr, data_rows):
  for sample_dict in data_rows:
    update_sample_with_parents(tr, sample_dict)


def process(tr):
  data_rows = parse_incoming(tr)
  register_samples_in_openbis(tr, data_rows)