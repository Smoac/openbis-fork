#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import sys
import datetime
import traceback

from com.xhaus.jyson import JysonCodec as json
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClauseAttribute
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from org.springframework.web.context.request import RequestContextHolder, RequestAttributes

def process(tr, parameters, tableBuilder):
	method = parameters.get("method")
	methodParameters = parameters.get("methodParameters")

	print "Started processing '%s' publication method '%s'" % (method, str(datetime.datetime.utcnow()))

	tableBuilder.addHeader("RESULT")
	tableBuilder.addHeader("ERROR")
	row = tableBuilder.addRow()

	try:
		if method == "getSpaces":
			response = getSpaces(tr, methodParameters, tableBuilder)
		elif method == "getMeshTermChildren":
			response = getMeshTermChildren(tr, methodParameters, tableBuilder)
		elif method == "publish":
			response = publish(tr, methodParameters, tableBuilder)
		else:
			response = [None, "Method '" + str(method) + "' is not supported"]
			
		row.setCell("RESULT", response[0])
		row.setCell("ERROR", response[1])
			
	except Exception, e:
		row.setCell("RESULT", None)
		row.setCell("ERROR", traceback.format_exc())
	finally:
		print "Finished processing '%s' publication method '%s'" % (method, str(datetime.datetime.utcnow()))

def getSpaces(tr, parameters, tableBuilder):
	property = getProperty(tr, "published-spaces")
	codes = []
	
	if property and property.strip():
		codes = []
		
		for token in property.split(','):
			if token.strip():
				codes.append(token.strip())
	
	if codes:
		return [json.dumps(codes), None]
	else:
		return [None, "No publication spaces have been configured. Please check that 'published-spaces' property has been properly set in the reporting plugin plugin.properties."]	

def getMeshTermChildren(tr, parameters, tableBuilder):
	parentIdentifier = parameters["parent"]
	
	if(not parentIdentifier):
		parentIdentifier = ""
	
	termMap = loadMeshTerms()
	parent = termMap.get(parentIdentifier)

	if parent:
		childrenData = []
		for child in parent["children"]:
			childData = {
				"identifier" : child["identifier"],
				"name" : child["name"],
				"fullName" : child["fullName"],
				"hasChildren" : len(child["children"]) > 0
			}
			childrenData.append(childData)
		return [json.dumps(childrenData), None]
	else:
		return [None, "No term have been found for '" + identifier + "'."]	

def loadMeshTerms():

	cachedTermsSessionAttribute = "publication.meshTerms"
	cachedTerms = RequestContextHolder.getRequestAttributes().getAttribute(cachedTermsSessionAttribute, RequestAttributes.SCOPE_SESSION)
	
	if cachedTerms:
		print "Didn't have to load the mesh terms - found them in a session cache."
		return cachedTerms
	
	print "Loading mesh terms - start time: " + str(datetime.datetime.utcnow())
	
	binPath = str(sys.path[-1]) + "/terms/mesh_terms.bin"
	binFile = open(binPath)
	
	try:
		termMap = {}
		
		addRootMeshTerm(termMap)
		
		print "Root terms: " + str(termMap)
		
		lines = binFile.readlines()
		for line in lines:
			separatorIndex = line.rfind(";")
			
			termName = line[0:separatorIndex].strip()
			termIdentifier = line[separatorIndex + 1:len(line)].strip()
			parentIdentifier = None
			
			if(termIdentifier.find(".") == -1):
				parentIdentifier = termIdentifier[0];
			else:
				parentIdentifier = termIdentifier[0:termIdentifier.rfind(".")]

			parent = termMap[parentIdentifier]
			term = addMeshTerm(termMap, parent, termIdentifier, termName)
			parent["children"].append(term)
			
		RequestContextHolder.getRequestAttributes().setAttribute(cachedTermsSessionAttribute, termMap, RequestAttributes.SCOPE_SESSION)
			
		return termMap

	finally:
		print "Loading mesh terms - finish time: " + str(datetime.datetime.utcnow())
		binFile.close()
		

