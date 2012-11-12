SPACE_CODE = "VOC"
PROJECT_ID = "/VOC/VOC"
EXPERIMENT_ID = "/VOC/VOC/VOC"

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
    
    # test simple existing vocabulary
    sample = transaction.createNewSample("/VOC/CELL_PLATE", "CELL_PLATE")
    sample.setPropertyValue("ORGANISM", "RAT")
    
    # test iteration over vocabulary terms
    vocabulary = transaction.getVocabulary("TEST_VOCABULARY")
    
    for term in vocabulary.getTerms():
        sample = transaction.createNewSample("/VOC/NORMAL_%s" % term.getCode(), "NORMAL")
        sample.setPropertyValue("TEST_VOCABULARY", term.getCode())
    
    # test modifying vocabulary
    vocabulary = transaction.getVocabularyForUpdate("TEST_VOCABULARY")
    vocabulary.setDescription("modified description")
    vocabulary.setManagedInternally(True)
    vocabulary.setInternalNamespace(True)
    vocabulary.setChosenFromList(False)
    vocabulary.setUrlTemplate("localuri")
    
    vocabularyTerm = transaction.createNewVocabularyTerm()
    vocabularyTerm.setCode("NEW_TERM")
    vocabularyTerm.setOrdinal(3)
    vocabularyTerm.setDescription("new description")
    vocabularyTerm.setLabel("new label")
    
    vocabulary.addTerm(vocabularyTerm)
    
    sample = transaction.createNewSample("/VOC/NORMAL_NEW_TERM", "NORMAL")
    sample.setPropertyValue("TEST_VOCABULARY", vocabularyTerm.getCode())