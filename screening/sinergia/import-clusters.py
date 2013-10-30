#! /usr/bin/env python
"""
  Script for uploading cluster data. Data is organized in directories called as cluster numbers (from 1 to 17). Each cluster
  directory has 1 pdf file and 10 TIF image files. There is also a text file that contains the list of genes contained in each cluster.
  
  
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

#create samples called Cluster1 to 17. Take the name from the directory (in the incoming folder there are 17 directories called cluster1-17)		
	newClusterSample = transaction.getSample("/SINERGIA/" + clusterName)
	if not newClusterSample:
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
	if glob.glob(os.path.join(incoming.getPath(), '*.tif')):	     		
		for tif in glob.glob(os.path.join(incoming.getPath(), '*.tif')):
			shutil.move(tif, tifDir)
		dataSetTIF = transaction.createNewDataSet()
		dataSetTIF.setDataSetType("TIF_IMAGES")
		dataSetTIF.setSample(newClusterSample)
		transaction.moveFile(tifDir, dataSetTIF)


# upload the 10 png images for each cluster as a dataset in the corresponding Cluster sample		
	pngDir = incoming.getPath() + "/pngs"
	if not os.path.exists(pngDir):
		os.makedirs(pngDir)
	if glob.glob(os.path.join(incoming.getPath(), '*.png')):	     		
		for png in glob.glob(os.path.join(incoming.getPath(), '*.png')):
			shutil.move(png, pngDir)
		dataSetPNG = transaction.createNewDataSet()
		dataSetPNG.setDataSetType("PNG_IMAGES")
		dataSetPNG.setSample(newClusterSample)
		transaction.moveFile(pngDir, dataSetPNG)



# Open the geneList text file and create samples with the name of the genes. These samples have to be contained in the corresponding cluster.
	for textfile in glob.glob(os.path.join(incoming.getPath(), 'geneList.txt')):
		text = open(textfile, "r")
		lineIndex =0
		for line in text:
			lineIndex=lineIndex+1
			gene_list = re.split(r"[,]",line)
			gene_list = [ item.strip() for item in gene_list ]
			gene_list = filter(lambda x: len(x) > 0, gene_list)
			for gene in gene_list:
				print gene
				newGeneSample = transaction.createNewSample("/SINERGIA/" + gene,'GENE')
				newGeneSample.setContainer(newClusterSample) 
				newGeneSample.setExperiment(exp)




###################################################################################################################################
#This part of the script assumes that the gene directories are inside the cluster directories. 

# #in each Cluster directory there are subdirectories for each gene. Now we create a sample for each gene and set the cluster it belongs to as a container sample.
# #Each gene has a pdf file and 10 movies, so tehy will be uploaded as datasets
# 	if not glob.glob(os.path.join(incoming.getPath(), 'tiffs')):	
# 		for genes, pdfGene in zip(glob.glob(os.path.join(incoming.getPath(), '*')), glob.glob(os.path.join(incoming.getPath(), '*/*.pdf'))):
# 	 		geneName = os.path.basename(genes)
# 	 		newGeneSample = transaction.createNewSample("/SINERGIA/" + geneName,'GENE')
# 	 		newGeneSample.setContainer(newClusterSample)
#  			newGeneSample.setExperiment(exp)
# 			videoDir = genes + "/videos"
# 			if not os.path.exists(videoDir):
# 				os.makedirs(videoDir)
# 	 		dataSetpdfGene = transaction.createNewDataSet()
# 	  		dataSetpdfGene.setDataSetType("PDF")
#   			dataSetpdfGene.setSample(newGeneSample)
#   			transaction.moveFile(pdfGene, dataSetpdfGene)
# 
# 	 		for genes, mp4 in zip(glob.glob(os.path.join(incoming.getPath(), '*')), glob.glob(os.path.join(incoming.getPath(), '*/*.mp4'))):
# 	 			shutil.move(mp4, videoDir)
# 			dataSetMP4Gene = transaction.createNewDataSet()
# 	 		dataSetMP4Gene.setDataSetType("VIDEOS")
# 	 		dataSetMP4Gene.setSample(newGeneSample)
# 	 		transaction.moveFile(videoDir, dataSetMP4Gene)
# 
# 

