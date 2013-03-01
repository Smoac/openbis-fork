import os, glob, re, csv, time, shutil
from java.io import File
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageContainerDataConfig, ChannelColor
from loci.formats import ImageReader



from time import *
from datetime import *

'''
Dropbox script for uploading a .lsm/ .czi file. Images contained in the file are displayed in openBIS and metadate is extracted from the lsm file and 
stored in a file called "metadata.txt" uploaded in the dataset. The lsm or czi file needs to be put in a directory inside the incoming folder. The name of this directory is then taken as sample name for openBIS.
'''


print '###################################'
tz=localtime()[3]-gmtime()[3]
d=datetime.now()
print d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

class BioFormatsHelper(ImageReader):
  def tryToCreateReaderForFile(fileName):
    for reader in ImageReader().getReaders():
      if reader.isThisType(fileName):
        return reader
    return None

def process(transaction):
	
	incoming = transaction.getIncoming()


	def readCzi(incoming):
		filename_list = {}
		for cziFile in glob.glob(os.path.join(incoming, '*.czi')):
			objective =''
			scaling = None
			(dir, file) = os.path.split(cziFile)
			(filename, extension) = os.path.splitext(file)
			helper = BioFormatsHelper()
			reader = helper.getReader(cziFile)
			reader.setId(cziFile)
  			
  			globalMetadata = str(reader.getGlobalMetadata().toString())
  			
			list_globalMetadata = re.split(r"[,]",globalMetadata)
			metadataFile = dir+"/" + filename + "-metadata.txt"
			f = open(metadataFile, "a")
			for item in list_globalMetadata:
  				f.write(item)
  				f.write('\n')
  				if re.search ("=", item):
  					item_total = re.split(r"[=]", item)
  					item_label = item_total[0]
  					item_value = item_total[1]
  					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock AcquisitionModeSetup Objective 0", item_label):
  						objective = item_value
						
  					if re.search ("Metadata Experiment ExperimentBlocks AcquisitionBlock AcquisitionModeSetup ScalingX 0", item_label):
  						scaling = item_value
 					
			f.close()
			reader.close();
			filename_list[filename] = { "objective" : objective, "scaling" : scaling}
			
			
		return filename_list
			   
	cziInfo = readCzi(incoming.getPath())
	
	directoryName = os.path.basename(incoming.getPath())

	names = re.split(r"[-]",directoryName)
	projectName = names[0] + "-IMG"
	experimentName = names[1]

	
	for name in cziInfo.keys():
		print "name", name
		sampleIdentifier = "/PANTAZIS_GROUP/" + name
		sample = transaction.createNewSample(sampleIdentifier, "MICROSCOPY_IMG")
		experiment = "/PANTAZIS_GROUP/" + projectName + "/" + experimentName  
		exp = transaction.getExperiment(experiment)
		if not exp:
			exp = transaction.createNewExperiment(experiment, 'IMAGING')

		sample.setExperiment(exp)
		sample.setPropertyValue("OBJECTIVE", cziInfo[name]["objective"])
		sample.setPropertyValue("SCALING", cziInfo[name]["scaling"])

		imageDataset = SimpleImageContainerDataConfig()
		imageDataset.setPlate("PANTAZIS_GROUP", name)
		imageDataset.setMicroscopyData(True)
		imageDataset.setDataSetType("MICROSCOPY_IMG")
		imageDataset.setRecognizedImageExtensions(['lif', 'dv', 'tif', 'tiff','nd2', 'lsm', 'czi'])
		imageDataset.setImageLibrary("BioFormats")
		imageDataset.setMeasuredData(True)
		
 		sampleNamePath = incoming.getPath() + '/' + name  + '.czi'
 		metadataPath = incoming.getPath() + '/' + name  + '-metadata.txt'
 		dataSet = transaction.createNewImageDataSet(imageDataset, File(sampleNamePath))
		
		transaction.moveFile(sampleNamePath, dataSet)
		transaction.moveFile(metadataPath, dataSet)

# 		transaction.moveFile(sampleNamePath, dataSet)


# 	def moveFiles(incoming):
# 		for cziFile in glob.glob(os.path.join(incoming, '*.czi')):
# 			(incoming, file) = os.path.split(cziFile)
# 			(filename, extension) = os.path.splitext(file)
# 			newDir = incoming + '/' + filename
# 			print "newdir", newDir
# 			if not os.path.exists(newDir):
# 				os.makedirs(newDir)
# 			shutil.move(incoming + '/' + file, newDir)
# 		for txtFile in glob.glob(os.path.join(incoming, '*.txt')):
# 			print "textfile", txtFile
# 			(incoming, file) = os.path.split(txtFile)
# 			(filename, extension) = os.path.splitext(file)		
# 			shutil.move(incoming + '/' + file, newDir)
# 			
# 				
# 	moveFiles(incoming.getPath())
 

	
	
	
  		
  	
# 		
# 	 
	  
  
  
  
  
  
  
  
  
  
