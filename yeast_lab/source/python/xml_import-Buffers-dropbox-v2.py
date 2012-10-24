#! /usr/bin/env python
"""
 Import sample properties from a text file.  
"""
print "########################################################"

import os
import glob
import re
import time
import shutil, sys

import xml.etree.ElementTree as ET
from xml.etree import ElementTree

from time import *
from datetime import datetime
import calendar

from ch.systemsx.cisd.openbis.generic.shared.managed_property.structured import ElementFactory
from ch.systemsx.cisd.openbis.generic.shared.managed_property.structured import XmlStructuredPropertyConverter

ATR_NAME='name'
ATR_CODE = "code"
ATR_CONC = "concentration"
DATE_ATTRIBUTE = 'date'
PERSON_ATTRIBUTE = 'person'
COMMENT_ENTRY_ELEMENT_LABEL = 'commentEntry'
LINK_LABEL = "link"
CODE_LABEL = "code"
CONC_LABEL = "concentration"
DATE_LABEL = 'Date'
PERSON_LABEL = 'Person'
COMMENT_TEXT_LABEL = 'Comment Text'
NAME_LABEL='name'

chemicals_list=[]
concentration_list=[]
buffers_list=[]
buffers_concentration_list =[]
date_list=[]
person_list=[]
comment_text_list=[]
medias_list=[]
medias_concentration_list=[]


def create_openbis_timestamp():
	tz=localtime()[3]-gmtime()[3]
	d=datetime.now().timetuple()
	timestamp = (calendar.timegm(d)*1000)
	return timestamp
   

def process(transaction):
	incomingPath = transaction.getIncoming().getAbsolutePath()

###PARSE XML FILE############################################################


	textfile = open(incomingPath, "r")
	tree = ET.parse(textfile)
	root = tree.getroot()

	
	
	for path in [ './Identifier']:
	  node = tree.find(path)
	  if node.text != "":
	    sample_name = node.text
	  else:
	    node.text = ""  
	    
	  
	for path in [ './Experiment']:
	  node = tree.find(path)
 	  if node.text != "":
		experiment_name = node.text
	  else:
	    node.text = ""  
	
	
       
	  
	def Chemicals():
	  for path in [ './Chemicals']:
		node = tree.find(path)
		if node.text is not None:
		  chemicals = node.text
		  tokens = chemicals.split(',')
		  for token in tokens:
		    if re.search(":", token): 
			  token = token.split(':')
			  chemical_name = token[0]
			  chemical_concentration=token[1]
			  chemicals_list.append(chemical_name)
			  concentration_list.append(chemical_concentration)
		    else:
			  chemical_name=token  
			  chemical_concentration = "n.a."
			  chemicals_list.append(chemical_name)
			  concentration_list.append(chemical_concentration)
		else:
		  node.text = "n.a." 	  
	  print "CHEM", chemicals_list, concentration_list
	  return chemicals_list, concentration_list	  
	
	Chemicals()

	def Buffers():
		  for path in [ './Solutions_Buffers']:
			node = tree.find(path)
			if node.text is not None:
			  buffers = node.text
			  tokens = buffers.split(',')
			  for token in tokens:
				if re.search(":", token): 
				  token = token.split(':')
				  buffer_name = token[0]
				  buffer_concentration=token[1]
				  buffers_list.append(buffer_name)
				  buffers_concentration_list.append(buffer_concentration)
				else:
				  buffer_name=token  
				  buffer_concentration = "n.a."
				  buffers_list.append(buffer_name)
				  buffers_concentration_list.append(buffer_concentration)
			else:
			  node.text = "n.a." 	  
		  print "BUFF", buffers_list, buffers_concentration_list
		  return buffers_list, buffers_concentration_list
		  
	Buffers()	  
	
	def Medias():
		  for path in [ './Media']:
			node = tree.find(path)
			if node.text is not None:
			  medias = node.text
			  tokens = medias.split(',')
			  for token in tokens:
				if re.search(":", token): 
				  token = token.split(':')
				  media_name = token[0]
				  media_concentration=token[1]
				  medias_list.append(media_name)
				  medias_concentration_list.append(media_concentration)
				else:
				  media_name=token  
				  media_concentration = "n.a."
				  medias_list.append(media_name)
				  medias_concentration_list.append(media_concentration)
			else:
			  node.text = "n.a." 	  
		  print "MEDIA", medias_list, medias_concentration_list
		  return medias_list, medias_concentration_list
		  
	Medias()	  


	
	
	for path in [ './XMLCOMMENTS']:
	  node = tree.find(path)
	  if node.text is not None:
		comment_text_list= node.text
	  else:
	    comment_text_list = None 



	elementFactory = ElementFactory()
	
	propertyConverter = XmlStructuredPropertyConverter(elementFactory);
    


