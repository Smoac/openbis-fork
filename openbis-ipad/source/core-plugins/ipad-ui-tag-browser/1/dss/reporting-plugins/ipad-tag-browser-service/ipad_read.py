from ch.systemsx.cisd.openbis.ipad.v2.server import AbstractRequestHandler, ClientPreferencesRequestHandler, RootRequestHandler
from ch.systemsx.cisd.openbis.ipad.v2.server import DrillRequestHandler, NavigationRequestHandler, DetailRequestHandler, SearchRequestHandler
from ch.systemsx.cisd.openbis.ipad.v2.server import EmptyDataRequestHandler, IpadServiceUtilities
from ch.systemsx.cisd.openbis.ipad.v2.server import IRequestHandlerFactory, RequestHandlerDispatcher
from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import MaterialIdentifier
from com.fasterxml.jackson.databind import ObjectMapper
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria
from java.util import Date

from datetime import datetime

###############
# Dictionaries
###############

def createTagDictionary(name, children):
	dictionary = {}
	dictionary['PERM_ID'] = 'TAG#' + name.upper()
	dictionary['CATEGORY'] = 'Navigation'
	dictionary['SUMMARY_HEADER'] = name
	dictionary['SUMMARY'] = None
	dictionary['ROOT_LEVEL'] = True
	dictionary['CHILDREN'] = IpadServiceUtilities.jsonEncodedValue(children)

	refcon = {}
	refcon['NAME'] =  name.upper()
	dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)
	
	return dictionary

def createMaterialDictionary(material):
	dictionary = {}
	dictionary['PERM_ID'] = 'MATERIAL#' + material.getMaterialIdentifier()
	dictionary['CATEGORY'] = 'Material ' + material.getMaterialType()
	dictionary['SUMMARY_HEADER'] = material.getCode()
	dictionary['SUMMARY'] = material.getPropertyValue("DESC")
	dictionary['IDENTIFIER'] = material.getMaterialIdentifier()
	dictionary['IMAGES'] = []
	dictionary['CHILDREN'] = []
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
	properties = getProperties(material, propertyDefinitions)
	dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties) 
	return dictionary

def createSampleDictionary(sample):
	dictionary = {}
	dictionary['PERM_ID'] = 'SAMPLE#' + sample.getPermId()
	dictionary['CATEGORY'] = 'Sample ' + sample.getSampleType()	
	dictionary['SUMMARY_HEADER'] = sample.getCode()
	dictionary['SUMMARY'] = sample.getPropertyValue("DESC")
	dictionary['IDENTIFIER'] = sample.getSampleIdentifier()
	dictionary['IMAGES'] = []
	dictionary['CHILDREN'] = []
	dictionary['ROOT_LEVEL'] = None
	
	refcon = {}
	refcon['ENTITY_TYPE'] =  'SAMPLE'
	refcon['PERM_ID'] = sample.getPermId()
	dictionary['REFCON'] = IpadServiceUtilities.jsonEncodedValue(refcon)
	
	return dictionary
	
def createSampleDetailedDictionary(sample):
	dictionary = createSampleDictionary(sample)
	propertyDefinitions = getPropertyDefinitions(sample.getSampleType(), searchService.listPropertiesDefinitionsForSampleType)
	properties = getProperties(sample, propertyDefinitions)
	dictionary['PROPERTIES'] = IpadServiceUtilities.jsonEncodedValue(properties) 
	return dictionary
	
#########
# Entity
#########

def getExperiment(permId):
	pass

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
	pass

def getMaterial(code, typeCode):
	pass

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
		properties.append({
			'key' : propertyDefinition.getPropertyTypeCode(), 
			'label' : propertyDefinition.getPropertyTypeLabel(), 
			'value' : entity.getPropertyValue(propertyDefinition.getPropertyTypeCode()) 
		})
	return properties
	
###################
# Request Handlers
###################

class TagNavigationRequestHandler(NavigationRequestHandler):
	"""Handler for the NAVIGATION request."""
	
	def addDataRows(self):
		tags = self.searchService.listMetaprojects()
		self.addRows([createTagDictionary(tag.getName(), []) for tag in tags])

class TagRootRequestHandler(RootRequestHandler):
	"""Handler for the ROOT request."""
	
	def addDataRows(self):
		tagsPermIdsAndRefcons = self.getEntitiesParameter()
		
		if tagsPermIdsAndRefcons:
			tagName = tagsPermIdsAndRefcons[0]['REFCON']['NAME']
			samples = self.searchService.getMetaprojectAssignments(tagName).getSamples();
			self.addRows([createTagDictionary(tagName, [sample.getPermId() for sample in samples])])
			self.addRows([createSampleDictionary(sample) for sample in samples])

class TagDrillRequestHandler(DrillRequestHandler):
	"""Handler for the DRILL request."""

class TagDetailRequestHandler(DetailRequestHandler):
	"""Handler for the DETAIL request."""

	def addDataRows(self):
		entitiesPermIdsAndRefcons = self.getEntitiesParameter()
		
		if entitiesPermIdsAndRefcons:
			entityRefcon = entitiesPermIdsAndRefcons[0]['REFCON'] 
			entityType = entityRefcon['ENTITY_TYPE'];
			
			if 'SAMPLE' == entityType:
				sample = getSample(entityRefcon['PERM_ID'])
				self.addRows([createSampleDetailedDictionary(sample)])

class TagSearchRequestHandler(SearchRequestHandler):
	"""Handler for the SEARCH request"""

	def addDataRows(self):
		criteria = self.trySearchCriteria()
		if criteria:
			samples = self.searchService.searchForSamples(criteria)
			self.addRows([createSampleDictionary(sample) for sample in samples])

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
