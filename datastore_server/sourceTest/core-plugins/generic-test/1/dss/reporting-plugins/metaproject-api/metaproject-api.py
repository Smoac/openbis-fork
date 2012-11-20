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
    
def process(transaction, parameters, tableBuilder):
  create_project_and_experiment(transaction)

  existing_metaproject = transaction.getMetaproject("TEST_METAPROJECTS", "test")
  copy_metaproject = transaction.createNewMetaproject("COPY_TEST_METAPROJCTS", existing_metaproject.getDescription(), "test")
  
  new_metaproject = transaction.createNewMetaproject("TEST_META", "description", "test")
  
  sample = transaction.createNewSample("/META/METASAMPLE", "NORMAL")
  new_metaproject.addEntity(sample)


  tableBuilder.addHeader("VALUE")  

  def addRow(value):
    row = tableBuilder.addRow()
    row.setCell("VALUE", value)

  transaction.setUserId("test")
  ms = transaction.getSearchService().listMetaprojects()
  for m in ms:
     addRow("%s %s %s" % (m.getName(), m.getDescription(), m.getOwnerId()))
  
  ms = transaction.getSearchServiceFilteredForUser("test_role").listMetaprojects()
  for m in ms:
     addRow("%s %s %s" % (m.getName(), m.getDescription(), m.getOwnerId()))