###IMPORT CHEMICALS####################################################################


	def _createChemicalsSampleLink(chemicals_list, concentration_list):
		"""
		   Creates sample link XML element for sample with specified 'code'. The element will contain
		   given code as 'code' attribute apart from standard 'permId' attribute.
		   
		   If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
		   
		   @return: sample link XML element as string, e.g.:
		   - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
		   - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
		"""
		if chemicals_list is not None:
		  chemicalPath= "/YLAB-TEST/" + chemicals_list
		  permId =transaction.getSample(chemicalPath).getSample().getPermId()
		  name = transaction.getSample(chemicalPath).getPropertyValue("NAME")
		  if not permId:
			permId = chemicals_list
		  sampleLink = elementFactory.createSampleLink(permId)
		
		  sampleLink.addAttribute(ATR_CODE, chemicals_list)
		  sampleLink.addAttribute(ATR_NAME, name)
		  sampleLink.addAttribute(ATR_CONC, concentration_list)
		 		 
		return sampleLink    
	    
	
	"""
	Example input:
	
	FRC1: 2nM, FRC2, FRC3: 4nM, FRC4
	"""
	
	
	def updateChemicalsFromBatchInput(chemicals_list, concentration_list):
		elements = []
		input = chemicals_list
		input2 = concentration_list
		if input != "":
		   for i, j in zip(chemicals_list,concentration_list): #zip is used to iterate over two lists in parallel
				sampleLink = _createChemicalsSampleLink(i.strip(), j.strip())
				elements.append(sampleLink)
		return propertyConverter.convertToString(elements)


###IMPORT SOLUTION BUFFERS####################################################################


	def _createBuffersLink(buffers_list, buffers_concentration_list):
		"""
		   Creates sample link XML element for sample with specified 'code'. The element will contain
		   given code as 'code' attribute apart from standard 'permId' attribute.
		   
		   If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
		   
		   @return: sample link XML element as string, e.g.:
		   - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
		   - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
		"""
		if buffers_list is not None:
		  buffersPath= "/YLAB-TEST/" + buffers_list
		  permId =transaction.getSample(buffersPath).getSample().getPermId()
		  if not permId:
			permId = buffers_list
		  name = transaction.getSample(buffersPath).getPropertyValue("NAME")
		  sampleLink = elementFactory.createSampleLink(permId)
		
		  sampleLink.addAttribute(ATR_CODE, buffers_list)
		  sampleLink.addAttribute(ATR_NAME, name)
		  sampleLink.addAttribute(ATR_CONC, buffers_concentration_list)

		 
		return sampleLink    
	    
	
	"""
	Example input:
	
	FRC1: 2nM, FRC2, FRC3: 4nM, FRC4
	"""
	
	
	def updateBuffersFromBatchInput(buffers_list, buffers_concentration_list):
		elements = []
		input = buffers_list
		input2 = buffers_concentration_list
		if input != "":
		   for i, j in zip(buffers_list,buffers_concentration_list): #zip is used to iterate over two lists in parallel
				sampleLink = _createBuffersLink(i.strip(), j.strip())
				elements.append(sampleLink)
		return propertyConverter.convertToString(elements)
		
