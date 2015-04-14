# some_file.py
from datetime import datetime
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType
import xml.etree.ElementTree as ET

##
## Definitions
##

sampleTypesToVerify = ["YEAST","POMBE"];
logLevelsToPrint = ["ERROR", "REPORT", "MANUAL-FIX"];

##
## Logging
##

numberOfManualFixes = 0;
numberOfAutoFixes = 0;
def log(level, message):
    if level == "MANUAL-FIX":
        global numberOfManualFixes
        numberOfManualFixes = numberOfManualFixes + 1
    if level == "AUTO-FIX":
        global numberOfAutoFixes
        numberOfAutoFixes = numberOfAutoFixes + 1
    if any(level in s for s in logLevelsToPrint):
        print "[" + level + "] " + message;

##
## Cache
##
currentCache = None

def getSampleFromCache(identifier):
    sampleToReturn = None
    for sample in currentCache:
        if sample.getSampleIdentifier() == identifier:
            return sample
    return None

##
## Search
##

def getSamplesByType(tr, sampleType):
    criteria = SearchCriteria()
    criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
    criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.TYPE, sampleType))
    samples = tr.getSearchService().searchForSamples(criteria)
    return samples

def getSampleByPermId(tr, permId):
    criteria = SearchCriteria()
    criteria.setOperator(criteria.SearchOperator.MATCH_ANY_CLAUSES)
    criteria.addMatchClause(criteria.MatchClause.createAttributeMatch(criteria.MatchClauseAttribute.PERM_ID, permId))
    samples = tr.getSearchService().searchForSamples(criteria)
    if len(samples) is 1:
        return samples[0]
    else:
        return None

##
## Main Methods
##
def process(tr):
    log("REPORT", "START VERIFICATION REPORT!");
    
    for sampleType in sampleTypesToVerify:
        samples = getSamplesByType(tr, sampleType)
        global currentCache
        currentCache = samples
        print sampleType + ": "+ str(len(samples))
        for sample in samples:
            verify(tr, sample)
    
    global numberOfManualFixes
    global numberOfAutoFixes
    log("REPORT", "FOUND " + str(numberOfAutoFixes) + " AUTOMATIC FIXES!");
    log("REPORT", "REQUIRED " + str(numberOfManualFixes) + " MANUAL FIXES!");
    log("REPORT", "FINISH VERIFICATION REPORT!");

def verify(tr, sample):
    annotationsRoot = getAnnotationsRootNodeFromSample(sample)
    requiredAnnotationsFound = getRequiredAnnotations(sample)
    #1. Delete annotations and parent to be added
    
    #1.Annotations hierarchy
    if annotationsRoot is not None:
        for annotation in annotationsRoot:
            annotatedSampleIdentifier = annotation.attrib["identifier"] #Identifier from annotated sample
            requiredAnnotationsFound[annotatedSampleIdentifier] = True
            try:
                if isChild(sample, annotatedSampleIdentifier):
                    #This is an annotation from a parent, this is by default correct and don't needs further inspection.
                    log("INFO", "GOOT ANNOTATION AT SAMPLE: " + sample.getSampleIdentifier() + " ANNOTATION: " + annotatedSampleIdentifier);
                else:
                    foundAnnotationAndAncestor = getAnnotationAndAncestor(annotatedSampleIdentifier, sample.getParentSampleIdentifiers())
                    foundAnnotation = foundAnnotationAndAncestor[0]
                    foundAncestor = foundAnnotationAndAncestor[1]
                    if foundAnnotation is not None and foundAncestor is not None:
                        log("INFO", "BAD CHILD FOUND - " + sample.getSampleIdentifier() + " " + annotatedSampleIdentifier);
                        if areAnnotationsEqual(annotation, foundAnnotation):
                            log("AUTO-FIX", "CASE 1 - GOOD REPEATED ANNOTATION THAT CAN BE DELETED - " + sample.getSampleIdentifier() + " " + annotatedSampleIdentifier);
                        else:
                            log("MANUAL-FIX", "CASE 3 - THE ANNOTATION: " + annotatedSampleIdentifier + " IS DIFFERENT AT SAMPLE: " + sample.getSampleIdentifier() + " AND ORIGINAL ANCESTOR:" + foundAncestor.getSampleIdentifier());
                    elif foundAncestor is None:
                        log("MANUAL-FIX", "CASE 1 - THE ANNOTATED SAMPLE IS NOT AN ANCESTOR - FOR SAMPLE: " + sample.getSampleIdentifier() + " ANNOTATION WITH MISSING ANCESTOR:" + annotatedSampleIdentifier);
                    elif foundAnnotation is None:
                        log("MANUAL-FIX", "CASE 2 - THE ANNOTATED SAMPLE IS NOT ANNOTATED WHERE IT SHOULD - FOR SAMPLE: " + sample.getSampleIdentifier() + " ANNOTATION: " + annotatedSampleIdentifier +" NOT AT " + foundAncestor.getSampleIdentifier());
            except Exception:
                log("ERROR", "PROCESSING ANNOTATIONS XML CHILD " + sample.getSampleIdentifier());
    #2.Missing Annotations
    for parentIdentifier in requiredAnnotationsFound:
        if not requiredAnnotationsFound[parentIdentifier]:
            log("MANUAL-FIX", "CASE 4 - MISSING ANNOTATIONS ON SAMPLE: " + sample.getSampleIdentifier() + " FOR PARENT:" + parentIdentifier);

def getRequiredAnnotations(sample):
    requiredAnnotationsFound = {}
    for parentIdentifier in sample.getParentSampleIdentifiers():
        if "/FRP" in parentIdentifier: #Only require Plasmids
            requiredAnnotationsFound[parentIdentifier] = False;
    return requiredAnnotationsFound;

def areAnnotationsEqual(annotationA, annotationB):
    for key in annotationA.attrib:
        value = annotationA.attrib[key]
        if key != "CONTAINED":
            if value != annotationB.attrib[key]:
                log("INFO", "EQUALITY FAILED FOR " + key + ": - " + value + " " + annotationB.attrib[key]);
                return False
    return True

def getAnnotationAndAncestor(annotatedSampleIdentifier, sampleParentsIdentifiers):
    ancestorsIdentifiers = list(sampleParentsIdentifiers)
    while( len(ancestorsIdentifiers) > 0):
        ancestorIdentifier = ancestorsIdentifiers.pop(0)
        ancestor = getSampleFromCache(ancestorIdentifier)
        if ancestor is not None:
                if isChild(ancestor, annotatedSampleIdentifier): #We only accept annotations from the original sample to avoid test repetitions
                    ancestorAnnotationsRoot = getAnnotationsRootNodeFromSample(ancestor)
                    if ancestorAnnotationsRoot is not None:
                        for annotation in ancestorAnnotationsRoot:
                            if annotation.attrib["identifier"] == annotatedSampleIdentifier:
                                return [annotation, ancestor]
                    return [None, ancestor]
                else:
                    ancestorsIdentifiers.extend(ancestor.getParentSampleIdentifiers())
    return [None, None] #Should never happen

def isChild(sample, identifier):
    if any(identifier in s for s in sample.getParentSampleIdentifiers()):
        return True
    else:
        return False
    
def getAnnotationsRootNodeFromSample(sample):
    annotations = sample.getPropertyValue("ANNOTATIONS_STATE")
    if '<root>' in annotations:
        try:
            return ET.fromstring(annotations)
        except Exception:
            log("ERROR", "READING ANNOTATIONS XML FOR " + sample.getSampleIdentifier());
    return None