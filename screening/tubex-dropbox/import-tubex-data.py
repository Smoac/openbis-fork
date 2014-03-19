#! /usr/bin/env python
"""
  Script for uploading image data and some other files.
  
"""

import os, glob, re, csv, time, shutil
from java.io import File
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageContainerDataConfig, ChannelColor
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


	#create a directory where to move all mtTracker files
	# def create_mtTrackerdir(incoming, fileList):
	# 	mtTrackerDir = incoming + "/mtTrackerDir" 
	# 	if not os.path.exists(mtTrackerDir):
	# 		os.makedirs(mtTrackerDir)
	# 	for item in fileList:
	# 		print "item", item
	# 		mtTrackerSphere = incoming + "/" +item + "_mtTracker_sphereCovMatrix.txt"
	# 		mtTrackerResults = incoming + "/" +item + "_mtTracker_results.txt"
	# 		mtTrackerParams = incoming + "/" +item + "_mtTracker_params.txt"
	# 		mtTrackerInitVal = incoming + "/" +item + "_mtTracker_initValues.txt"
	# 		mtTrackerCovMatrix = incoming + "/" +item + "_mtTracker_covMatrix.txt"

	# 		shutil.move(mtTrackerSphere,  mtTrackerDir)
	# 		shutil.move(mtTrackerResults, mtTrackerDir)
	# 		shutil.move(mtTrackerParams, mtTrackerDir)			
	# 		shutil.move(mtTrackerInitVal, mtTrackerDir)
	# 		shutil.move(mtTrackerCovMatrix, mtTrackerDir)
			
	# 		print "incoming", incoming
	# 		print "test", mtTrackerParams
	# 		print "trackerdir", mtTrackerDir

	# 	return mtTrackerDir


	# create_mtTrackerdir(incoming.getPath(), fileList)

