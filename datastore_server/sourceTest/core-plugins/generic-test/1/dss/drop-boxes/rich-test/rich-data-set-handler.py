from ch.systemsx.cisd.common.mail import EMailAddress
from org.apache.commons.io import IOUtils
from java.lang import String

SPACE_CODE = "RICH_SPACE"
PROJECT_ID = "/RICH_SPACE/RICH_PROJECT"
EXPERIMENT_ID = "/RICH_SPACE/RICH_PROJECT/RICH_EXPERIMENT"

# the hooks

def sendMail(context, subject, message):
    mailClient = context.getGlobalState().getMailClient();
    addressFrom = EMailAddress("example@example.com")
    addressTo = EMailAddress("rich_test_example@example.com", "example name")
    mailClient.sendEmailMessage(subject, message, None,
            addressFrom, addressTo) 

def post_metadata_registration(context):
    content = "post_metadata_registration rich %s " % context.getPersistentMap().get("email_text")
    sendMail(context, "Subject", content)

def add_attachment(entity, transaction):
    f = open("%s/%s" % (transaction.getIncoming().getPath(), "set1.txt"), 'r')
    entity.addAttachment("attachment.txt", 'Source Import File', 'Source Import File ', f.read())
    f.close()

def get_project_attachment(transaction, projectIdentifier, name, version):
    project = transaction.getProject(projectIdentifier)
    return transaction.getProjectAttachmentContent(project, name, version)

def get_experiment_attachment(transaction, experimentIdentifier, name, version):
    experiment = transaction.getExperiment(experimentIdentifier)
    return transaction.getExperimentAttachmentContent(experiment, name, version)

def get_sample_attachment(transaction, sampleIdentifier, name, version):
    sample = transaction.getSample(sampleIdentifier)
    return transaction.getSampleAttachmentContent(sample, name, version)

def add_project_attachment(project, stream, name):
    content = IOUtils.toByteArray(stream);
    contentString = String(content);
    project.addAttachment(name, 'Title ' + name, contentString, content)
    stream.close();

def create_space(transaction):
    space = transaction.createNewSpace(SPACE_CODE, None)
    space.setDescription("A demo space")

def create_project(transaction):
    project = transaction.createNewProject(PROJECT_ID)
    project.setDescription("A demo project")
    add_attachment(project, transaction)
    return project
    

def create_experiment(transaction):
    exp = transaction.createNewExperiment(EXPERIMENT_ID, 'SIRNA_HCS')
    exp.setPropertyValue("DESCRIPTION", "A sample experiment")
    add_attachment(exp, transaction)
    return exp

def createMaterials(transaction):
    for x in range(0,60):
        mat = transaction.createNewMaterial("RM_%d" % x, "SLOW_GENE")
        mat.setPropertyValue("GENE_SYMBOL", "RM_%d_S" %x)

def createSamples(transaction):
    sample = transaction.createNewSample('/RICH_SPACE/SAMPLE123', 'DYNAMIC_PLATE')
    add_attachment(sample, transaction)

def updateMaterial(transaction):
    ma = transaction.getMaterialForUpdate("AD3", "VIRUS");
    ma.setPropertyValue("DESCRIPTION", "modified description");

def updateExperiment(transaction):
    ex = transaction.getExperimentForUpdate("/CISD/NEMO/EXP1")
    ex.setPropertyValue("DESCRIPTION", "modified experiment description")

def createBacterias(transaction):
    vocabulary = transaction.getSearchService().searchForVocabulary("ORGANISM")
    for term in vocabulary.getTerms():
        mat = transaction.createNewMaterial("BC_%s" % term.getCode(), "BACTERIUM")
        mat.setPropertyValue("DESCRIPTION", term.getDescription())
        mat.setPropertyValue("ORGANISM", term.getCode())
        
def process(transaction):
    # create experiment
    create_space(transaction)
    project = create_project(transaction)
    experiment = create_experiment(transaction)
    
    # register link data set
    link = transaction.createNewDataSet("LINK_TYPE", "FR_LINK_CODE")
    link.setExperiment(experiment)
    link.setExternalCode("EX_CODE")
    link.setExternalDataManagementSystem(transaction.getExternalDataManagementSystem("DMS_1"))

    # register many materials
    createMaterials(transaction)
    
    # update material
    updateMaterial(transaction) 

    # update existing experiment
    updateExperiment(transaction)

    # register samples
    createSamples(transaction)
    
    # read controlled vocabularies and create materials
    createBacterias(transaction)
    
    # get existing attachents and attach them to project
    projectAttachment = get_project_attachment(transaction, "/CISD/NEMO", "projectDescription.txt", None);
    add_project_attachment(project, projectAttachment, "test-attachment-project")
    
    experimentAttachment = get_experiment_attachment(transaction, "/CISD/NEMO/EXP1", "exampleExperiments.txt", 2);
    add_project_attachment(project, experimentAttachment, "test-attachment-experiment")

    sampleAttachment = get_sample_attachment(transaction, "/CISD/3VCP6", "sampleHistory.txt", None);
    add_project_attachment(project, sampleAttachment, "test-attachment-sample")
    
    transaction.getRegistrationContext().getPersistentMap().put("email_text", "rich_email_text")