def addRootMeshTerm(termMap):

	root = addMeshTerm(termMap, None, "", "")
	
	binPath = str(sys.path[-1]) + "/terms/root_mesh_terms.bin"
	binFile = open(binPath)

	print "Loading root mesh terms - start time: " + str(datetime.datetime.utcnow())
	try:
		lines = binFile.readlines()
		for line in lines:
			separatorIndex = line.rfind(";")

			termName = line[0:separatorIndex].strip()
			termIdentifier = line[separatorIndex + 1:len(line)].strip()
			
			term = addMeshTerm(termMap, root, termIdentifier, termName)
			root["children"].append(term)
		
	finally:
		print "Loading root mesh terms - finish time: " + str(datetime.datetime.utcnow())
		binFile.close()
		

def addMeshTerm(termMap, termParent, termIdentifier, termName):
	termFullName = ""
	
	if termParent:
		termFullName = termParent["fullName"] + "/" + termName
	
	term = {
		"identifier" : termIdentifier, 			
		"name" : termName,
		"fullName" : termFullName,
		"children" : []
	}
	termMap[termIdentifier] = term
	return term

def validateNotEmpty(value, name):
	if not value or not value.strip():
		raise "Field '%s' cannot be empty." % (name)

def validateSize(array, name, minSize, maxSize):
	if not array:
		raise "Field '%s' cannot be empty." % (name)
	elif len(array) < minSize or len(array) > maxSize:
		raise "Field '%s' must contain from %s up to %s items." % (name, minSize, maxSize)

def publish(tr, parameters, tableBuilder):
	paramExperiment = parameters["experiment"]
	paramSpace = parameters["space"]
	paramPublicationId = parameters["publicationId"]
	paramTitle = parameters["title"]
	paramAuthor = parameters["author"]
	paramAuthorEmail = parameters["authorEmail"]
	paramLicense = parameters["license"]
	paramNotes = parameters["notes"]
	paramMeshTerms = parameters["meshTerms"]

	meshTermMap = loadMeshTerms()
	
	validateNotEmpty(paramExperiment, "experiment")
	validateNotEmpty(paramSpace, "space")
	validateNotEmpty(paramPublicationId, "publicationId")
	validateNotEmpty(paramTitle, "title")
	validateNotEmpty(paramAuthor, "author")
	validateNotEmpty(paramAuthorEmail, "authorEmail")
	validateNotEmpty(paramLicense, "license")
	validateSize(paramMeshTerms, "meshTerms", 1, 5)
	
	originalExperiment = tr.getSearchService().getExperiment(paramExperiment)
	publicationSpace = getOrCreateSpace(tr, paramSpace)
	publicationProject = getOrCreateProject(tr, paramSpace, "DEFAULT")
	publicationExperiment = getOrCreateExperiment(tr, paramSpace, "DEFAULT", originalExperiment.getPermId())
	
	publicationExperiment.setPropertyValue("PUBLICATION_ID", paramPublicationId)
	publicationExperiment.setPropertyValue("PUBLICATION_TITLE", paramTitle)
	publicationExperiment.setPropertyValue("PUBLICATION_AUTHOR", paramAuthor)
	publicationExperiment.setPropertyValue("PUBLICATION_AUTHOR_EMAIL", paramAuthorEmail)
	publicationExperiment.setPropertyValue("PUBLICATION_LICENSE", paramLicense)
	publicationExperiment.setPropertyValue("PUBLICATION_NOTES", paramNotes)
	
	meshTermsPropertyValue = ""
	for paramMeshTerm in paramMeshTerms:
		meshTerm = meshTermMap.get(paramMeshTerm)
		meshTermsPropertyValue += meshTerm["name"] + ";" + meshTerm["identifier"] + "\n"
	publicationExperiment.setPropertyValue("PUBLICATION_MESH_TERMS", meshTermsPropertyValue)
	
	copyOrUpdateDataSets(tr, originalExperiment, publicationExperiment)

	return [None, None]

def getOrCreateSpace(tr, spaceCode):
	space = tr.getSearchService().getSpace(spaceCode)
	if space == None:
		space = tr.createNewSpace(spaceCode, None)
	return space

