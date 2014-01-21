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

###############
# Dictionaries
###############

def createTagDictionary(tag, children):
	dictionary = {}
	dictionary['PERM_ID'] = 'TAG.' + tag.getName()
	dictionary['CATEGORY'] = ' '
	dictionary['SUMMARY_HEADER'] = tag.getName()
	dictionary['SUMMARY'] = tag.getDescription()
	dictionary['ROOT_LEVEL'] = True
	dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue(children)

	refcon = {}
	refcon['NAME'] =  tag.getName()
	dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)
	
	return dictionary

def createExperimentDictionary(experiment):
	dictionary = {}
	dictionary['PERM_ID'] = 'EXPERIMENT.' + experiment.getPermId()
	dictionary['CATEGORY'] = 'Experiment (' + experiment.getExperimentType() + ')'
	dictionary['SUMMARY_HEADER'] = experiment.getExperimentIdentifier()
	dictionary['SUMMARY'] = None
	dictionary['IDENTIFIER'] = dictionary['CATEGORY']
	dictionary['IMAGES'] = IpadServiceUtilities.jsonEncodedValue({})
	dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue([])
	dictionary['ROOT_LEVEL'] = None
	
	refcon = {}
	refcon['ENTITY_TYPE'] =  'EXPERIMENT'
	# there is no way to get an experiment by a permanent id
	refcon['IDENTIFIER'] = experiment.getExperimentIdentifier()
	dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)
	
	return dictionary
	
def createExperimentDetailedDictionary(experiment):
	dictionary = createExperimentDictionary(experiment)
	propertyDefinitions = getPropertyDefinitions(experiment.getExperimentType(), searchService.listPropertiesDefinitionsForExperimentType)

	properties = []
	properties.append(getProperty("#PERM_ID", "Perm ID", experiment.getPermId()))
	properties.extend(getProperties(experiment, propertyDefinitions))
	properties.append(getTimestampProperty())

	dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties) 
	return dictionary

def createSampleDictionary(sample):
	dictionary = {}
	dictionary['PERM_ID'] = 'SAMPLE.' + sample.getPermId()
	dictionary['CATEGORY'] = 'Sample (' + sample.getSampleType() + ')'	
	dictionary['SUMMARY_HEADER'] = sample.getSampleIdentifier()
	dictionary['SUMMARY'] = None
	dictionary['IDENTIFIER'] = dictionary['CATEGORY']
	dictionary['IMAGES'] = IpadServiceUtilities.jsonEncodedValue({})
	dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue([])
	dictionary['ROOT_LEVEL'] = None
	
	refcon = {}
	refcon['ENTITY_TYPE'] =  'SAMPLE'
	refcon['PERM_ID'] = sample.getPermId()
	dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)
	
	return dictionary
	
def createSampleDetailedDictionary(sample):
	dictionary = createSampleDictionary(sample)
	propertyDefinitions = getPropertyDefinitions(sample.getSampleType(), searchService.listPropertiesDefinitionsForSampleType)
	
	properties = []
	properties.append(getProperty("#PERM_ID", "Perm ID", sample.getPermId()))
	if sample.getExperiment():
		properties.append(getProperty("#EXPERIMENT", "Experiment", sample.getExperiment().getExperimentIdentifier()))
	properties.extend(getProperties(sample, propertyDefinitions))
	properties.append(getTimestampProperty())

	dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties) 
	return dictionary

def createDataSetDictionary(dataSet):
	dictionary = {}
	dictionary['PERM_ID'] = 'DATA_SET.' + dataSet.getDataSetCode()
	dictionary['CATEGORY'] = 'Data Set (' + dataSet.getDataSetType() + ')'	
	dictionary['SUMMARY_HEADER'] = dataSet.getDataSetCode()
	dictionary['SUMMARY'] = None
	dictionary['IDENTIFIER'] = dictionary['CATEGORY']
	dictionary['IMAGES'] = IpadServiceUtilities.jsonEncodedValue({})
	dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue([])
	dictionary['ROOT_LEVEL'] = None
	
	refcon = {}
	refcon['ENTITY_TYPE'] =  'DATA_SET'
	refcon['CODE'] = dataSet.getDataSetCode()
	dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)
	
	return dictionary
	
def createDataSetDetailedDictionary(dataSet):
	dictionary = createDataSetDictionary(dataSet)
	propertyDefinitions = getPropertyDefinitions(dataSet.getDataSetType(), searchService.listPropertiesDefinitionsForDataSetType)

	properties = []
	if dataSet.getExperiment():
		properties.append(getProperty("#EXPERIMENT", "Experiment", dataSet.getExperiment().getExperimentIdentifier()))
	if dataSet.getSample():
		properties.append(getProperty("#SAMPLE", "Sample", dataSet.getSample().getSampleIdentifier()))
	properties.append(getProperty("#FiLE_TYPE", "File Type", dataSet.getFileFormatType()))
	properties.extend(getProperties(dataSet, propertyDefinitions))
	properties.append(getTimestampProperty())

	dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties) 
	return dictionary

