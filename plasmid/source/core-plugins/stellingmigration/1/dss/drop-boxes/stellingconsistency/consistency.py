# some_file.py
from datetime import datetime
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType
import xml.etree.ElementTree as ET

##
## Definitions
##
UPDATES_ENABLED = False

sampleTypesToVerify = ["YEAST","POMBE"]
logLevelsToPrint = ["ERROR", "REPORT", "MANUAL-FIX", "UPDATED"] #INFO not included, use it for debug only

##
## Logging
##

numberOfManualFixes = 0
numberOfAutoFixesDeletes = 0
numberOfAutoFixesLost = 0
numberOfCanBeUpdated = 0

def logManualFix(message, creator, sampleIdentifier, affectedSampleToBeAnnotated, creatorOfAncestor, ancestor):
    log("MANUAL-FIX", message + "\t" + creator + "\t" + sampleIdentifier + "\t" + affectedSampleToBeAnnotated + "\t" + creatorOfAncestor + "\t" + ancestor)
    
def log(level, message):
    if level == "MANUAL-FIX":
        global numberOfManualFixes
        numberOfManualFixes = numberOfManualFixes + 1
    if level == "AUTO-FIX":
        global numberOfAutoFixesDeletes
        numberOfAutoFixesDeletes = numberOfAutoFixesDeletes + 1
    if level == "AUTO-FIX-2":
        global numberOfAutoFixesLost
        numberOfAutoFixesLost = numberOfAutoFixesLost + 1
    if level in logLevelsToPrint:
        print "[" + level + "] " + message

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
    log("REPORT", "START VERIFICATION REPORT!")
    logManualFix("MANUAL-FIX", "AFFECTED SAMPLE CREATOR", "AFFECTED SAMPLE", "ANNOTATION OF AFFECTED SAMPLE", "AFFECTED ANCESTOR CREATOR", "AFFECTED ANCESTOR")
    for sampleType in sampleTypesToVerify:
        samples = getSamplesByType(tr, sampleType)
        global currentCache
        currentCache = samples
        print sampleType + ": "+ str(len(samples))
        for sample in samples:
            verify(tr, sample)
    
    global numberOfManualFixes
    global numberOfAutoFixesDeletes
    global numberOfAutoFixesLost
    global numberOfCanBeUpdated
    log("REPORT", "FOUND " + str(numberOfAutoFixesDeletes) + " AUTOMATIC DELETE FIXES!")
    log("REPORT", "FOUND " + str(numberOfAutoFixesLost) + " AUTOMATIC LOST FIXES!")
    log("REPORT", "UPDATABLE " + str(numberOfCanBeUpdated) + " SAMPLES!")
    log("REPORT", "REQUIRED " + str(numberOfManualFixes-1) + " MANUAL FIXES!") #-1 For the Header
    log("REPORT", "FINISH VERIFICATION REPORT!")

