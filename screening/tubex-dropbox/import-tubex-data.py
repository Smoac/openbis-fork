#! /usr/bin/env python
"""
  Script for uploading image data and some other files.
  
"""

import os, glob, re, csv, time, shutil
from java.io import File
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageContainerDataConfig, ChannelColor
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from loci.formats import ImageReader




from time import *
from datetime import *


print "###################################################"
tz=localtime()[3]-gmtime()[3]
d=datetime.now()

print d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

fileList=[]
axisList=[]
neckList=[]
dateExpSampleList=[]
cellSampleList=[]

def process(transaction):
	incoming = transaction.getIncoming()
	# the name of the folder is the name of the experiment
	experimentName = os.path.basename(incoming.getPath())
	

	#Check if project and experiment already exist, if not create them	
	project = transaction.getProject("/TUBEX/LEGACY_DATA/")
	if not project:
		project = transaction.createNewProject("/TUBEX/LEGACY_DATA/")
	exp = transaction.getExperiment("/TUBEX/LEGACY_DATA/" + experimentName)
	if not exp:
 		exp = transaction.createNewExperiment("/TUBEX/LEGACY_DATA/" + experimentName, "CELL_EXPERIMENT")
	

	#get the name of the tif files
	def extract_file_start(incoming):
		for tif in glob.glob(os.path.join(incoming, '*.tif')):
			(incomingPath, file) = os.path.split(tif)
			(filename, extension) = os.path.splitext(file)
			
			fileList.append(filename)

		return fileList

	extract_file_start(incoming.getPath())




	#from the axis files, extract the part of the name that contains date, experiment and cell number
	def extract_axis_file_name(incoming):
		for axisData in glob.glob(os.path.join(incoming, 'AxisData/*')):
			(incomingPath, file) = os.path.split(axisData)
			(filename, extension) = os.path.splitext(file)
			nameParts = re.split("_", filename)
			axis = nameParts[1]+"_"+nameParts[2]
			
			axisList.append(axis)
		
		return axisList

	extract_axis_file_name(incoming.getPath())	

	#from the neck files, extract the part of the name that contains date, experiment and cell number
	def extract_neck_file_name(incoming):
		for neckData in glob.glob(os.path.join(incoming, 'NeckData/*')):
			(incomingPath, file) = os.path.split(neckData)
			(filename, extension) = os.path.splitext(file)
			nameParts = re.split("_", filename)
			neck = nameParts[1]+"_"+nameParts[2]
			
			neckList.append(neck)
		
		return neckList

	extract_neck_file_name(incoming.getPath())	

	#create a directory where to move all the .txt files 
	def create_dir(incoming):
		directory =  incoming + '/text_files'
		if not os.path.exists(directory):
	  		os.makedirs(directory)
	  	return directory
	

	#create a sample for each date and experiment number. Create a sample for each cell of each date and experiment number and set it as contained sample.
	# import the tif files as image dataset. Import the mtTracker files and the neck and axis files in a "TEXT_FILES" dataset, which is a child of the image dataset.
	def import_files(incoming):
		for item, axisFile, neckFile in zip(fileList, axisList, neckList):
			tifFile = item + ".tif"
	
			mtTrackerSphere = incoming + "/" +item + "_mtTracker_sphereCovMatrix.txt"
			shutil.move(mtTrackerSphere, create_dir(incoming))
			mtTrackerResults = incoming + "/" +item + "_mtTracker_results.txt"
			shutil.move(mtTrackerResults, create_dir(incoming))
			mtTrackerParams = incoming + "/" +item + "_mtTracker_params.txt"
			shutil.move(mtTrackerParams, create_dir(incoming))
			mtTrackerInitVal = incoming + "/" +item + "_mtTracker_initValues.txt"
			shutil.move(mtTrackerInitVal, create_dir(incoming))
			mtTrackerCovMatrix = incoming + "/" +item + "_mtTracker_covMatrix.txt"	
			shutil.move(mtTrackerCovMatrix, create_dir(incoming))
			

		  tokens = re.split('_', item)
			  	
	 	  dateExp = tokens[0]
	 	 	cellNum = tokens[1]
	 	 	day = dateExp[:2]
	 	  month = dateExp[2:4]
	 	 	year =  "20" + dateExp[4:6]
	 	  date = year + "-" + month + "-" + day + " 12:00:00 GMT+02:00" # the provided date is day-month-year it should be month-day-year
	 	 	 	  	
	
	 	  dateExpSample = transaction.createNewSample("/TUBEX/" + dateExp,'DATE_EXPERIMENT')
			dateExpSample.setExperiment(exp)
			dateExpSample.setPropertyValue("DATE", date)
			

			cellSample = transaction.createNewSample("/TUBEX/" +  dateExp + ":" + cellNum,'CELL_NUMBER' )
			cellSample.setExperiment(exp)
			cellSample.setContainer(dateExpSample)
			
	
		
			if (axisFile == item):
				axisFilePath = incoming + "/AxisData/Axis_"+ axisFile + ".txt"
				shutil.move(axisFilePath, create_dir(incoming))

			if (neckFile == item):
				neckFilePath = incoming + "/NeckData/Neck_"+ neckFile + ".txt"
				shutil.move(neckFilePath, create_dir(incoming))

			


			imageDataset = SimpleImageContainerDataConfig()
			imageDataset.setMicroscopyData(True)
			imageDataset.setDataSetType("MICROSCOPY_IMG")
			imageDataset.setRecognizedImageExtensions(['lif', 'dv', 'tif', 'tiff','nd2', 'lsm', 'czi'])
			imageDataset.setImageLibrary("BioFormats")
			imageDataset.setMeasuredData(True)


			
			sampleNamePath = incoming + '/' + tifFile

	 	 	dataSet = transaction.createNewImageDataSet(imageDataset, File(sampleNamePath))
			dataSet.setSample(cellSample)
			dataSetCode = dataSet.getDataSetCode()
			
			transaction.moveFile(sampleNamePath, dataSet)

			dataSetTXT = transaction.createNewDataSet()
			dataSetTXT.setDataSetType("TEXT_FILES")
			dataSetTXT.setSample(cellSample)
			dataSetTXT.setParentDatasets([dataSetCode])


			transaction.moveFile(create_dir(incoming), dataSetTXT)
	

	import_files(incoming.getPath())


		

	


