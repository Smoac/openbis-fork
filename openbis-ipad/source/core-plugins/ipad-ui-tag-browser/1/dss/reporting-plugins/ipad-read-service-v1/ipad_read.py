from datetime import datetime
from java.util import Date
from java.util import HashMap
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria
from ch.systemsx.cisd.openbis.ipad.v2.server import RootRequestHandler, DrillRequestHandler, NavigationRequestHandler, DetailRequestHandler, SearchRequestHandler, IpadServiceUtilities
from ch.systemsx.cisd.openbis.ipad.v2.server import IRequestHandlerFactory, RequestHandlerDispatcher
from com.fasterxml.jackson.databind import ObjectMapper

##########
# Request
##########

def getEntitiesParameter(handler):
	entities = handler.getEntitiesParameter();
	for entity in entities:
		refcon = entity['REFCON']
		if type(refcon) == str or type(refcon) == unicode:
			refconMap = ObjectMapper().readValue(refcon, HashMap().getClass())
			entity['REFCON'] = refconMap
	return entities

###################
# Dictionary types
###################

# Experiment
ROOT_EXPERIMENT = 'ROOT_EXPERIMENT'
DETAIL_EXPERIMENT = 'DETAIL_EXPERIMENT'

# Sample
ROOT_SAMPLE = 'ROOT_SAMPLE'
DRILL_EXPERIMENT_SAMPLE = 'DRILL_EXPERIMENT_SAMPLE'
DRILL_SAMPLE_PARENT = 'DRILL_SAMPLE_PARENT'
DRILL_SAMPLE_CHILD = 'DRILL_SAMPLE_CHILD'
DETAIL_SAMPLE = 'DETAIL_SAMPLE'
SEARCH_SAMPLE = 'SEARCH_SAMPLE'

# Data set
ROOT_DATA_SET = 'ROOT_DATA_SET'
DRILL_EXPERIMENT_DATA_SET = 'DRILL_EXPERIMENT_DATA_SET'
DETAIL_DATA_SET = 'DETAIL_DATA_SET'
SEARCH_DATA_SET = 'SEARCH_DATA_SET'

# Material
ROOT_MATERIAL = 'ROOT_MATERIAL'
DETAIL_MATERIAL = 'DETAIL_MATERIAL'

###############
# Dictionaries
###############

def createTagDictionary(tag, children):
	dictionary = {}
	dictionary['PERM_ID'] = getTagIPadId(tag)
	dictionary['CATEGORY'] = 'Tag'
	dictionary['SUMMARY_HEADER'] = tag.getName()
	dictionary['SUMMARY'] = None
	dictionary['IDENTIFIER'] = 'Tag'
	dictionary['ROOT_LEVEL'] = True
	dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue(children)

	refcon = {}
	refcon['ENTITY_TYPE'] =  'TAG'
	refcon['NAME'] =  tag.getName()
	dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)
	
	return dictionary
	
def createTagDetailedDictionary(tag):
	dictionary = createTagDictionary(tag, [])

	properties = []
	if tag.getDescription():
		properties.append(getProperty("#DESCRIPTION", "Description", tag.getDescription()))
	properties.append(getTimestampProperty())

	dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties)
	return dictionary

