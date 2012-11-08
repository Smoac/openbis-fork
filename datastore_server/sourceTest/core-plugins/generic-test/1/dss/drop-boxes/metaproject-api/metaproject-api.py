SPACE_CODE = "META"
PROJECT_ID = "/META/META"
EXPERIMENT_ID = "/META/META/META"

def create_project_and_experiment(transaction):
    space = transaction.createNewSpace(SPACE_CODE, None)
    space.setDescription("A demo space")
    project = transaction.createNewProject(PROJECT_ID)
    project.setDescription("A demo project")
    exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
    exp.setPropertyValue("DESCRIPTION", "A sample experiment")
    return exp
    
def process(transaction):
  create_project_and_experiment(transaction)

  existing_metaproject = transaction.getMetaproject("TEST_METAPROJECTS", "test")
  copy_metaproject = transaction.createNewMetaproject("COPY_TEST_METAPROJCTS", existing_metaproject.getDescription(), "test")
  
  print existing_metaproject.getDescription()
  
  # existing_attached_sample = transaction.getSample("TEST-SPACE/EV-TEST")
  
  new_metaproject = transaction.createNewMetaproject("TEST_META", "description", "test")
  
  sample = transaction.createNewSample("/META/METASAMPLE", "NORMAL")
  new_metaproject.addEntity(sample)