def verify(tr, sample):
    newAnnotations = [];
    newAnnotationsReady = True;
    annotationsRoot = getAnnotationsRootNodeFromSample(sample)
    #1.Annotations hierarchy
    requiredAnnotationsFound = getRequiredAnnotations(sample) #To detect case 4
    requiredAnnotationsFromParents = getRequiredAnnotationsFromParents(sample) #To detect case 5
    if annotationsRoot is not None:
        for annotation in annotationsRoot:
            annotatedSampleIdentifier = annotation.attrib["identifier"] #Identifier from annotated sample
            requiredAnnotationsFound[annotatedSampleIdentifier] = True
            requiredAnnotationsFromParents[annotatedSampleIdentifier] = True
            try:
                if isChild(sample, annotatedSampleIdentifier):
                    #This is an annotation from a parent, this is by default correct and don't needs further inspection.
                    newAnnotations.append(getAnnotationMap(annotation));
                    log("INFO", "GOOT ANNOTATION AT SAMPLE: " + sample.getSampleIdentifier() + " ANNOTATION: " + annotatedSampleIdentifier)
                else:
                    foundAnnotationAndAncestor = getAnnotationAndAncestor(annotatedSampleIdentifier, sample.getParentSampleIdentifiers())
                    foundAnnotation = foundAnnotationAndAncestor[0]
                    foundAncestor = foundAnnotationAndAncestor[1]
                    if foundAnnotation is not None and foundAncestor is not None:
                        log("INFO", "BAD ANNOTATION FOUND - ON:" + sample.getSampleIdentifier() + " ANNOTATION:" + annotatedSampleIdentifier)
                        if areAnnotationsEqual(annotation, foundAnnotation) and areAnnotationsEqual(foundAnnotation, annotation):
                            log("AUTO-FIX", "CASE 1 - GOOD REPEATED ANNOTATION THAT CAN BE DELETED - AT SAMPLE: " + sample.getSampleIdentifier() + " FOR ANNOTATION: " + annotatedSampleIdentifier + " FOUND ORIGINAL AT: " + foundAncestor.getSampleIdentifier())
                        else:
                            logManualFix("Case 3 - The annotation is different on the sample and his ancestor.", sample.getSample().getRegistrator().getUserId(), sample.getSampleIdentifier(), annotatedSampleIdentifier, foundAncestor.getSample().getRegistrator().getUserId(), foundAncestor.getSampleIdentifier())
                            newAnnotationsReady = False
                    elif foundAncestor is None:
                        logManualFix("Case 1 - The annotated sample is not an ancestor, is missing on the hierarchy for some reason.", sample.getSample().getRegistrator().getUserId(), sample.getSampleIdentifier(), annotatedSampleIdentifier, "?", "?")
                        newAnnotationsReady = False
                    elif foundAnnotation is None:
                        logManualFix("Case 2 - The annotated sample is not annotated on the appropriate ancestor.", sample.getSample().getRegistrator().getUserId(), sample.getSampleIdentifier(), annotatedSampleIdentifier, foundAncestor.getSample().getRegistrator().getUserId(), foundAncestor.getSampleIdentifier())
                        newAnnotationsReady = False
            except Exception, err:
                log("ERROR", "PROCESSING ANNOTATIONS XML CHILD " + sample.getSampleIdentifier() + " ERR: " + str(err))
    #2.Missing Parents Annotations
    for parentIdentifier in requiredAnnotationsFound:
        if not requiredAnnotationsFound[parentIdentifier]:
            logManualFix("Case 4 - Missing annotation on sample for parent.", sample.getSample().getRegistrator().getUserId(), sample.getSampleIdentifier(), parentIdentifier, "-", "-")
            newAnnotationsReady = False
    #3.Missing Annotations LOST
    for parentAnnotationIdentifier in requiredAnnotationsFromParents:
        if not requiredAnnotationsFromParents[parentAnnotationIdentifier]:
            lostPermId = getRequiredAnnotationsFromParentsPermId(sample, parentAnnotationIdentifier)
            if lostPermId is None:
                raise Exception("Lost PermId Not Found");
            newAnnotations.append({ "permId" : lostPermId, "identifier" : parentAnnotationIdentifier, "PLASMID_RELATIONSHIP" : "LOT" });
            log("AUTO-FIX-2", "CASE 2 - MISSING LOST ANNOTATIONS ON SAMPLE: " + sample.getSampleIdentifier() + " FOR LOST ANNOTATION: " + parentAnnotationIdentifier + " PRESENT INTO ONE OF THE PARENTS")
    #4 Check parents from contained
    expectedParents = getContainedFromAnnotations(sample)
    lostParents = areParentsPresent(sample, expectedParents)
    if len(lostParents) > 0:
        logManualFix("Case 5 - Missing Parents present in Contained: ", sample.getSample().getRegistrator().getUserId(), sample.getSampleIdentifier(), str(lostParents), "?", "?")
        newAnnotationsReady = False
    #5 Check repeated parents in annotations
    if areAnnotationDuplicated(sample):
        logManualFix("Case 6 - Same annotation coming from different parents: ", sample.getSample().getRegistrator().getUserId(), sample.getSampleIdentifier(), "?", "?", "?")
        newAnnotationsReady = False
    global UPDATES_ENABLED;
    if newAnnotationsReady and UPDATES_ENABLED:
        global numberOfCanBeUpdated;
        numberOfCanBeUpdated = numberOfCanBeUpdated + 1;
        log("UPDATED", str(newAnnotations))
        sample =  tr.makeSampleMutable(sample);
        #1. Create new annotations XML
        newAnnotationsRoot = ET.Element("root");
        for newAnnotation in newAnnotations:
            newAnnotationsNode = ET.SubElement(newAnnotationsRoot, "Sample")
            for newAttribute in newAnnotation:
                newAnnotationsNode.attrib[newAttribute] = newAnnotation[newAttribute];
            if getValueOrNull(newAnnotation, "PLASMID_RELATIONSHIP") == "LOT": #2. Add LOST parents
                sample.getParentSampleIdentifiers().add(newAnnotation["identifier"]);
        sample.setPropertyValue("ANNOTATIONS_STATE", ET.tostring(newAnnotationsRoot, encoding='utf-8'));