def createExperimentDictionaries(experiments, dictionaryType):
	if not experiments:
		return []

	experimentIdentifiers = [experiment.getExperimentIdentifier() for experiment in experiments]
	experimentSamplesMap = {}
	experimentDataSetsMap = {}
	experimentPropertyDefinitionsMap = {}
	
	relationsDictionaryTypes = [ROOT_EXPERIMENT]
	if dictionaryType in relationsDictionaryTypes:
		experimentSamplesMap = getExperimentSamplesMap(experimentIdentifiers)
		experimentDataSetsMap = getExperimentDataSetsMap(experimentIdentifiers)
	
	propertiesDictionaryTypes = [DETAIL_EXPERIMENT]
	if dictionaryType in propertiesDictionaryTypes:
		experimentTypes = [experiment.getExperimentType() for experiment in experiments]
		experimentPropertyDefinitionsMap = getPropertyDefinitionsMap(experimentTypes, searchService.listPropertiesDefinitionsForExperimentType)
	
	dictionaries = []
	
	for experiment in experiments:
		dictionary = {}
		dictionary['PERM_ID'] = getExperimentIPadId(experiment, dictionaryType)
		dictionary['CATEGORY'] = 'Experiment (' + experiment.getExperimentType() + ')'
		dictionary['SUMMARY_HEADER'] = experiment.getExperimentIdentifier()
		dictionary['SUMMARY'] = None
		dictionary['IDENTIFIER'] = 'Experiment'
		dictionary['IMAGES'] = IpadServiceUtilities.jsonEncodedValue({})
		dictionary['ROOT_LEVEL'] = None

		refcon = {}
		refcon['ENTITY_TYPE'] =  'EXPERIMENT'
		# there is no way to get an experiment by a permanent id
		refcon['IDENTIFIER'] = experiment.getExperimentIdentifier()
		dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)
		
		if dictionaryType in relationsDictionaryTypes:
			experimentSamples = experimentSamplesMap.get(experiment.getExperimentIdentifier(), [])
			experimentDataSets = experimentDataSetsMap.get(experiment.getExperimentIdentifier(), [])
			children = []
			children.extend([ getSampleIPadId(experimentSample, DRILL_EXPERIMENT_SAMPLE) for experimentSample in experimentSamples ])
			children.extend([ getDataSetIPadId(experimentDataSet, DRILL_EXPERIMENT_DATA_SET) for experimentDataSet in experimentDataSets ])
			dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue(children)

		if dictionaryType in propertiesDictionaryTypes:
			properties = []
			properties.append(getProperty("#TYPE", "Type", experiment.getExperimentType()))
			properties.append(getProperty("#PERM_ID", "Perm ID", experiment.getPermId()))
			propertyDefinitions = experimentPropertyDefinitionsMap.get(experiment.getExperimentType())			
			properties.extend(getProperties(experiment, propertyDefinitions))
			properties.append(getTimestampProperty())
			dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties)
			
		dictionaries.append(dictionary)
	
	return dictionaries

def createSampleDictionaries(samples, dictionaryType):
	if not samples:
		return []

	samplePermIds = [sample.getPermId() for sample in samples]
	sampleParentsMap = {}
	sampleChildrenMap = {}
	samplePropertyDefinitionsMap = {}
	
	relationsDictionaryTypes = [ROOT_SAMPLE, DRILL_EXPERIMENT_SAMPLE, DRILL_SAMPLE_PARENT, DRILL_SAMPLE_CHILD, SEARCH_SAMPLE]
	sampleParentsMap = getSampleParentsMap(samplePermIds)
	sampleChildrenMap = getSampleChildrenMap(samplePermIds)
	
	propertiesDictionaryTypes = [DETAIL_SAMPLE]
	sampleTypes = [sample.getSampleType() for sample in samples]
	samplePropertyDefinitionsMap = getPropertyDefinitionsMap(sampleTypes, searchService.listPropertiesDefinitionsForSampleType)
	
	dictionaries = []
	
	for sample in samples:
		dictionary = {}
		dictionary['PERM_ID'] = getSampleIPadId(sample, dictionaryType)
		
		if dictionaryType == DRILL_SAMPLE_PARENT:
			dictionary['CATEGORY'] = 'Parent (' + sample.getSampleType() + ')'
		elif dictionaryType == DRILL_SAMPLE_CHILD:
			dictionary['CATEGORY'] = 'Child (' + sample.getSampleType() + ')'
		else:
			dictionary['CATEGORY'] = 'Sample (' + sample.getSampleType() + ')'
			
		dictionary['SUMMARY_HEADER'] = sample.getSampleIdentifier()
		dictionary['SUMMARY'] = None
		dictionary['IDENTIFIER'] = 'Sample'
		dictionary['IMAGES'] = IpadServiceUtilities.jsonEncodedValue({})
		dictionary['ROOT_LEVEL'] = None
		
		refcon = {}
		refcon['ENTITY_TYPE'] =  'SAMPLE'
		refcon['PERM_ID'] = sample.getPermId()
		refcon['DICTIONARY_TYPE'] = dictionaryType
		dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)

		sampleParents = sampleParentsMap.get(sample.getPermId(), [])
		sampleChildren = sampleChildrenMap.get(sample.getPermId(), [])
		children = []
		children.extend([ getSampleIPadId(sampleParent, DRILL_SAMPLE_PARENT) for sampleParent in sampleParents ])
		children.extend([ getSampleIPadId(sampleChild, DRILL_SAMPLE_CHILD) for sampleChild in sampleChildren ])
		dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue(children)

		properties = []
		properties.append(getProperty("#TYPE", "Type", sample.getSampleType()))
		properties.append(getProperty("#PERM_ID", "Perm ID", sample.getPermId()))
		if sample.getExperiment():
			properties.append(getProperty("#EXPERIMENT", "Experiment", sample.getExperiment().getExperimentIdentifier()))
		propertyDefinitions = samplePropertyDefinitionsMap.get(sample.getSampleType())				
		properties.extend(getProperties(sample, propertyDefinitions))
		properties.append(getTimestampProperty())
		dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties)
			
		dictionaries.append(dictionary)
	
	return dictionaries

