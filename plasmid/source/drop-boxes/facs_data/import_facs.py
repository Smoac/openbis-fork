#! /usr/bin/env python
"""
  Script for uploading FACS data for the YeastLab.

  FACS data are organized in one main folder which contains a folder with the username. Inside this folder there are folders and subfolders that correspond 
  to different FACS experiments. Inside the username folder there is a file called data_structure.ois which contains the structure of the FACS experiments.
  This script creates a sample in openBIS for each FACS experiment and uploads the corresponding data in one dataset connected to the samples.
  The space where the sample should go is detected from the username. The project and experiment are detected from the name of the folders given by the user.
  The FACS experiment should be named by the users as PROJECT-EXPERIMENT-SAMPLE.  
  
"""

import os, glob, re, csv, time, shutil
from java.io import File
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageContainerDataConfig, ChannelColor
from loci.formats import ImageReader
from time import *
from datetime import *


def process(transaction):
	incoming = transaction.getIncoming()
	
	projectNameList=[]
	experimentNameList=[]
	sampleNameList=[]
	directoryToImportList=[]
	userNameList=[]


	# read the data_structure.ois file to get the structure of the FACS experiment. The name of the FACS experiment contains project, experiment, sample info for openBIS.
	def getProjExpSample(incoming):
		for userName in os.listdir(incoming):
			if not userName.startswith('.'):
				dataStructure = incoming + "/" + userName + "/data_structure.ois" 
				f = open(dataStructure)
				lines=f.readlines()
				for line in lines:
					path = re.split("/", line)
					directoryToImport= incoming +"/"+ "/".join(path[:-1])
					projectName = re.split("-",path[-1])[0]
					if len(re.split("-",path[-1])) == 3:
						experimentName = re.split("-",path[-1])[1]
						sampleNameFile = re.split("-",path[-1])[2]
						sampleName = re.split("_properties.oix", sampleNameFile)[0]
					elif len(re.split("-",path[-1]))==2:
						experimentNameProp = re.split("-",path[-1])[1]
						experimentName = re.split("_properties.oix", experimentNameProp)[0]
						sampleName = "na"
					

					projectNameList.append(projectName)
					experimentNameList.append(experimentName)
					sampleNameList.append(sampleName)
					directoryToImportList.append(directoryToImport)
					userNameList.append(userName)

		return userNameList, projectNameList, experimentNameList, sampleNameList, directoryToImportList
					

	getProjExpSample(incoming.getPath()) 

	
	for user in set (userNameList):
		if user == "pontia":
			space = "AARON"
		elif user == "ottozd":
			space = "DIANA"
		elif user == "elfstrok":
			space = "KRISTINA"


	for proj in set(projectNameList):
		print space
		project = transaction.getProject("/" + space + "/" + proj)
		if not project:
	 		project = transaction.createNewProject("/" + space + "/" + proj)

	for exp in set(experimentNameList):
		experiment = transaction.getExperiment("/" + space  + "/" + proj + "/" + exp)
		if not experiment:
  			experiment = transaction.createNewExperiment("/" + space  + "/" + proj + "/" + exp, "FACS_DATA")	

  	for sample, directory in zip(sampleNameList,directoryToImportList):
  		if sample == "na":
  			sampleNew = transaction.createNewSampleWithGeneratedCode(space, "FACS_DATA")
  			sampleNew.setExperiment(experiment)
  		else:
  			sampleNew = transaction.createNewSample("/" + space + "/" + sample, "FACS_DATA" )
			sampleNew.setExperiment(experiment)
		
		dataSet = transaction.createNewDataSet()
		dataSet.setDataSetType("FACS_DATA")
		dataSet.setSample(sampleNew)
		transaction.moveFile(directory, dataSet)					
	

	
		

	