###IMPORT MEDIA####################################################################


	def _createMediasLink(medias_list, medias_concentration_list):
		"""
		   Creates sample link XML element for sample with specified 'code'. The element will contain
		   given code as 'code' attribute apart from standard 'permId' attribute.
		   
		   If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
		   
		   @return: sample link XML element as string, e.g.:
		   - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
		   - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
		"""
		if medias_list is not None:
		  mediasPath= "/YLAB-TEST/" + medias_list
		  permId =transaction.getSample(mediasPath).getSample().getPermId()
		  if not permId:
			permId = medias_list
		  name = transaction.getSample(mediasPath).getPropertyValue("NAME")
		  sampleLink = elementFactory.createSampleLink(permId)
		
		  sampleLink.addAttribute(ATR_CODE, medias_list)
		  sampleLink.addAttribute(ATR_NAME, name)
		  sampleLink.addAttribute(ATR_CONC, medias_concentration_list)
		 		 
		return sampleLink    
	    
	
	"""
	Example input:
	
	FRC1: 2nM, FRC2, FRC3: 4nM, FRC4
	"""
	
	
	def updateMediasFromBatchInput(medias_list, medias_concentration_list):
		elements = []
		input = medias_list
		input2 = medias_concentration_list
		if input != "":
		   for i, j in zip(medias_list,medias_concentration_list): #zip is used to iterate over two lists in parallel
				sampleLink = _createMediasLink(i.strip(), j.strip())
				elements.append(sampleLink)
		return propertyConverter.convertToString(elements)			
		
		
###IMPORT COMMENTS####################################################################
	
	

	def _createCommentsSampleLink(comment_text_list):
		#if comment_text_list is not None:
		commentEntry = elementFactory.createElement(COMMENT_ENTRY_ELEMENT_LABEL)
		  		
		user = transaction.getUserId()
		commentEntry.addAttribute(PERSON_ATTRIBUTE, user)
		commentEntry.addAttribute(DATE_ATTRIBUTE,str(create_openbis_timestamp()))
		commentEntry.setData(comment_text_list)
		print "comments", comment_text_list  
		return commentEntry   
	
	def updateCommentsFromBatchInput(comment_text_list):
		elements = []
		input = comment_text_list
		#if input is not None:
		commentEntry = _createCommentsSampleLink(comment_text_list)
		elements.append(commentEntry)
		return propertyConverter.convertToString(elements)
	
	_createCommentsSampleLink(comment_text_list)
	
	
	

###CREATE New sample with related properties#################################################

	newSampleIdentifier="/YLAB-TEST/" + sample_name
	newSample=transaction.createNewSample(newSampleIdentifier,'SOLUTIONS_BUFFERS')
	
	
	exp = transaction.getExperiment(experiment_name)
	newSample.setExperiment(exp)
	
	
	
	for child in root:
		if child.tag == "Chemicals":
			newSample.setPropertyValue("CHEMICALS",updateChemicalsFromBatchInput(chemicals_list,concentration_list))
		if child.tag == "Solutions_Buffers":
			newSample.setPropertyValue("SOLUTIONS_BUFFERS",updateBuffersFromBatchInput(buffers_list,buffers_concentration_list))
		if child.tag == "Media":
			newSample.setPropertyValue("Media",updateMediasFromBatchInput(medias_list,medias_concentration_list))
  		if child.tag == "XMLCOMMENTS":
  			newSample.setPropertyValue("XMLCOMMENTS", updateCommentsFromBatchInput(comment_text_list))
		if child.tag != "Identifier" and child.tag !="Experiment" and child.tag != "Chemicals" and child.tag != "XMLCOMMENTS" and child.tag != "Solutions_Buffers" and child.tag != "Media":
			if child.text != None:
				newSample.setPropertyValue(child.tag, child.text)
  			else:
				child.text= ""
				newSample.setPropertyValue(child.tag, child.text)