def createDataSetDictionaries(dataSets, dictionaryType):
	if not dataSets:
		return []

	dataSetPropertyDefinitionsMap = {}
	
	propertiesDictionaryTypes = [DETAIL_DATA_SET]	
	if dictionaryType in propertiesDictionaryTypes:
		dataSetTypes = [dataSet.getDataSetType() for dataSet in dataSets]
		dataSetPropertyDefinitionsMap = getPropertyDefinitionsMap(dataSetTypes, searchService.listPropertiesDefinitionsForDataSetType)
	
	dictionaries = []
	
	for dataSet in dataSets:
		dictionary = {}
		dictionary['PERM_ID'] = getDataSetIPadId(dataSet, dictionaryType)
		dictionary['CATEGORY'] = 'Data Set (' + dataSet.getDataSetType() + ')'	
		dictionary['SUMMARY_HEADER'] = dataSet.getDataSetCode()
		dictionary['SUMMARY'] = None
		dictionary['IDENTIFIER'] = 'Data Set'
		dictionary['IMAGES'] = IpadServiceUtilities.jsonEncodedValue({})
		dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue([])
		dictionary['ROOT_LEVEL'] = None
		
		refcon = {}
		refcon['ENTITY_TYPE'] =  'DATA_SET'
		refcon['CODE'] = dataSet.getDataSetCode()
		dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)

		if dictionaryType in propertiesDictionaryTypes:
			properties = []
			properties.append(getProperty("#TYPE", "Type", dataSet.getDataSetType()))
			if dataSet.getExperiment():
				properties.append(getProperty("#EXPERIMENT", "Experiment", dataSet.getExperiment().getExperimentIdentifier()))
			if dataSet.getSample():
				properties.append(getProperty("#SAMPLE", "Sample", dataSet.getSample().getSampleIdentifier()))
			properties.append(getProperty("#FiLE_TYPE", "File Type", dataSet.getFileFormatType()))
			propertyDefinitions = dataSetPropertyDefinitionsMap.get(dataSet.getDataSetType())
			properties.extend(getProperties(dataSet, propertyDefinitions))
			properties.append(getTimestampProperty())
			dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties)
			
		dictionaries.append(dictionary)
	
	return dictionaries
	
