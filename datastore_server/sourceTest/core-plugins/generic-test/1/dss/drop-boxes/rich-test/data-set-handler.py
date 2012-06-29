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


def createMaterials(transaction):
    for x in range(0,100):
        mat = transaction.createNewMaterial("RM_%d" % x, "GENE")
        mat.setPropertyValue("GENE_SYMBOL", "RM_%d_S" %x)

def process(transaction):
    # create experiment
    experiment = create_experiment_if_needed(transaction)
    
    # register link data set
    link = transaction.createNewDataSet("LINK_TYPE", "FR_LINK_CODE")
    link.setExperiment(experiment)
    link.setExternalCode("EX_CODE")
    link.setExternalDataManagementSystem(transaction.getExternalDataManagementSystem("DMS_1"))

    # register many materials
    createMaterials(transaction)
