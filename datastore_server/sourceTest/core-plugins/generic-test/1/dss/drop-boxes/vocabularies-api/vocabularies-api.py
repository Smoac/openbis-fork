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
    
    sample = transaction.createNewSample("/VOC/CELL_PLATE", "CELL_PLATE")
    sample.setPropertyValue("ORGANISM", "RAT")