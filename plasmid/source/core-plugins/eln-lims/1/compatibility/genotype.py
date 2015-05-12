import xml.dom.minidom
from ch.systemsx.cisd.common.exceptions import UserFailureException

configuration = {}
configuration["YEAST"] = ["PLASMID"]
configuration["BACTERIA"] = ["PLASMID"]
configuration["CELL_LINE"] = ["PLASMID"]
configuration["FLY"] = ["PLASMID"]

def getSampleTypeCode(sampleAdaptor):
    return sampleAdaptor.samplePE().getSampleType().getCode()

def calculate():
    #Configuration
    sampleTypeCode = getSampleTypeCode(entity)
    typesForGenotype = configuration[sampleTypeCode]
    
    genotypeResult = ""
    sampleCodesInGenotype = {} #To avoid to add repetitions
    isFirst = True
    parentIterables = [entity.parents()]

    while len(parentIterables) > 0:
        parentIterable = parentIterables.pop(0)
        for parent in parentIterable:
            parentTypeCode = getSampleTypeCode(parent)
            if parentTypeCode in typesForGenotype:
                parentCode = parent.code()
                if parentCode not in sampleCodesInGenotype: #To avoid to add repetitions
                    #Check if is the first to add the separator or not
                    if isFirst:
                        isFirst = False
                    else:
                        genotypeResult = genotypeResult + "\n "
                    #Add the code
                    genotypeResult = genotypeResult + parent.code() + " " + str(getAnnotationsForParent(parent, entity))
                    sampleCodesInGenotype[parent.code()] = True
            else:
                parentIterables.append(parent.parents())
    return genotypeResult

def getAnnotationsForParent(parent, child):
    permId = parent.entityPE().getPermId()
    annotations = child.propertyValue("ANNOTATIONS_STATE")
    if (annotations is not None) and ('<root>' in annotations):
        relationship = getAnnotationFromPermId(annotations, permId, "PLASMID_RELATIONSHIP")
        annotation = getAnnotationFromPermId(annotations, permId, "PLASMID_ANNOTATION")
        return str(relationship) + " " + str(annotation)
    return None
    
def getAnnotationFromPermId(annotations, permId, key):
    dom = xml.dom.minidom.parseString(annotations)
    for child in dom.childNodes[0].childNodes:
        if child.localName != None:
            permIdFound = child.attributes["permId"].value
            if permIdFound == permId:
                keys = child.attributes.keys();
                for keyFound in keys:
                    if keyFound == key:
                        return child.attributes[key].value
    return None

def getValueOrNull(map, key):
    if key in map:
        value = map[key]
        if not value: #Check for null strings
            return None
        else:
            return value
    else:
        return None