def getOrCreateProject(tr, spaceCode, projectCode):
	identifier = "/" + str(spaceCode) + "/" + str(projectCode);
	project = tr.getSearchService().getProject(identifier)
	if project == None:
		project = tr.createNewProject(identifier)
	return project
	
def getOrCreateExperiment(tr, spaceCode, projectCode, experimentCode):
	identifier = "/" + str(spaceCode) + "/" + str(projectCode) + "/" + str(experimentCode)
	experiment = tr.getExperimentForUpdate(identifier)
	if experiment == None:
		experiment = tr.createNewExperiment(identifier, "PUBLICATION")
	return experiment

def getMapping(originalExperiment, publicationExperiment):
	string = publicationExperiment.getPropertyValue("PUBLICATION_MAPPING")
	if string:
		try:
			mapping = json.loads(string)
			if mapping == None:
				mapping = {}
				
			mapping["experiment"] = { originalExperiment.getPermId() : publicationExperiment.getPermId() }
			if mapping.get("dataset") == None:
				mapping["dataset"] = {}
				
			return mapping
		except:
			raise Exception("Couldn't parse an existing publication '%s' mapping property value '%s'" % (publicationExperiment.getExperimentIdentifier(), string))
	else:
		mapping = {}
		mapping["experiment"] = { originalExperiment.getPermId() : publicationExperiment.getPermId() }
		mapping["dataset"] = {}
		return mapping

def setMapping(experiment, mapping):
	string = json.dumps(mapping)
	experiment.setPropertyValue("PUBLICATION_MAPPING", string)

def getDataSets(tr, experiment):
	experimentCriteria = SearchCriteria()
	experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, experiment.getPermId()))
	dataSetCriteria = SearchCriteria()
	dataSetCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentCriteria))
	return tr.getSearchService().searchForDataSets(dataSetCriteria)

def copyOrUpdateDataSets(tr, originalExperiment, publicationExperiment):
	
	originalDataSets = getDataSets(tr, originalExperiment)	
	publicationDataSets = getDataSets(tr, publicationExperiment)
	
	mapping = getMapping(originalExperiment, publicationExperiment)
	originalDataSetCodeToPublicationDataSetCodeMap = mapping["dataset"]
	originalDataSetCodeToPublicationDataSetMap = {}
	publicationDataSetsCodeToDataSetMap = {}
	
	for publicationDataSet in publicationDataSets:
		publicationDataSetsCodeToDataSetMap[publicationDataSet.getDataSetCode()] = tr.makeDataSetMutable(publicationDataSet)
	
	for originalDataSetCode, publicationDataSetCode in originalDataSetCodeToPublicationDataSetCodeMap.iteritems():
		originalDataSetCodeToPublicationDataSetMap[originalDataSetCode] = publicationDataSetsCodeToDataSetMap.get(publicationDataSetCode)
	
	for originalDataSet in originalDataSets:
		publicationDataSet = originalDataSetCodeToPublicationDataSetMap.get(originalDataSet.getDataSetCode())
		
		if publicationDataSet == None:
			
			if originalDataSet.isContainerDataSet():
				publicationDataSet = tr.createNewDataSet(originalDataSet.getDataSetType())
			else:
				publicationDataSet = tr.createNewDataSet("PUBLICATION_CONTAINER")
			
			publicationDataSet.setExperiment(publicationExperiment)
			publicationDataSet.setContainedDataSetCodes([originalDataSet.getDataSetCode()])
			
			originalDataSetCodeToPublicationDataSetCodeMap[originalDataSet.getDataSetCode()] = publicationDataSet.getDataSetCode()
			originalDataSetCodeToPublicationDataSetMap[originalDataSet.getDataSetCode()] = publicationDataSet

		if originalDataSet.isContainerDataSet():			
			for propertyCode in originalDataSet.getAllPropertyCodes():
				publicationDataSet.setPropertyValue(propertyCode, originalDataSet.getPropertyValue(propertyCode));
		
	setMapping(publicationExperiment, mapping)

def getProperty(tr, propertyName):
  properties = tr.getGlobalState().getThreadParameters().getThreadProperties()
  return properties.getProperty(propertyName)
