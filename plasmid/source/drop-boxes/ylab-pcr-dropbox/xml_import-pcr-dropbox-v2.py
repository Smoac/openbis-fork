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
enzymes_list=[]


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
	
	def Enzymes():
		  for path in [ './Enzymes']:
			node = tree.find(path)
			if node.text is not None:
			  enzymes = node.text
			  tokens = enzymes.split(',')
			  for token in tokens:
				if re.search(":", token): 
				  token = token.split(':')
				  enzyme_name = token[0]
				  enzyme_concentration=token[1]
				  enzymes_list.append(enzyme_name)
				else:
				  enzyme_name=token  
				  enzyme_concentration = "n.a."
				  enzymes_list.append(enzyme_name)
			else:
			  node.text = "n.a." 	  
		  print "ANTIBODY", enzymes_list
		  return enzymes_list
		  
	Enzymes()	  


	
	
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
		  chemicalPath= "/YEAST_LAB/" + chemicals_list
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
		  buffersPath= "/YEAST_LAB/" + buffers_list
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


	def _createEnzymesLink(enzymes_list):
		"""
		   Creates sample link XML element for sample with specified 'code'. The element will contain
		   given code as 'code' attribute apart from standard 'permId' attribute.
		   
		   If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
		   
		   @return: sample link XML element as string, e.g.:
		   - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
		   - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
		"""
		if enzymes_list is not None:
		  enzymesPath= "/YEAST_LAB/" + enzymes_list
		  permId =transaction.getSample(enzymesPath).getSample().getPermId()
		  if not permId:
			permId = enzymes_list
		  name = transaction.getSample(enzymesPath).getPropertyValue("NAME")
		  sampleLink = elementFactory.createSampleLink(permId)
		
		  sampleLink.addAttribute(ATR_CODE, enzymes_list)
		  sampleLink.addAttribute(ATR_NAME, name)
		 		 
		return sampleLink    
	    
	
	"""
	Example input:
	
	FRC1: 2nM, FRC2, FRC3: 4nM, FRC4
	"""
	
	
	def updateEnzymesFromBatchInput(enzymes_list):
		elements = []
		input = enzymes_list
		if input != "":
		   for i in enzymes_list: 
				sampleLink = _createEnzymesLink(i.strip())
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

	newSampleIdentifier="/YEAST_LAB/" + sample_name
	newSample=transaction.createNewSample(newSampleIdentifier,'PCR')
	
	
	exp = transaction.getExperiment(experiment_name)
	newSample.setExperiment(exp)

	""" Set enzymes, chemicals, buffers as parents for the sample"""	

	enzymeId_list =[]
	for enzyme in enzymes_list:
		enzymeIdentifier = "/YEAST_LAB/" + enzyme.strip()
		enzymeId_list.append(enzymeIdentifier)

	chemicalId_list =[]
	for chemical in chemicals_list:
		chemicalIdentifier = "/YEAST_LAB/" + chemical.strip()
		chemicalId_list.append(chemicalIdentifier)
		
	bufferId_list =[]
	for buffer in buffers_list:
		bufferIdentifier = "/YEAST_LAB/" + buffer.strip()
		bufferId_list.append(bufferIdentifier)
		
	parents_list = enzymeId_list + chemicalId_list + bufferId_list
	newSample.setParentSampleIdentifiers(parents_list)	
	
	
	for child in root:
		if child.tag == "Chemicals":
			newSample.setPropertyValue("CHEMICALS",updateChemicalsFromBatchInput(chemicals_list,concentration_list))
		if child.tag == "Solutions_Buffers":
			newSample.setPropertyValue("SOLUTIONS_BUFFERS",updateBuffersFromBatchInput(buffers_list,buffers_concentration_list))
		if child.tag == "Enzymes":
			newSample.setPropertyValue("Enzymes",updateEnzymesFromBatchInput(enzymes_list))
  		if child.tag == "XMLCOMMENTS":
  			newSample.setPropertyValue("XMLCOMMENTS", updateCommentsFromBatchInput(comment_text_list))
		if child.tag != "Identifier" and child.tag !="Experiment" and child.tag != "Chemicals" and child.tag != "XMLCOMMENTS" and child.tag != "Solutions_Buffers" and child.tag != "Enzymes":
			if child.text != None:
				newSample.setPropertyValue(child.tag, child.text)
  			else:
				child.text= ""
				newSample.setPropertyValue(child.tag, child.text)
