#! /usr/bin/env python
from java.util import Date
from java.text import SimpleDateFormat
import xml.etree.ElementTree as ET
from xml.etree.ElementTree import Element, SubElement
from com.lyncode.xoai.dataprovider import DataProvider
from com.lyncode.xoai.dataprovider.model import Context, MetadataFormat, Item
from com.lyncode.xoai.dataprovider.repository import Repository, RepositoryConfiguration
from com.lyncode.xoai.dataprovider.parameters import OAIRequest
from com.lyncode.xoai.dataprovider.handlers.results import ListItemIdentifiersResult, ListItemsResults
from com.lyncode.xoai.model.oaipmh import OAIPMH, DeletedRecord, Granularity, Metadata
from com.lyncode.xoai.xml import XmlWriter
from ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.xoai import SimpleItemIdentifier, SimpleItem, SimpleItemRepository, SimpleSetRepository
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, MatchClauseAttribute, MatchClauseTimeAttribute, CompareMode

DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
TIME_ZONE = "0"
 
PUBLICATION_EXPERIMENT_TYPE = "PUBLICATION"

def handle(req, resp):
    context = Context();
    context.withMetadataFormat(MetadataFormat().withPrefix("oai_dc").withTransformer(MetadataFormat.identity()));
    configuration = RepositoryConfiguration();
    configuration.withMaxListSets(100);
    configuration.withMaxListIdentifiers(100);
    configuration.withMaxListRecords(100);
    configuration.withAdminEmail("test@test");
    configuration.withBaseUrl(getServerUrl());
    configuration.withDeleteMethod(DeletedRecord.NO);
    configuration.withEarliestDate(Date(0));
    configuration.withRepositoryName("TEST");
    configuration.withGranularity(Granularity.Day);
    repository = Repository();
    repository.withConfiguration(configuration);
    repository.withItemRepository(ItemRepository());
    repository.withSetRepository(SimpleSetRepository());
    provider = DataProvider(context, repository);
    params = {}
    for param in req.getParameterNames():
        values = []
        for value in req.getParameterValues(param):
            values.append(value)
        params[param] = values
    request = OAIRequest(params);
    response = provider.handle(request);
    writer = XmlWriter(resp.getOutputStream());
    response.write(writer);
    writer.flush();
 
class ItemRepository(SimpleItemRepository):
        
    def doGetItem(self, identifier):
        criteria = SearchCriteria()
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, identifier))
        experiments = searchService.searchForExperiments(criteria)
        experiments = filterNotPublishedExperiments(experiments)
         
        if experiments:
            return createExperimentItem(experiments[0])
        else:
            return None
    def doGetItemIdentifiers(self, filters, offset, length, setSpec, fromDate, untilDate):
        results = self.doGetItems(filters, offset, length, setSpec, fromDate, untilDate)
        return ListItemIdentifiersResult(results.hasMore(), results.getResults(), results.getTotal())
     
    def doGetItems(self, filters, offset, length, setSpec, fromDate, untilDate):
        criteria = SearchCriteria()
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, PUBLICATION_EXPERIMENT_TYPE));
        if fromDate:
            criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE, CompareMode.GREATER_THAN_OR_EQUAL, DATE_FORMAT.format(fromDate), TIME_ZONE))
        if untilDate:
            criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE, CompareMode.LESS_THAN_OR_EQUAL, DATE_FORMAT.format(untilDate), TIME_ZONE))
        experiments = searchService.searchForExperiments(criteria)
        experiments = filterNotPublishedExperiments(experiments)
        if experiments:
            hasMoreResults = (offset + length) < len(experiments)
            results = [createExperimentItem(experiment) for experiment in experiments[offset:(offset + length)]]
            total = len(experiments)
            return ListItemsResults(hasMoreResults, results, total)
        else:
            return ListItemsResults(False, [], 0)

def filterNotPublishedExperiments(experiments):
    publishedSpaces = getPublishedSpaces()
    if experiments:
        published = []
        for experiment in experiments:
            space = experiment.getExperimentIdentifier().split("/")[1].upper()
            if space in publishedSpaces:
                published.append(experiment)
        return published
    else:
        return experiments

def createExperimentMetadata(experiment):
    oai = ET.Element("oai_dc:dc")
    oai.attrib["xmlns:dc"] = "http://purl.org/dc/elements/1.1/"
    oai.attrib["xmlns:oai_dc"] = "http://www.openarchives.org/OAI/2.0/oai_dc/"
    oai.attrib["xmlns:xsi"] = "http://www.w3.org/2001/XMLSchema-instance"
    oai.attrib["xsi:schemaLocation"] = "http://www.openarchives.org/OAI/2.0/oai_dc/\nhttp://www.openarchives.org/OAI/2.0/oai_dc.xsd"

    fields = [
        ["dc:identifier" , "PUBLICATION_ID"],
        ["dc:title" , "PUBLICATION_TITLE"],
        ["dc:creator" , "PUBLICATION_AUTHOR"],
        ["creator_email" , "PUBLICATION_AUTHOR_EMAIL"],
        ["dc:description" , "PUBLICATION_NOTES"],
        ["dc:rights" , "PUBLICATION_LICENSE"]
    ]

    for field in fields:
        fieldNode = ET.SubElement(oai, field[0])
        fieldNode.text = experiment.getPropertyValue(field[1])
        
    meshTerms = experiment.getPropertyValue("PUBLICATION_MESH_TERMS")
    if meshTerms:
        for meshTerm in meshTerms.splitlines():
            if meshTerm and meshTerm.strip():
                subjectNode = ET.SubElement(oai, "dc:subject")
                subjectNode.text = meshTerm.strip()

    identifierNode = ET.SubElement(oai, "dc:source")
    identifierNode.text = getServerUrl() + "/openbis/?viewMode=SIMPLE#entity=EXPERIMENT&permId=" + str(experiment.getPermId())

    return Metadata(ET.tostring(oai))
 
def createExperimentItem(experiment):
    item = SimpleItem()
    item.setIdentifier(experiment.getPermId())
    item.setDatestamp(Date())
    item.setMetadata(createExperimentMetadata(experiment))
    return item

def getPublishedSpaces():
    property = properties.get("published-spaces")
    codes = []
    
    if property and property.strip():
        codes = []
        
        for token in property.split(','):
            if token.strip():
                codes.append(token.strip().upper())

    if codes:
        return codes
    else:
        raise Exception("No publication spaces have been configured. Please check that 'published-spaces' property has been properly set in the service plugin plugin.properties.")    

def getServerUrl():
    property = properties.get("server-url")
    if property and property.strip():
        return property.strip()
    else:
        raise Exception("No server url has been configured. Please check that 'server-url' property has been properly set in the service plugin plugin.properties.")
