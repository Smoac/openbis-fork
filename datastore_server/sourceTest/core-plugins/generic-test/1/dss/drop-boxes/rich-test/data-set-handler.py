SPACE_CODE = "RICH_SPACE"
PROJECT_ID = "/RICH_SPACE/RICH_PROJECT"
EXPERIMENT_ID = "/RICH_SPACE/RICH_PROJECT/RICH_EXPERIMENT"

def create_space_if_needed(transaction):
    space = transaction.getSpace(SPACE_CODE)
    if None == space:
        space = transaction.createNewSpace(SPACE_CODE, None)
        space.setDescription("A demo space")

def create_project_if_needed(transaction):
    project = transaction.getProject(PROJECT_ID)
    if None == project:
        create_space_if_needed(transaction)
        project = transaction.createNewProject(PROJECT_ID)
        project.setDescription("A demo project")

def create_experiment_if_needed(transaction):
    exp = transaction.getExperiment(EXPERIMENT_ID)
    if None == exp:
        create_project_if_needed(transaction)
        print 'Creating new experiment : ' + EXPERIMENT_ID
        exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
        exp.setPropertyValue("DESCRIPTION", "A sample experiment")

    return exp

def process(transaction):
    experiment = create_experiment_if_needed(transaction)
    link = transaction.createNewDataSet("LINK_TYPE", "FR_LINK_CODE")
    link.setExperiment(experiment)
    link.setExternalCode("EX_CODE")
    link.setExternalDataManagementSystem(transaction.getExternalDataManagementSystem("DMS_1"))

