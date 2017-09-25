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

from java.lang import Thread
# python 2.6+ already has json library. On 2.5 we use JysonCodec.
try:
	import json
except:
	from com.xhaus.jyson import JysonCodec as json
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClauseAttribute
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import SearchOperator
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import DataSetKind
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from org.springframework.web.context.request import RequestContextHolder, RequestAttributes

INFO = 1
DEBUG = 2

def process(tr, parameters, tableBuilder):
	method = parameters.get("method")
	methodParameters = parameters.get("methodParameters")

	log("Started processing '%s' publication method" % method, INFO) 
	log("Setting user to '%s'" % userId, INFO) 
	tr.setUserId(userId);

	tableBuilder.addHeader("RESULT")
	row = tableBuilder.addRow()

	try:

		if method == "getSpaces":
			result = getSpaces(tr, methodParameters, tableBuilder)
		elif method == "getTags":
			result = getTags(tr, methodParameters, tableBuilder)
		elif method == "getMeshTermChildren":
			result = getMeshTermChildren(tr, methodParameters, tableBuilder)
		elif method == "publish":
			result = publish(tr, methodParameters, tableBuilder)
		else:
			raise "Method '%s' is not supported" % str(method);

		row.setCell("RESULT", result)
			
	finally:
		log("Finished processing '%s' publication method" % method, INFO)

def getSpaces(tr, parameters, tableBuilder):
	property = getProperty(tr, "published-spaces")
	codes = []
	
	if property and property.strip():
		codes = []
		
		for token in property.split(','):
			if token.strip():
				codes.append(token.strip())
	
	if codes:
		return json.dumps(codes)
	else:
		raise "No publication spaces have been configured. Please check that 'published-spaces' property has been properly set in the reporting plugin plugin.properties."	

def getTags(tr, parameters, tableBuilder):

	paramExperiment = parameters.get("experiment")
	validateNotEmpty(paramExperiment, "experiment")

	experiment = tr.getSearchService().getExperiment(paramExperiment)
	dataSets = getDataSetsByExperiment(tr, experiment)
	tagsMap = getDataSetsTags(tr, dataSets)
	tags = set()
	
	for dataSetCode, tagNames in tagsMap.iteritems():
		tags.update(tagNames)

	tagsList = list(tags)
	tagsList.sort()

	return json.dumps(tagsList)

def getMeshTermChildren(tr, parameters, tableBuilder):
	parentIdentifier = parameters.get("parent")
	filter = parameters.get("filter")
	
	if(not parentIdentifier):
		parentIdentifier = ""
		
	termMap = loadMeshTerms()
	
	parent = termMap.get(parentIdentifier)
	filterTokens = getMeshTermFilterTokens(filter)

	if parent:
		childrenData = []
		for child in parent["children"]:
			if matchesMeshTermFilterTokens(child, filterTokens): 
				childData = {
					"identifier" : child["identifier"],
					"name" : child["name"],
					"fullName" : child["fullName"],
					"hasChildren" : len(child["children"]) > 0
				}
				childrenData.append(childData)
		return json.dumps(childrenData)
	else:
		raise "No term have been found for '" + identifier + "'."	

def getMeshTermFilterTokens(filter):
	if filter and filter.strip():
		tokens = []
		for part in filter.lower().split():
			if part and part.strip():
				tokens.append(part.strip())
		print "Mesh terms filter tokens: " + str(tokens)
		return tokens
	else:
		return []

def matchesMeshTermFilterTokens(term, tokens):
	if tokens:
		name = term["name"].lower()
		count = 0

		for token in tokens:
			if token in name:
				count = count + 1
		
		if count == len(tokens):
			return True		
		else:
			children = term["children"]
			for child in children:
				if matchesMeshTermFilterTokens(child, tokens):
					return True
		return False
	else:
		return True

def loadMeshTerms():

	cachedTermsSessionAttribute = "publication.meshTerms"
	cachedTerms = RequestContextHolder.getRequestAttributes().getAttribute(cachedTermsSessionAttribute, RequestAttributes.SCOPE_SESSION)
	
	if cachedTerms:
		log("Didn't have to load the mesh terms - found them in a session cache.", INFO)
		return cachedTerms
	
	log("Loading mesh terms - start time: " + str(datetime.datetime.utcnow()), INFO)
	
	binPath = str(sys.path[-1]) + "/terms/mesh_terms.bin"
	binFile = open(binPath)
	
	try:
		termMap = {}
		
		addRootMeshTerm(termMap)
		
		log("Root terms: " + str(termMap), INFO)
		
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
		log("Loading mesh terms - finish time: " + str(datetime.datetime.utcnow()), INFO)
		binFile.close()