def createMaterialDictionaries(materials, dictionaryType):
	if not materials:
		return []

	materialPropertyDefinitionsMap = {}
	
	propertiesDictionaryTypes = [DETAIL_MATERIAL]	
	if dictionaryType in propertiesDictionaryTypes:
		materialTypes = [material.getMaterialType() for material in materials]
		materialPropertyDefinitionsMap = getPropertyDefinitionsMap(materialTypes, searchService.listPropertiesDefinitionsForMaterialType)
	
	dictionaries = []
	
	for material in materials:
		dictionary = {}
		dictionary['PERM_ID'] = getMaterialIPadId(material, dictionaryType)
		dictionary['CATEGORY'] = 'Material (' + material.getMaterialType() + ')'
		dictionary['SUMMARY_HEADER'] = material.getMaterialIdentifier()
		dictionary['SUMMARY'] = None
		dictionary['IDENTIFIER'] = 'Material'
		dictionary['IMAGES'] = IpadServiceUtilities.jsonEncodedValue({})
		dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue([])
		dictionary['ROOT_LEVEL'] = None
		
		refcon = {}
		refcon['ENTITY_TYPE'] =  'MATERIAL'
		refcon['CODE'] = material.getCode()
		refcon['TYPE_CODE'] = material.getMaterialType();
		dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)

		if dictionaryType in propertiesDictionaryTypes:
			properties = []
			properties.append(getProperty("#TYPE", "Type", material.getMaterialType()))
			propertyDefinitions = materialPropertyDefinitionsMap.get(material.getMaterialType())
			properties.extend(getProperties(material, propertyDefinitions))
			properties.append(getTimestampProperty())
			dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties)
			
		dictionaries.append(dictionary)
	
	return dictionaries	

#################
# Entity IPad Id
#################

def getTagIPadId(tag):
	return 'TAG.' + tag.getName()	

def getExperimentIPadId(experiment, dictionaryType):
	return 'EXPERIMENT.' + experiment.getPermId() + '.' + str(dictionaryType)
	
def getSampleIPadId(sample, dictionaryType):
	return 'SAMPLE.' + sample.getPermId() + '.' + str(dictionaryType)

def getDataSetIPadId(dataSet, dictionaryType):
	return 'DATA_SET.' + dataSet.getDataSetCode() + '.' + str(dictionaryType)
	
def getMaterialIPadId(material, dictionaryType):
	return 'MATERIAL.' + material.getMaterialIdentifier() + '.' + str(dictionaryType)

#########
# Entity
#########

def getExperiment(identifier):
	# there is no way to get an experiment by a permanent id
	return searchService.getExperiment(identifier)
	
def getSample(permId):
	criteria = SearchCriteria()
	criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
	criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.PERM_ID, permId))
	samples = searchService.searchForSamples(criteria)
	if samples:
		return samples[0]
	else:
		return None

def getDataSet(code):
	criteria = SearchCriteria()
	criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
	criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.CODE, code))
	datasets = searchService.searchForDataSets(criteria)
	if datasets:
		return datasets[0]
	else:
		return None

def getMaterial(code, typeCode):
	return searchService.getMaterial(code, typeCode)
	
def getTag(name):
	return searchService.getMetaproject(name)
	
###################
# Entity Relations
###################

def getExperimentEntitiesMap(experimentIdentifiers, searchFunction):
	experimentCriteria = SearchCriteria()
	experimentCriteria.setOperator(experimentCriteria.SearchOperator.MATCH_ANY_CLAUSES)
	
	for experimentIdentifier in experimentIdentifiers:
		experimentTokens = experimentIdentifier.split('/');
		experimentCode = experimentTokens[len(experimentTokens) - 1]
		experimentCriteria.addMatchClause(experimentCriteria.MatchClause.createAttributeMatch(experimentCriteria.MatchClauseAttribute.CODE, experimentCode))

	entityCriteria = SearchCriteria()
	entityCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentCriteria))

	entities = searchFunction(entityCriteria)
	
	# as there is no way to search for entities by experiment identifier (only by experiment code)
	# we have to filter out ones that have an experiment with the same code but a different identifier
	entities = [ entity for entity in entities if entity.getExperiment().getExperimentIdentifier() in experimentIdentifiers]	
	
	experimentIdentifierToEntitiesMap = {}
	
	for entity in entities:
		experimentIdentifier = entity.getExperiment().getExperimentIdentifier()
		experimentEntities = experimentIdentifierToEntitiesMap.get(experimentIdentifier)
		if not experimentEntities:
			experimentEntities = []
			experimentIdentifierToEntitiesMap[experimentIdentifier] = experimentEntities
		experimentEntities.append(entity)
		
	return experimentIdentifierToEntitiesMap

