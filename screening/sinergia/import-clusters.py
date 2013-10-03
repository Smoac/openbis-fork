#! /usr/bin/env python
"""
  Script for uploading cluster data. Data is organized in directories called as cluster numbers (from 1 to 19). Each cluster
  directory has 1 pdf file and 10 TIF image files. Each directory also has several subdirectories with gene names. In each of these
  a pdf file and 10 mp4 files are contained.
  In openBIS samples withe the names of clusters will be created with the files contained in them uploaded as datasets.
  the gene directories will be uploaded as contained samples in the corresponding cluster with the respective files uploaded as datasets.
  
"""

import os, re, glob, shutil
from time import *
from datetime import *


from java.io import File
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, Location
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageContainerDataConfig, ChannelColor
from loci.formats import ImageReader

print "###################################################"
tz=localtime()[3]-gmtime()[3]
d=datetime.now()
print d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")


def process(transaction):
	incoming = transaction.getIncoming()
	clusterName = os.path.basename(incoming.getPath())

#Check if project and experiment called "CLUSTERS" already exist, if not create them	
	project = transaction.getProject("/SINERGIA/CLUSTERS")
	exp = transaction.getExperiment("/SINERGIA/CLUSTERS/CLUSTERS")
	if not project:
		project = transaction.createNewProject("/SINERGIA/CLUSTERS")
	if not exp:
		exp = transaction.createNewExperiment("/SINERGIA/CLUSTERS/CLUSTERS", 'SIRNA_HCS')
		exp.setPropertyValue("DESCRIPTION", "gene clusters")

#create samples called Cluster1 to 19. Take the name from the directory (in the incoming folder there are 19 directories called cluster1-19)		
	newClusterSample=transaction.createNewSample("/SINERGIA/" + clusterName,'CLUSTER') 
	newClusterSample.setExperiment(exp)

#upload the pdf image of each cluster as dataset in the corresponding Cluster sample
	for pdf in glob.glob(os.path.join(incoming.getPath(), '*.pdf')):
		dataSetPDF = transaction.createNewDataSet()
		dataSetPDF.setDataSetType("PDF")
		dataSetPDF.setSample(newClusterSample)
		transaction.moveFile(pdf, dataSetPDF)
 
# upload the 10 tif images for each cluster as a dataset in the corresponding Cluster sample		
	tifDir = incoming.getPath() + "/tiffs"
	if not os.path.exists(tifDir):
		os.makedirs(tifDir)
	if glob.glob(os.path.join(incoming.getPath(), '*.TIF')):	     		
		for tif in glob.glob(os.path.join(incoming.getPath(), '*.TIF')):
			shutil.move(tif, tifDir)
		dataSetTIF = transaction.createNewDataSet()
		dataSetTIF.setDataSetType("MICROSCOPY_IMG")
		dataSetTIF.setSample(newClusterSample)
		transaction.moveFile(tifDir, dataSetTIF)

#in each Cluster directory there are subdirectories for each gene. Now we create a sample for each gene and set the cluster it belongs to as a container sample.
#Each gene has a pdf file and 10 movies, so tehy will be uploaded as datasets
	if not glob.glob(os.path.join(incoming.getPath(), 'tiffs')):	
		for genes, pdfGene in zip(glob.glob(os.path.join(incoming.getPath(), '*')), glob.glob(os.path.join(incoming.getPath(), '*/*.pdf'))):
	 		geneName = os.path.basename(genes)
	 		newGeneSample = transaction.createNewSample("/SINERGIA/" + geneName,'GENE')
	 		newGeneSample.setContainer(newClusterSample)
 			newGeneSample.setExperiment(exp)
			videoDir = genes + "/videos"
			if not os.path.exists(videoDir):
				os.makedirs(videoDir)
	 		dataSetpdfGene = transaction.createNewDataSet()
	  		dataSetpdfGene.setDataSetType("PDF")
  			dataSetpdfGene.setSample(newGeneSample)
  			transaction.moveFile(pdfGene, dataSetpdfGene)

	 		for genes, mp4 in zip(glob.glob(os.path.join(incoming.getPath(), '*')), glob.glob(os.path.join(incoming.getPath(), '*/*.mp4'))):
	 			shutil.move(mp4, videoDir)
			dataSetMP4Gene = transaction.createNewDataSet()
	 		dataSetMP4Gene.setDataSetType("VIDEOS")
	 		dataSetMP4Gene.setSample(newGeneSample)
	 		transaction.moveFile(videoDir, dataSetMP4Gene)



