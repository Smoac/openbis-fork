# some_file.py
from datetime import datetime
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType
import xml.etree.ElementTree as ET

##
## Generic Process Method
##
sampleTypes = [
            "GENERAL_PROTOCOL",
            "MEDIA",
            "PCR",
            "POMBE",
            "READOUT",
            "RESULT",
            "SOLUTIONS_BUFFERS",
            "WESTERN_BLOTTING",
            "YEAST"];
    
properties = [
            "CHEMICALS",
            "SOLUTIONS_BUFFERS",
            "ENZYMES",
            "MEDIA",
            "GENERAL_PROTOCOL",
            "PLASMIDS",
            "POMBE-PARENTS",
            "ANNOTATIONS",
            "ANTIBODIES",
            "YEAST_PARENTS"];

def process(tr):
    print "START!"
    for sampleType in sampleTypes:
        samples = getSamplesByType(tr, sampleType)
        print sampleType + ": "+ str(len(samples))
        for sample in samples:
            translate(tr, sample, properties)
    print "FINISH!"

def getSamplesByType(tr, sampleType):
    criteria = SearchCriteria()
    criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
    criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.TYPE, sampleType))
    samples = tr.getSearchService().searchForSamples(criteria)
    return samples

def getSampleByPermId(permId):
    criteria = SearchCriteria()
    criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
    criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.PERM_ID, permId))
    samples = tr.getSearchService().searchForSamples(criteria)
    if len(samples) is 1:
        return samples[0]
    else:
        return None

def translate(tr, sample, properties):
    sampleType = sample.getSampleType()
    # Create new annotations from scratch
    newAnnotations = sample.getPropertyValue("ANNOTATIONS_STATE")
    newAnnotationsRoot = None
#     if (newAnnotations is not None) and (newAnnotations is not ""):
#         newAnnotationsRoot = ET.fromstring(newAnnotations)
#     if (newAnnotations is None) or (newAnnotations is ""):
    newAnnotationsRoot = ET.Element("root")
    
    # Read old annotations
    for property in properties:
        propertyValue = unicode(sample.getPropertyValue(property), "utf-8")
        if propertyValue != None:
            oldAnnotationsRoot = ET.fromstring(propertyValue)
            if property is "CHEMICALS" and sampleType is "GENERAL_PROTOCOL":
                for child in oldAnnotationsRoot:
                    permId = child.attrib["permId"]
                    concentration = getValueOrNull(child.attrib, "concentration")
                    chemicalName = getValueOrNull(child.attrib, "name")
                    linkedSample = getSampleByPermId(permId)
                    newAnnotationsNode = SubElement(newAnnotationsRoot, "Sample")
                    
                    newAnnotationsNode.attrib = {
                                                    "permId" : permId,
                                                    "identifier" : linkedSample.getSampleIdentifier(),
                                                    "concentration" : concentration,
                                                    "chemicalName" : chemicalName
                                                }
#                     for key, value in newAnnotationsNode.attrib:
#                         if value is None:
#                             my_dict.pop(key, None)
    save(tr, sample, "ANNOTATIONS_STATE", ET.tostring(newAnnotationsRoot, encoding='utf-8'))

def save(tr, sample, property, propertyValue):
    mutableSample = tr.makeSampleMutable(sample)
    mutableSample.setPropertyValue(property, propertyValue)
    
def getValueOrNull(map, key):
    if key in map:
        return map[key]
    else:
        return None