#	print "3333333333333", create_mtTrackerdir(incoming.getPath(), fileList)
		

	#move mtTracker text files into the mtTracker directory
	# def move_mtTracker_files(incoming):
	# 	for item in fileList:
	# 		mtTrackerSphere = item + "_mtTracker_sphereCovMatrix.txt"
	# 		mtTrackerResults = item + "_mtTracker_results.txt"
	# 		mtTrackerParams = item + "_mtTracker_params.txt"
	# 		mtTrackerInitVal = item + "_mtTracker_initValues.txt"
	# 		mtTrackerCovMatrix = item + "_mtTracker_covMatrix.txt"

	# 		shutil.move(incoming + "/" + mtTrackerSphere,  create_mtTrackerdir(incoming))
	# 		shutil.move(incoming + "/" + mtTrackerResults, create_mtTrackerdir(incoming))
	# 		shutil.move(incoming + "/" + mtTrackerParams, create_mtTrackerdir(incoming))			
	# 		shutil.move(incoming + "/" + mtTrackerInitVal, create_mtTrackerdir(incoming))
	# 		shutil.move(incoming + "/" + mtTrackerCovMatrix, create_mtTrackerdir(incoming))	
	# 		test = incoming + "/" + mtTrackerCovMatrix
	# 		print "test", test

	# move_mtTracker_files(incoming.getPath())

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
	

	#create a sample for each date and experiment number. Create a sample for each cell of each date and experiment number and set it as contained sample.
	# extract data from the Neck and Axis files and set them as properties of the cell sample
	# import the tif files as image dataset. Import the mtTracker files in the same dataset.
	def import_files(incoming):
		for item, axisFile, neckFile in zip(fileList, axisList, neckList):
			tifFile = item + ".tif"
	
			mtTrackerSphere = incoming + "/" +item + "_mtTracker_sphereCovMatrix.txt"
			mtTrackerResults = incoming + "/" +item + "_mtTracker_results.txt"
			mtTrackerParams = incoming + "/" +item + "_mtTracker_params.txt"
			mtTrackerInitVal = incoming + "/" +item + "_mtTracker_initValues.txt"
			mtTrackerCovMatrix = incoming + "/" +item + "_mtTracker_covMatrix.txt"			

		  	tokens = re.split('_', item)
	 	  	dateExp = tokens[0]
	 	  	cellNum = tokens[1]
			dateExpSample = transaction.createNewSample("/TUBEX/" + dateExp,'DATE_EXPERIMENT_NUM')
			dateExpSample.setExperiment(exp) 
			cellSample = transaction.createNewSample("/TUBEX/" + item,'CELL_NUMBER' )
			cellSample.setContainer(dateExpSample)
			cellSample.setExperiment(exp)

		
			if (axisFile == item):
				axisFilePath = incoming + "/AxisData/Axis_"+ axisFile + ".txt"
				f = open(axisFilePath, 'r')		
				lines=f.readlines()
				for line in lines:
					if re.match('^1', line):
						fields = re.split("\t", line)
						budEnd_x_coordinate= fields[3]
						budEnd_y_coordinate = fields[4]
						cellSample.setPropertyValue('BUDEND_X_COORDINATE', str(budEnd_x_coordinate))
			 			cellSample.setPropertyValue('BUDEND_Y_COORDINATE', str(budEnd_y_coordinate))
			 		if re.match('^2', line):
		 				fields = re.split("\t", line)
		 				motherEnd_x_coordinate= fields[3]
		 				motherEnd_y_coordinate = fields[4]
						cellSample.setPropertyValue('MOTHEREND_X_COORDINATE', str(motherEnd_x_coordinate))
			 			cellSample.setPropertyValue('MOTHEREND_Y_COORDINATE', str(motherEnd_y_coordinate))	


			if (neckFile == item):
				neckFilePath = incoming + "/NeckData/Neck_"+ neckFile + ".txt"
				f1 = open(neckFilePath, 'r')		
				lines1=f1.readlines()
				for line in lines1:
					if re.match('^1', line):
						fields = re.split("\t", line)
						neck1_x_coordinate= fields[3]
						neck1_y_coordinate = fields[4]
						cellSample.setPropertyValue('NECK1_X_COORDINATE', str(neck1_x_coordinate))
			 			cellSample.setPropertyValue('NECK1_Y_COORDINATE', str(neck1_y_coordinate))
			 		if re.match('^2', line):
		 				fields = re.split("\t", line)
		 				neck2_x_coordinate= fields[3]
		 				neck2_y_coordinate = fields[4]
						cellSample.setPropertyValue('NECK2_X_COORDINATE', str(neck2_x_coordinate))
			 			cellSample.setPropertyValue('NECK2_Y_COORDINATE', str(neck2_y_coordinate))	

			imageDataset = SimpleImageContainerDataConfig()
			imageDataset.setMicroscopyData(True)
			imageDataset.setDataSetType("MICROSCOPY_IMG")
			imageDataset.setRecognizedImageExtensions(['lif', 'dv', 'tif', 'tiff','nd2', 'lsm', 'czi'])
			imageDataset.setImageLibrary("BioFormats")
			imageDataset.setMeasuredData(True)


			
			sampleNamePath = incoming + '/' + tifFile

	 	 	dataSet = transaction.createNewImageDataSet(imageDataset, File(sampleNamePath))
			dataSet.setSample(cellSample)
			
			transaction.moveFile(sampleNamePath, dataSet)
			transaction.moveFile(mtTrackerSphere, dataSet)
			transaction.moveFile(mtTrackerResults, dataSet)
			transaction.moveFile(mtTrackerParams, dataSet)
			transaction.moveFile(mtTrackerInitVal, dataSet)
			transaction.moveFile(mtTrackerCovMatrix, dataSet)


			#transaction.moveFile(incoming, dataSet)
			#transaction.moveFile(create_mtTrackerdir(incoming, fileList), dataSet)
	


	import_files(incoming.getPath())




	