def loadMeshTermsVersion():
	
	cachedVersionSessionAttribute = "publication.meshTermsVersion"
	cachedVersion = RequestContextHolder.getRequestAttributes().getAttribute(cachedVersionSessionAttribute, RequestAttributes.SCOPE_SESSION)
	
	if cachedVersion:
		log("Didn't have to load the mesh terms version - found it in a session cache.", INFO)
		return cachedVersion
	
	log("Loading mesh terms version - start time: " + str(datetime.datetime.utcnow()), INFO)
	
	versionPath = str(sys.path[-1]) + "/terms/version.txt"
	versionFile = open(versionPath)
	
	try:
		version = versionFile.read()
		RequestContextHolder.getRequestAttributes().setAttribute(cachedVersionSessionAttribute, version, RequestAttributes.SCOPE_SESSION)
		return version
	finally:
		log("Loading mesh terms version - finish time: " + str(datetime.datetime.utcnow()), INFO)
		versionFile.close()

def addRootMeshTerm(termMap):

	root = addMeshTerm(termMap, None, "", "")
	
	binPath = str(sys.path[-1]) + "/terms/root_mesh_terms.bin"
	binFile = open(binPath)

	log("Loading root mesh terms - start time: " + str(datetime.datetime.utcnow()), INFO)
	try:
		lines = binFile.readlines()
		for line in lines:
			separatorIndex = line.rfind(";")

			termName = line[0:separatorIndex].strip()
			termIdentifier = line[separatorIndex + 1:len(line)].strip()
			
			term = addMeshTerm(termMap, root, termIdentifier, termName)
			root["children"].append(term)
		
	finally:
		log("Loading root mesh terms - finish time: " + str(datetime.datetime.utcnow()), INFO)
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
	paramExperiment = parameters.get("experiment")
	paramSpace = parameters.get("space")
	paramPublicationId = parameters.get("publicationId")
	paramTitle = parameters.get("title")
	paramAuthor = parameters.get("author")
	paramAuthorEmail = parameters.get("authorEmail")
	paramLicense = parameters.get("license")
	paramNotes = parameters.get("notes")
	paramMeshTerms = parameters.get("meshTerms")
	paramTag = parameters.get("tag")

	meshTermMap = loadMeshTerms()
	meshTermVersion = loadMeshTermsVersion()
	
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
	publicationExperiment.setPropertyValue("PUBLICATION_MESH_TERMS_DATABASE", meshTermVersion)
	
	copyOrUpdateDataSets(tr, originalExperiment, publicationExperiment, paramTag)

	return publicationExperiment.getPermId()

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
			raise "Couldn't parse an existing publication '%s' mapping property value '%s'" % (publicationExperiment.getExperimentIdentifier(), string)
	else:
		mapping = {}
		mapping["experiment"] = { originalExperiment.getPermId() : publicationExperiment.getPermId() }
		mapping["dataset"] = {}
		return mapping

def setMapping(experiment, mapping):
	string = json.dumps(mapping)
	experiment.setPropertyValue("PUBLICATION_MAPPING", string)

def getDataSetsToPublish(tr, experiment, tag):
	log("getDataSetsToPublish - experiment: " + str(experiment.getExperimentIdentifier()) + ", tag: " + str(tag), DEBUG)

	dataSets = getDataSetsByExperiment(tr, experiment);
	containerMap = getDataSetsContainers(tr, dataSets)
	tagsMap = {}

	if tag:
		tagsMap = getDataSetsTags(tr, dataSets)

	dataSetsToPublish = []

	for dataSet in dataSets:
		if isPublishable(dataSet, tagsMap, tag, experiment) and not hasPublishableContainer(tr, dataSet, tagsMap, tag, experiment, containerMap):
			dataSetsToPublish.append(dataSet)

	log("getDataSetsToPublish - found: " + str(len(dataSetsToPublish)), DEBUG) 

	return dataSetsToPublish

def isPublishable(dataSet, tagsMap, tag, experiment):
	log("isPublishable - dataSet: " + str(dataSet.getDataSetCode()), DEBUG)

	if tag:
		dataSetTags = tagsMap.get(dataSet.getDataSetCode())
		if not dataSetTags or not tag in dataSetTags:
			log("isPublishable - False (does not match tag)", DEBUG)
			return False

	if not experiment.equals(dataSet.getExperiment()):
		log("isPublishable - False (does not match experiment)", DEBUG)
		return False

	log("isPublishable - True", DEBUG)
	return True