def areAnnotationDuplicated(sample):
    annotated = {}; #They should be parents of the sample and not been missing
    annotationsRoot = getAnnotationsRootNodeFromSample(sample);
    if annotationsRoot is not None:
        for annotation in annotationsRoot:
            permId = getValueOrNull(annotation.attrib, "permId");
            if permId in annotated:
                return True
            else:
                annotated[permId] = True
    return False
            
def getContainedFromAnnotations(sample):
    contained = []; #They should be parents of the sample and not been missing
    annotationsRoot = getAnnotationsRootNodeFromSample(sample);
    if annotationsRoot is not None:
        for annotation in annotationsRoot:
            containedFound = getValueOrNull(annotation.attrib, "CONTAINED");
            if containedFound is not None and ('/' in containedFound):
                contained.append(containedFound);
    return contained;

def areParentsPresent(sample, parents):
    notPresent = [];
    for parent in parents:
        isPresent = (parent in sample.getParentSampleIdentifiers())
        if not isPresent:
            notPresent.append(parent);
    return notPresent

def getRequiredAnnotationsFromParentsPermId(sample, identifier):
    for parentIdentifier in sample.getParentSampleIdentifiers():
        if ("/FRY" in parentIdentifier) or ("/FRS" in parentIdentifier): #Only check Yeast and Pombe Parents
            parent = getSampleFromCache(parentIdentifier)
            parentAnnotationsRoot = getAnnotationsRootNodeFromSample(parent)
            if parentAnnotationsRoot is not None:
                for parentAnnotation in parentAnnotationsRoot:
                    parentAnnotationIdentifier = parentAnnotation.attrib["identifier"]
                    if "/FRP" in parentAnnotationIdentifier: #Only require Plasmids
                        if identifier == parentAnnotationIdentifier:
                            return parentAnnotation.attrib["permId"]
    return None

def getRequiredAnnotationsFromParents(sample):
    requiredAnnotationsFromParents = {}
    for parentIdentifier in sample.getParentSampleIdentifiers():
        if ("/FRY" in parentIdentifier) or ("/FRS" in parentIdentifier): #Only check Yeast and Pombe Parents
            parent = getSampleFromCache(parentIdentifier)
            parentAnnotationsRoot = getAnnotationsRootNodeFromSample(parent)
            if parentAnnotationsRoot is not None:
                for parentAnnotation in parentAnnotationsRoot:
                    parentAnnotationIdentifier = parentAnnotation.attrib["identifier"]
                    if "/FRP" in parentAnnotationIdentifier: #Only require Plasmids
                        requiredAnnotationsFromParents[parentAnnotationIdentifier] = False
    return requiredAnnotationsFromParents

def getRequiredAnnotations(sample):
    requiredAnnotationsFound = {}
    for parentIdentifier in sample.getParentSampleIdentifiers():
        if "/FRP" in parentIdentifier: #Only require Plasmids
            requiredAnnotationsFound[parentIdentifier] = False
    return requiredAnnotationsFound

def getAnnotationMap(annotation):
    map = {};
    for key in annotation.attrib:
        value = getValueOrNull(annotation.attrib, key)
        if value is not None:
            map[key] = value;
    return map

def areAnnotationsEqual(annotationA, annotationB):
    for key in annotationA.attrib:
        value = getValueOrNull(annotationA.attrib, key)
        if key != "CONTAINED":
            if value != getValueOrNull(annotationB.attrib, key):
                log("INFO", "EQUALITY FAILED FOR " + key + ": - " + str(value) + " " + str(getValueOrNull(annotationB.attrib, key)))
                return False
    return True

def getValueOrNull(map, key):
    if key in map:
        value = map[key]
        if not value: #Check for null strings
            return None
        else:
            return value
    else:
        return None
    
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
            log("ERROR", "READING ANNOTATIONS XML FOR " + sample.getSampleIdentifier())
    return None