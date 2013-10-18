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
    if(expectedContentString == None and actualContentStream <> None):
        actualContentString = String(IOUtils.toByteArray(actualContentStream));
        raise Exception('Attachment content should be: None but was: "' + str(actualContentString) + '"')
    if(expectedContentString <> None and actualContentStream == None):
        raise Exception('Attachment content should contain: "' + str(expectedContentString) + '" but was: None')
    if(expectedContentString <> None and actualContentStream <> None):
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

def testProjectWithoutAttachments(transaction):
    PROJECT_IDENTIFIER = "/CISD/DEFAULT";

    attachments = listProjectAttachments(transaction, PROJECT_IDENTIFIER)
    assertAttachmentCount(attachments, 0)
    
    content = getProjectAttachmentContent(transaction, PROJECT_IDENTIFIER, "not-existing-attachment", None);
    assertAttachmentContentContains(content, None);

def testProjectWithAttachments(transaction):
    PROJECT_IDENTIFIER = "/CISD/NEMO";

    attachments = listProjectAttachments(transaction, PROJECT_IDENTIFIER)
    assertAttachmentCount(attachments, 1)
    assertAttachment(attachments[0], "projectDescription.txt", "The Project", "All about it.", 1);

    content = getProjectAttachmentContent(transaction, PROJECT_IDENTIFIER, "projectDescription.txt", None);
    assertAttachmentContentContains(content, "3VCP1");
    
    content2 = getProjectAttachmentContent(transaction, PROJECT_IDENTIFIER, "not-existing-attachment", None);
    assertAttachmentContentContains(content2, None);

def testExperimentWithoutAttachments(transaction):
    EXPERIMENT_IDENTIFIER = "/CISD/NEMO/EXP10";

    attachments = listExperimentAttachments(transaction, EXPERIMENT_IDENTIFIER)
    assertAttachmentCount(attachments, 0)

    content = getExperimentAttachmentContent(transaction, EXPERIMENT_IDENTIFIER, "not-existing-attachment", 2);
    assertAttachmentContentContains(content, None);

def testExperimentWithAttachments(transaction):
    EXPERIMENT_IDENTIFIER = "/CISD/NEMO/EXP1";

    attachments = listExperimentAttachments(transaction, EXPERIMENT_IDENTIFIER)
    assertAttachmentCount(attachments, 4)
    assertAttachment(attachments[0], "exampleExperiments.txt", None, None, 1)
    assertAttachment(attachments[1], "exampleExperiments.txt", None, None, 2)
    assertAttachment(attachments[2], "exampleExperiments.txt", None, None, 3)
    assertAttachment(attachments[3], "exampleExperiments.txt", None, None, 4)
    
    content = getExperimentAttachmentContent(transaction, EXPERIMENT_IDENTIFIER, "exampleExperiments.txt", 2);
    assertAttachmentContentContains(content, "koko");

    content2 = getExperimentAttachmentContent(transaction, EXPERIMENT_IDENTIFIER, "not-existing-attachment", 2);
    assertAttachmentContentContains(content2, None);

def testSampleWithoutAttachments(transaction):
    SAMPLE_IDENTIFIER = "/CISD/3VCP5";

    attachments = listSampleAttachments(transaction, SAMPLE_IDENTIFIER)
    assertAttachmentCount(attachments, 0)

    content = getSampleAttachmentContent(transaction, SAMPLE_IDENTIFIER, "not-existing-attachment", None);
    assertAttachmentContentContains(content, None);

def testSampleWithAttachments(transaction):
    SAMPLE_IDENTIFIER = "/CISD/3VCP6";

    attachments = listSampleAttachments(transaction, SAMPLE_IDENTIFIER)
    assertAttachmentCount(attachments, 1)
    assertAttachment(attachments[0], "sampleHistory.txt", None, None, 1)

    content = getSampleAttachmentContent(transaction, SAMPLE_IDENTIFIER, "sampleHistory.txt", None);
    assertAttachmentContentContains(content, "kot")
    
    content2 = getSampleAttachmentContent(transaction, SAMPLE_IDENTIFIER, "not-existing-attachment", None);
    assertAttachmentContentContains(content2, None);

def process(transaction):

    testProjectWithoutAttachments(transaction);
    testProjectWithAttachments(transaction);

    testExperimentWithoutAttachments(transaction);
    testExperimentWithAttachments(transaction);

    testSampleWithoutAttachments(transaction);
    testSampleWithAttachments(transaction);
