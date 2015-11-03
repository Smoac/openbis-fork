import os
import time
from ch.systemsx.cisd.openbis.dss.etl.dto.api import SimpleImageDataConfig
from ch.systemsx.cisd.openbis.dss.etl.dto.api import ImageMetadata
from ch.systemsx.cisd.openbis.dss.etl.dto.api import ChannelColor
from BioFormatsProcessor import BioFormatsProcessor
from MicroscopySingleDatasetConfig import MicroscopySingleDatasetConfig

SPACE_CODE = "TYPHOON"
PROJECT_CODE = "TYPHOON"
PROJECT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s" % vars()
EXPERIMENT_CODE = "TYPHOON_INCOMING"
EXPERIMENT_ID = "/%(SPACE_CODE)s/%(PROJECT_CODE)s/%(EXPERIMENT_CODE)s" % vars()

#SAMPLE_CODE = "SERIES1"
#SAMPLE_ID = "/%(SPACE_CODE)s/%(SAMPLE_CODE)s" % vars()
#path_name, fileExtension = os.path.splitext(incoming.getPath())
#SAMPLE_CODE = path_name
#SAMPLE_ID = "/%(SPACE_CODE)s/%(SAMPLE_CODE)s" % vars()


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
    """ Get the specified experiment or register it if necessary """
    exp = transaction.getExperiment(EXPERIMENT_ID)
    if None == exp:
        create_project_if_needed(transaction)
        print 'Creating new experiment : ' + EXPERIMENT_ID
        exp = transaction.createNewExperiment(EXPERIMENT_ID, 'TYPHOON')
        
    return exp
    
def create_sample_if_needed(transaction):
    """ Get the specified sample or register it if necessary """

    incoming = transaction.getIncoming()
    path_name = os.path.basename(incoming.getPath())
    SAMPLE_CODE = os.path.splitext(path_name)[0]
    SPACE_CODE = "TYPHOON"
    MILLIS = int(round(time.time() * 1000))
    SAMPLE_ID = "/%(SPACE_CODE)s/%(SAMPLE_CODE)s-%(MILLIS)s" % vars()
	
    print SAMPLE_ID

    samp = transaction.getSample(SAMPLE_ID)

    if None == samp:
        exp = create_experiment_if_needed(transaction)
        samp = transaction.createNewSample(SAMPLE_ID, 'TYPHOON_IMG')
        samp.setExperiment(exp)
        
    return samp

     
def printMetaData(metaDataReader):
    num_series = metaDataReader.getNumSeries()

    print "Meta data: number of series: %s" % num_series
    for i in range(num_series):
        metadata = metaDataReader.getMetadata()[i]
        print "  meta data for series %s:" % i
        for k in metadata:
            print "    %s = %s" % (k, metadata[k]) 


def process(transaction): 
    incoming = transaction.getIncoming()
    metaDataReader = BioFormatsProcessor(incoming)
    metaDataReader.extractMetadata()
    printMetaData(metaDataReader)
    imageDatasetConfig = MicroscopySingleDatasetConfig(metaDataReader)
    sample = create_sample_if_needed(transaction)
    containerDataset = transaction.createNewImageDataSet(imageDatasetConfig, incoming)
    containerDataset.setSample(sample)
    thumbnailDatasets = containerDataset.getThumbnailDatasets()
    for thumbnailDataset in thumbnailDatasets:
        if thumbnailDataset.getDataSetType() == "ELN_PREVIEW":
            thumbnailDataset.getRegistrationDetails().getDataSetInformation().setLinkSample(True)
    transaction.moveFile(incoming.getPath(), containerDataset)