def getExperimentSamplesMap(experimentIdentifiers):
	return getExperimentEntitiesMap(experimentIdentifiers, searchService.searchForSamples)
	
def getExperimentSamples(experimentIdentifier):
	return getExperimentSamplesMap([experimentIdentifier]).get(experimentIdentifier, [])

def getExperimentDataSetsMap(experimentIdentifiers):
	return getExperimentEntitiesMap(experimentIdentifiers, searchService.searchForDataSets)

def getExperimentDataSets(experimentIdentifier):
	return getExperimentDataSetsMap([experimentIdentifier]).get(experimentIdentifier, [])

def getSampleParentsMap(samplePermIds):
	criteria = SearchCriteria()
	criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
	
	for samplePermId in samplePermIds:
		criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.PERM_ID, samplePermId))

	samples = searchService.searchForSamples(criteria)
	
	parentIdentifiers = []
	for sample in samples:
		parentIdentifiers.extend(sample.getParentSampleIdentifiers())
	
	parentCriteria = SearchCriteria()
	parentCriteria.setOperator(parentCriteria.SearchOperator.MATCH_ANY_CLAUSES)
	
	for parentIdentifier in parentIdentifiers:
		parentTokens = parentIdentifier.split('/');
		parentCode = parentTokens[len(parentTokens) - 1]
		parentCriteria.addMatchClause(parentCriteria.MatchClause.createAttributeMatch(parentCriteria.MatchClauseAttribute.CODE, parentCode))

	parents = searchService.searchForSamples(parentCriteria)
	parents = [parent for parent in parents if parent.getSampleIdentifier() in parentIdentifiers]

	parentIdentifierToParentMap = {}
	for parent in parents:
		parentIdentifierToParentMap[parent.getSampleIdentifier()] = parent
		
	samplePermIdToParentsMap = {}

	for sample in samples:
		sampleParents = []
		for parentIdentifier in sample.getParentSampleIdentifiers():
			parent = parentIdentifierToParentMap.get(parentIdentifier)
			if parent:
				sampleParents.append(parent)
		samplePermIdToParentsMap[sample.getPermId()] = sampleParents
		
	return samplePermIdToParentsMap

def getSampleParents(samplePermId):
	return getSampleParentsMap([samplePermId]).get(samplePermId, [])

def getSampleChildrenMap(samplePermIds):
	parentCriteria = SearchCriteria()
	parentCriteria.setOperator(parentCriteria.SearchOperator.MATCH_ANY_CLAUSES)
	
	for samplePermId in samplePermIds:
		parentCriteria.addMatchClause(parentCriteria.MatchClause.createAttributeMatch(parentCriteria.MatchClauseAttribute.PERM_ID, samplePermId))
	
	childCriteria = SearchCriteria()
	childCriteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(parentCriteria))
	
	parents = searchService.searchForSamples(parentCriteria)
	children = searchService.searchForSamples(childCriteria)

	parentIdentifierToPermIdMap = {}
	for parent in parents:
		parentIdentifierToPermIdMap[parent.getSampleIdentifier()] = parent.getPermId()
	parentPermIdToChildrenMap = {}
	
	for child in children:
		parentIdentifiers = child.getParentSampleIdentifiers()
		
		for parentIdentifier in parentIdentifiers:
			parentPermId = parentIdentifierToPermIdMap.get(parentIdentifier)
			parentChildren = parentPermIdToChildrenMap.get(parentPermId)
			if not parentChildren:
				parentChildren = []
				parentPermIdToChildrenMap[parentPermId] = parentChildren
			parentChildren.append(child)
	return parentPermIdToChildrenMap
	
