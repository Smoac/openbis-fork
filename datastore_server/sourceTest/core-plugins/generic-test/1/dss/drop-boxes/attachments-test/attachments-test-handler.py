from java.lang import String
from org.apache.commons.io import IOUtils

def listProjectAttachments(transaction, projectIdentifier):
    project = transaction.getProject(projectIdentifier)    
    return transaction.listProjectAttachments(project);

def listExperimentAttachments(transaction, experimentIdentifier):
    experiment = transaction.getExperiment(experimentIdentifier)    
    return transaction.listExperimentAttachments(experiment);

def listSampleAttachments(transaction, sampleIdentifier):
    sample = transaction.getSample(sampleIdentifier)    
    return transaction.listSampleAttachments(sample);

def getProjectAttachmentContent(transaction, projectIdentifier, name, version):
    project = transaction.getProject(projectIdentifier)
    return transaction.getProjectAttachmentContent(project, name, version)

def getExperimentAttachmentContent(transaction, experimentIdentifier, name, version):
    experiment = transaction.getExperiment(experimentIdentifier)
    return transaction.getExperimentAttachmentContent(experiment, name, version)

def getSampleAttachmentContent(transaction, sampleIdentifier, name, version):
    sample = transaction.getSample(sampleIdentifier)
    return transaction.getSampleAttachmentContent(sample, name, version)

def assertAttachmentContentContains(actualContentStream, expectedContentString):
    actualContentString = String(IOUtils.toByteArray(actualContentStream));
    if(str(expectedContentString) not in str(actualContentString)):
        raise Exception('Attachment content should contain: "' + str(expectedContentString) + '" but was: "' + str(actualContentString) + '"')

def assertAttachmentCount(attachmentList, expectedCount):
    if(expectedCount == 0 and attachmentList <> None):
        raise Exception('Attachment list should be: None but was: ' + str(attachmentList))
    if(expectedCount > 0 and attachmentList == None):
        raise Exception('Attachment list length should be: ' + str(expectedCount) + ' but the list was: None')
    if(expectedCount > 0 and len(attachmentList) <> expectedCount):
        raise Exception('Attachment list length should be: ' + str(expectedCount) + ' but it was: ' + str(len(attachmentList)))

def assertAttachment(attachment, fileName, title, description, version):
    if(attachment.getFileName() <> fileName):
        raise Exception('Attachment file name should be: "' + str(fileName) + '" but was: "' + str(attachment.getFileName()) + '"')
    if(attachment.getTitle() <> title):
        raise Exception('Attachment title should be: "' + str(title) + '" but was: "' + str(attachment.getTitle()) + '"')
    if(attachment.getDescription() <> description):
        raise Exception('Attachment description should be: "' + str(description) + '" but was: "' + str(attachment.getDescription()) + '"')
    if(attachment.getVersion() <> version):
        raise Exception('Attachment version should be: "' + str(version) + '" but was: "' + str(attachment.getVersion()) + '"')

def process(transaction):
    
    PROJECT_IDENTIFIER = "/CISD/NEMO";
    EXPERIMENT_IDENTIFIER = "/CISD/NEMO/EXP1";
    SAMPLE_IDENTIFIER = "/CISD/3VCP6";
    
    # project
    projectAttachments = listProjectAttachments(transaction, PROJECT_IDENTIFIER)
    assertAttachmentCount(projectAttachments, 1)
    assertAttachment(projectAttachments[0], "projectDescription.txt", "The Project", "All about it.", 1);
    
    projectAttachmentContent = getProjectAttachmentContent(transaction, PROJECT_IDENTIFIER, "projectDescription.txt", None);
    assertAttachmentContentContains(projectAttachmentContent, "3VCP1");

    # experiment
    experimentAttachments = listExperimentAttachments(transaction, EXPERIMENT_IDENTIFIER)
    assertAttachmentCount(experimentAttachments, 4)
    assertAttachment(experimentAttachments[0], "exampleExperiments.txt", None, None, 1)
    assertAttachment(experimentAttachments[1], "exampleExperiments.txt", None, None, 2)
    assertAttachment(experimentAttachments[2], "exampleExperiments.txt", None, None, 3)
    assertAttachment(experimentAttachments[3], "exampleExperiments.txt", None, None, 4)
    
    experimentAttachmentContent = getExperimentAttachmentContent(transaction, EXPERIMENT_IDENTIFIER, "exampleExperiments.txt", 2);
    assertAttachmentContentContains(experimentAttachmentContent, "koko");

    # sample
    sampleAttachments = listSampleAttachments(transaction, SAMPLE_IDENTIFIER)
    assertAttachmentCount(sampleAttachments, 1)
    assertAttachment(sampleAttachments[0], "sampleHistory.txt", None, None, 1)

    sampleAttachmentContent = getSampleAttachmentContent(transaction, SAMPLE_IDENTIFIER, "sampleHistory.txt", None);
    assertAttachmentContentContains(sampleAttachmentContent, "kot")
