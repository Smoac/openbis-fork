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
    isFirst = True
    parentIterables = [{
                        "child" : entity,
                        "parents" : entity.parents()
                        }]

    while len(parentIterables) > 0:
        parentGroup = parentIterables.pop(0)
        parents = parentGroup["parents"]
        child = parentGroup["child"]
        for parent in parents:
            parentTypeCode = getSampleTypeCode(parent)
            if parentTypeCode in typesForGenotype:
                parentCode = parent.code()
                 #Check if is the first to add the separator or not
                if isFirst:
                    isFirst = False
                else:
                    genotypeResult = genotypeResult + "\n"
                #Add the code
                genotypeResult = genotypeResult + parent.code() + " " + str(getAnnotationsForParent(parent, child)) #TO-DO Change entity by the child of the parent for each iteration
            else:
                parentIterables.append({
                                        "child" : parent,
                                        "parents" : parent.parents()
                                        })
    return genotypeResult

def getAnnotationsForParent(parent, child):
    permId = parent.entityPE().getPermId()
    annotations = child.propertyValue("ANNOTATIONS_STATE")
    if (annotations is not None) and ('<root>' in annotations):
        relationshipValue = getAnnotationFromPermId(annotations, permId, "PLASMID_RELATIONSHIP")
        if relationshipValue is None:
            relationshipValue = "None"
        annotationValue = getAnnotationFromPermId(annotations, permId, "PLASMID_ANNOTATION");
        if annotationValue is None:
            annotationValue = ""
        annotation = "\"" + str(annotationValue) + "\""
        return str(relationshipValue) + " " + str(annotation)
    return "No Annotations Found"
    
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