def getSampleChildren(samplePermId):
	return getSampleChildrenMap([samplePermId]).get(samplePermId, [])

####################
# Entity Properties
####################

def getPropertyDefinitionsMap(typeCodes, searchFunction):
	typeCodeToDefinitionsMap = {}
	
	for typeCode in typeCodes:
		definitions = list(searchFunction(typeCode))
		definitions.sort(lambda x, y: cmp(x.getPositionInForms(), y.getPositionInForms()))
		typeCodeToDefinitionsMap[typeCode] = definitions

	return typeCodeToDefinitionsMap

def getProperties(entity, propertyDefinitions):
	properties = []
	for propertyDefinition in propertyDefinitions:
		propertyValue = entity.getPropertyValue(propertyDefinition.getPropertyTypeCode())
		if propertyValue:
			properties.append(getProperty(
				propertyDefinition.getPropertyTypeCode(),
				propertyDefinition.getPropertyTypeLabel(),
				propertyValue
			))
	return properties
	
def getProperty(code, label, value):
	return {
		'key' : code,
		'label' : label,
		'value' : value
	}
	
def getTimestampProperty():
	return getProperty("#TIMESTAMP", "Timestamp", datetime.today().strftime('%Y-%m-%d %H:%M:%S'))
	
###################
# Request Handlers
###################

class TagNavigationRequestHandler(NavigationRequestHandler):
	"""Handler for the NAVIGATION request."""
	
	def addDataRows(self):
		tags = self.searchService.listMetaprojects()
		self.addRows([createTagDictionary(tag, []) for tag in tags])

class TagRootRequestHandler(RootRequestHandler):
	"""Handler for the ROOT request."""
	
	def addDataRows(self):
		tagsPermIdsAndRefcons = getEntitiesParameter(self)
		
		if tagsPermIdsAndRefcons:
			if tagsPermIdsAndRefcons[0]['REFCON']['ENTITY_TYPE'] != 'TAG':
				return
			tagName = tagsPermIdsAndRefcons[0]['REFCON']['NAME']
			tagAssignments = self.searchService.getMetaprojectAssignments(tagName)
			tag = tagAssignments.getMetaproject()
			tagChildren = []

			experiments = tagAssignments.getExperiments();
			samples = tagAssignments.getSamples();
			dataSets = tagAssignments.getDataSets();
			materials = tagAssignments.getMaterials();
			
			for experiment in experiments:
				tagChildren.append(getExperimentIPadId(experiment, ROOT_EXPERIMENT))
			for sample in samples:
				tagChildren.append(getSampleIPadId(sample, ROOT_SAMPLE))
			for dataSet in dataSets:
				tagChildren.append(getDataSetIPadId(dataSet, ROOT_DATA_SET))
			for material in materials:
				tagChildren.append(getMaterialIPadId(material, ROOT_MATERIAL))
			
			self.addRows([createTagDictionary(tag, tagChildren)])
			self.addRows(createExperimentDictionaries(experiments, ROOT_EXPERIMENT))
			self.addRows(createSampleDictionaries(samples, ROOT_SAMPLE))
			self.addRows(createDataSetDictionaries(dataSets, ROOT_DATA_SET))
			self.addRows(createMaterialDictionaries(materials, ROOT_MATERIAL))