def createMaterialDictionary(material):
	dictionary = {}
	dictionary['PERM_ID'] = 'MATERIAL.' + material.getMaterialIdentifier()
	dictionary['CATEGORY'] = 'Material (' + material.getMaterialType() + ')'
	dictionary['SUMMARY_HEADER'] = material.getMaterialIdentifier()
	dictionary['SUMMARY'] = None
	dictionary['IDENTIFIER'] = dictionary['CATEGORY']
	dictionary['IMAGES'] = IpadServiceUtilities.jsonEncodedValue({})
	dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue([])
	dictionary['ROOT_LEVEL'] = None
	
	refcon = {}
	refcon['ENTITY_TYPE'] =  'MATERIAL'
	refcon['CODE'] = material.getCode()
	refcon['TYPE_CODE'] = material.getMaterialType();
	dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)
	
	return dictionary

def createMaterialDetailedDictionary(material):
	dictionary = createMaterialDictionary(material)
	propertyDefinitions = getPropertyDefinitions(material.getMaterialType(), searchService.listPropertiesDefinitionsForMaterialType)
	
	properties = []
	properties.extend(getProperties(material, propertyDefinitions))
	properties.append(getTimestampProperty())

	dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties) 
	return dictionary


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

####################
# Entity Properties
####################

def getPropertyDefinitions(typeCode, searchFunction):
	definitions = list(searchFunction(typeCode))
	definitions.sort(lambda x, y: cmp(x.getPositionInForms(), y.getPositionInForms()))
	return definitions

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
			tagName = tagsPermIdsAndRefcons[0]['REFCON']['NAME']
			tagAssignments = self.searchService.getMetaprojectAssignments(tagName)
			tag = tagAssignments.getMetaproject()
			tagChildren = []

			experiments = tagAssignments.getExperiments();
			samples = tagAssignments.getSamples();
			dataSets = tagAssignments.getDataSets();
			materials = tagAssignments.getMaterials();
			
			for experiment in experiments:
				tagChildren.append('EXPERIMENT.' + experiment.getPermId())
			for sample in samples:
				tagChildren.append('SAMPLE.' + sample.getPermId())
			for dataSet in dataSets:
				tagChildren.append('DATA_SET.' + dataSet.getDataSetCode())
			for material in materials:
				tagChildren.append('MATERIAL.' + material.getMaterialIdentifier())
			
			self.addRows([createTagDictionary(tag, tagChildren)])
			self.addRows([createExperimentDictionary(experiment) for experiment in experiments])
			self.addRows([createSampleDictionary(sample) for sample in samples])
			self.addRows([createDataSetDictionary(dataSet) for dataSet in dataSets])
			self.addRows([createMaterialDictionary(material) for material in materials])

class TagDrillRequestHandler(DrillRequestHandler):
	"""Handler for the DRILL request."""

class TagDetailRequestHandler(DetailRequestHandler):
	"""Handler for the DETAIL request."""

	def addDataRows(self):
		entitiesPermIdsAndRefcons = getEntitiesParameter(self)
		
		if entitiesPermIdsAndRefcons:
			entityRefcon = entitiesPermIdsAndRefcons[0]['REFCON'] 
			entityType = entityRefcon['ENTITY_TYPE'];
			
			if 'EXPERIMENT' == entityType:
				experiment = getExperiment(entityRefcon['IDENTIFIER'])
				self.addRows([createExperimentDetailedDictionary(experiment)])
			if 'SAMPLE' == entityType:
				sample = getSample(entityRefcon['PERM_ID'])
				self.addRows([createSampleDetailedDictionary(sample)])
			if 'DATA_SET' == entityType:
				dataSet = getDataSet(entityRefcon['CODE'])
				self.addRows([createDataSetDetailedDictionary(dataSet)])
			if 'MATERIAL' == entityType:
				material = getMaterial(entityRefcon['CODE'], entityRefcon['TYPE_CODE'])
				self.addRows([createMaterialDetailedDictionary(material)])

class TagSearchRequestHandler(SearchRequestHandler):
	"""Handler for the SEARCH request"""

	def addDataRows(self):
		criteria = self.trySearchCriteria()
		if criteria:
			# there is no way to search for experiments and materials
			
			samples = self.searchService.searchForSamples(criteria)
			self.addRows([createSampleDictionary(sample) for sample in samples])
			
			dataSets = self.searchService.searchForDataSets(criteria)
			self.addRows([createDataSetDictionary(dataSet) for dataSet in dataSets])

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