def hasPublishableContainer(tr, dataSet, tagsMap, tag, experiment, containerMap):
	log("hasPublishableContainer - dataSet: " + str(dataSet.getDataSetCode()), DEBUG)
	
	if dataSet.getContainerDataSets():
		for containerCode in dataSet.getContainerDataSets():
			container = containerMap.get(containerCode)

			log("hasPublishableContainer - containerCode: " + containerCode + ", container: " + str(container), DEBUG)

			if isPublishable(container, tagsMap, tag, experiment):
				log("hasPublishableContainer - True", DEBUG)
				return True
			elif hasPublishableContainer(tr, container, tagsMap, tag, experiment, containerMap):
				log("hasPublishableContainer - True", DEBUG)
				return True
		log("hasPublishableContainer - False", DEBUG)
		return False
	else:
		log("hasPublishableContainer - False", DEBUG)
		return False

def getDataSetsByExperiment(tr, experiment):
	log("getDataSetsByExperiment - experiment: " + str(experiment.getExperimentIdentifier()), DEBUG)
	
	experimentCriteria = SearchCriteria()
	experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, experiment.getPermId()))
	dataSetCriteria = SearchCriteria()
	dataSetCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentCriteria))

	dataSets = tr.getSearchService().searchForDataSets(dataSetCriteria)
	log("getDataSetsByExperiment found: " + str(len(dataSets)), DEBUG)
	return dataSets

def getDataSetsByCodes(tr, codes):
	log("getDataSetsByCodes - codes size: " + str(len(codes)) + ", codes: " + str(codes), DEBUG)
	
	if not codes:
		log("getDataSetsByCodes - found: 0", DEBUG)
		return []

	criteria = SearchCriteria()
	criteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES)
	for code in codes:
		criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code))

	dataSets = tr.getSearchService().searchForDataSets(criteria)
	log("getDataSetsByCodes found: " + str(len(dataSets)), DEBUG)
	return dataSets

def getDataSetsContainers(tr, dataSets):
	log("getDataSetsContainers data sets size: " + str(len(dataSets)), DEBUG)

	containerList = dataSets;
	containerMap = {}
	
	allCodes = set()
	missingCodes = set()
	
	while containerList:
		nextLevelCodes = set()
		for container in containerList:
			containerMap[container.getDataSetCode()] = container

			log("getDataSetsContainers - dataSet: " + container.getDataSetCode() + " has containers: " + str(container.getContainerDataSets()), DEBUG)

			for containerCode in container.getContainerDataSets():
				nextLevelCodes.add(containerCode)
				allCodes.add(containerCode)

		containerList = getDataSetsByCodes(tr, nextLevelCodes)

		if len(containerList) != len(nextLevelCodes):
			foundCodes = set()
			for container in containerList:
				foundCodes.add(container.getDataSetCode())
			missingCodes.update(nextLevelCodes.difference(foundCodes))	

	if missingCodes:
		raise "%s out of %s data sets that are related to the published experiment haven't been indexed yet: %s. Please try to publish the experiment later." % (str(len(missingCodes)), str(len(allCodes)), str(missingCodes))

	log("getDataSetsContainers - containerMap size: " + str(len(containerMap)), DEBUG)

	return containerMap

def getDataSetsTags(tr, dataSets):
	log("getDataSetsTags data sets size: " + str(len(dataSets)), DEBUG)
	
	dataSetToTagsMap = tr.getSearchService().listMetaprojectsForEntities(dataSets)
	dataSetCodeToTagNameMap = {}
	
	for entry in dataSetToTagsMap.entrySet():
		dataSet = entry.getKey()
		tags = entry.getValue()
	
		dataSetCode = dataSet.getDataSetCode()
		tagNames = set()
	
		for tag in tags:
			tagNames.add(tag.getName())

		if tagNames:
			dataSetCodeToTagNameMap[dataSetCode] = tagNames

	log("getDataSetsTags found: " + str(dataSetCodeToTagNameMap), DEBUG)
	
	return dataSetCodeToTagNameMap

def copyOrUpdateDataSets(tr, originalExperiment, publicationExperiment, tag):
	log("copyOrUpdateDataSets - originalExperiment: " + str(originalExperiment.getExperimentIdentifier()) + ", publicationExperiment: " + str(publicationExperiment.getExperimentIdentifier()), DEBUG) 
	
	originalDataSets = getDataSetsToPublish(tr, originalExperiment, tag)	
	publicationDataSets = getDataSetsByExperiment(tr, publicationExperiment)
	
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
			publicationDataSet.setDataSetKind(DataSetKind.CONTAINER);

			log("copyOrUpdateDataSets - originalDataSet: " + str(originalDataSet.getDataSetCode()) + ", publicationDataSet: " + str(publicationDataSet.getDataSetCode()), DEBUG) 
			
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
 
def log(message, level):
	print str(datetime.datetime.utcnow()) + " [" + Thread.currentThread().getName() + "] - " + message