class TagDrillRequestHandler(DrillRequestHandler):
	"""Handler for the DRILL request."""

	def addDataRows(self):
		entitiesPermIdsAndRefcons = getEntitiesParameter(self)

		if entitiesPermIdsAndRefcons:
			entityRefcon = entitiesPermIdsAndRefcons[0]['REFCON']
			entityType = entityRefcon['ENTITY_TYPE'];
			
			if 'EXPERIMENT' == entityType:
				experimentIdentifier = entityRefcon['IDENTIFIER']
				experimentSamples = getExperimentSamples(experimentIdentifier)
				experimentDataSets = getExperimentDataSets(experimentIdentifier)
				self.addRows(createSampleDictionaries(experimentSamples, DRILL_EXPERIMENT_SAMPLE))
				self.addRows(createDataSetDictionaries(experimentDataSets, DRILL_EXPERIMENT_DATA_SET))
			if 'SAMPLE' == entityType:
				samplePermId = entityRefcon['PERM_ID']
				sampleParents = getSampleParents(samplePermId)
				sampleChildren = getSampleChildren(samplePermId)
				self.addRows(createSampleDictionaries(sampleParents, DRILL_SAMPLE_PARENT))
				self.addRows(createSampleDictionaries(sampleChildren, DRILL_SAMPLE_CHILD))

class TagDetailRequestHandler(DetailRequestHandler):
	"""Handler for the DETAIL request."""

	def addDataRows(self):
		entitiesPermIdsAndRefcons = getEntitiesParameter(self)
		
		if entitiesPermIdsAndRefcons:
			entityRefcon = entitiesPermIdsAndRefcons[0]['REFCON']
			entityType = entityRefcon['ENTITY_TYPE'];
			dictionaryType = entityRefcon['DICTIONARY_TYPE'];
			
			if 'EXPERIMENT' == entityType:
				experiment = getExperiment(entityRefcon['IDENTIFIER'])
				self.addRows(createExperimentDictionaries([experiment], dictionaryType))
			if 'SAMPLE' == entityType:
				sample = getSample(entityRefcon['PERM_ID'])
				self.addRows(createSampleDictionaries([sample], dictionaryType))
			if 'DATA_SET' == entityType:
				dataSet = getDataSet(entityRefcon['CODE'])
				self.addRows(createDataSetDictionaries([dataSet], dictionaryType))
			if 'MATERIAL' == entityType:
				material = getMaterial(entityRefcon['CODE'], entityRefcon['TYPE_CODE'])
				self.addRows(createMaterialDictionaries([material], dictionaryType))
			if 'TAG' == entityType:
				tag = getTag(entityRefcon['NAME'])
				self.addRows([createTagDetailedDictionary(tag)])

class TagSearchRequestHandler(SearchRequestHandler):
	"""Handler for the SEARCH request"""

	def addDataRows(self):
		criteria = self.trySearchCriteria()
		if criteria:
			# there is no way to search for experiments and materials
			
			samples = self.searchService.searchForSamples(criteria)
			self.addRows(createSampleDictionaries(samples, SEARCH_SAMPLE))
			
			dataSets = self.searchService.searchForDataSets(criteria)
			self.addRows(createDataSetDictionaries(dataSets, SEARCH_DATA_SET))

####################
# Request Factories
####################

class NavigationRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		return TagNavigationRequestHandler(parameters, builder, searchService)
		
class RootRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		return TagRootRequestHandler(parameters, builder, searchService)
		
class DrillRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		return TagDrillRequestHandler(parameters, builder, searchService)

class DetailRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		return TagDetailRequestHandler(parameters, builder, searchService)

class SearchRequestHandlerFactory(IRequestHandlerFactory):
	def createRequestHandler(self, parameters, builder, searchService):
		return TagSearchRequestHandler(parameters, builder, searchService)

############
# Aggregate
############

def aggregate(parameters, builder):
	dispatcher = RequestHandlerDispatcher()
	dispatcher.navigationRequestHandlerFactory = NavigationRequestHandlerFactory()
	dispatcher.rootRequestHandlerFactory = RootRequestHandlerFactory()
	dispatcher.drillRequestHandlerFactory = DrillRequestHandlerFactory()
	dispatcher.detailRequestHandlerFactory = DetailRequestHandlerFactory()
	dispatcher.searchRequestHandlerFactory = SearchRequestHandlerFactory()
	dispatcher.dispatch(parameters, builder, searchService)