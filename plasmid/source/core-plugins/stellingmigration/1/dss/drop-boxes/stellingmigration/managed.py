from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider

##
## Configuration
##

# This needs to be edited for each sample type to match the Profile.js ANNOTATION_PROPERTIES map.
configuration = {}

configuration["GENERAL_PROTOCOL"] = {
                          "CHEMICAL" : {"QUANTITY" : False, "NAME" : False },
                          "SOLUTION_BUFFERS" : {"QUANTITY" : False, "NAME" : False },
                          "MEDIA" : {"QUANTITY" : False, "NAME" : False },
                          "ENZYME" : {"NAME" : False, "CONCENTRATION" : False }
                         };

configuration["MEDIA"] = {
                          "CHEMICAL" : {"CONCENTRATION" : False, "NAME" : False },
                          "SOLUTION_BUFFERS" : {"CONCENTRATION" : False, "NAME" : False },
                          "MEDIA" : {"CONCENTRATION" : False, "NAME" : False }
                         };

configuration["PCR"] = {
                          "CHEMICAL" : {"QUANTITY" : False, "NAME" : False },
                          "SOLUTION_BUFFERS" : {"QUANTITY" : False, "NAME" : False },
                          "ENZYME" : {"NAME" : False, "CONCENTRATION" : False }
                         };

configuration["POMBE"] = {
                          "POMBE" : {}, #Just a placeholder, is actually used by the Plasmids of the Pombe to create the links
                          "PLASMID" : {"PLASMID_ANNOTATION" : False, "PLASMID_RELATIONSHIP" : False, "COMMENTS" : False, "CONTAINED" : False }
                         };

configuration["READOUT"] = {
                          "CHEMICAL" : {"QUANTITY" : False, "NAME" : False },
                          "SOLUTION_BUFFERS" : {"QUANTITY" : False, "NAME" : False }
                         };

configuration["RESULT"] = {
                          "ANNOTATIONS" : {"QUANTITY" : False, "NAME" : False, "DETAIL" : False }
                         };

configuration["SOLUTION_BUFFERS"] = {
                          "CHEMICAL" : {"CONCENTRATION" : False, "NAME" : False },
                          "SOLUTION_BUFFERS" : {"CONCENTRATION" : False, "NAME" : False }
                         };

configuration["WESTERN_BLOTTING"] = {
                          "CHEMICAL" : {"QUANTITY" : False, "NAME" : False },
                          "SOLUTION_BUFFERS" : {"QUANTITY" : False, "NAME" : False },
                          "ANTIBODY" : {"QUANTITY" : False, "NAME" : False }
                         };

configuration["YEAST"] = {
                          "YEAST" : {}, #Just a placeholder, is actually used by the Plasmids of the Yeast to create the links
                          "PLASMID" : {"PLASMID_ANNOTATION" : False, "PLASMID_RELATIONSHIP" : False, "COMMENTS" : False, "CONTAINED" : False }
                         };

configurationCopyParents = {}
configurationCopyParents["YEAST"] = { "YEAST" : "PLASMID" };
configurationCopyParents["POMBE"] = { "POMBE" : "PLASMID" };

#Global Variables
annotableType = "<REPLACE_WITH_ANNOTABLE_TYPE>"
server = CommonServiceProvider.getCommonServer()
contextOrNull = server.tryToAuthenticateAsSystem()
propertyTypes = server.listPropertyTypes(contextOrNull.getSessionToken(), False)

##
## Help Methods
##
def getAllAnnotableSampleTypesForType(fromAnnotableType):
    allTypes = {};
    for sampleTypeWithAnnotations in configuration:
        if (fromAnnotableType == None) or (fromAnnotableType == sampleTypeWithAnnotations):
            for sampleTypeWithAnnotationsForType in configuration[sampleTypeWithAnnotations]:
                    allTypes[sampleTypeWithAnnotationsForType] = True;
    return allTypes;

def getPropertyTypesForSampleTypeFromAnnotableType(sampleTypeCodeToFind, fromAnnotableType):
    for sampleTypeWithAnnotations in configuration:
        if (fromAnnotableType == None) or (fromAnnotableType == sampleTypeWithAnnotations):
            for sampleTypeWithAnnotationsForType in configuration[sampleTypeWithAnnotations]:
                    if sampleTypeWithAnnotationsForType == sampleTypeCodeToFind:
                            return configuration[sampleTypeWithAnnotations][sampleTypeWithAnnotationsForType];
    return None;
    
def getPropertyType(propertyTypeCode):
    for propertyType in propertyTypes:
        if propertyType.code == propertyTypeCode:
            return propertyType
    return None;

def createAnnotationsFor(identifier, annotations, sampleType):
    permId = entityInformationProvider().getSamplePermId(identifier);
    newAnnotation = elementFactory().createSampleLink(permId)
    
    newAnnotation.addAttribute("identifier", identifier)
    newAnnotation.addAttribute("sampleType", sampleType)
    for annotation in annotations:
        newAnnotation.addAttribute(annotation, annotations[annotation])
    return newAnnotation

def getWidgetForAdd(sampleTypeCode, annotableType):
    #print "sampleTypeCode:" + sampleTypeCode + " - " + annotableType #Deleting this comment causes the thing to break
    widgets = []
    widgetIdentifier = inputWidgetFactory().createTextInputField("identifier")\
                            .setMandatory(True)\
                            .setValue("")\
                            .setDescription("")
    widgets.append(widgetIdentifier)
    for propertyTypeCode in getPropertyTypesForSampleTypeFromAnnotableType(sampleTypeCode, annotableType):
        propertyType = getPropertyType(propertyTypeCode)
        widget = inputWidgetFactory().createTextInputField(propertyType.label)\
                        .setMandatory(getPropertyTypesForSampleTypeFromAnnotableType(sampleTypeCode, annotableType)[propertyTypeCode])\
                        .setValue("")\
                        .setDescription(propertyType.description)
        widgets.append(widget)
    return widgets;

def isValid(dataType, value):
    return True
##
## Main Methods
##
def configureUI():
    # Add Headers
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader("identifier")
    tableBuilder.addHeader("sampleType")
    usedTableHeaders = {"identifier" : True, "sampleType" : True }
    for sampleTypeCode in getAllAnnotableSampleTypesForType(annotableType):
        for propertyTypeCode in getPropertyTypesForSampleTypeFromAnnotableType(sampleTypeCode, annotableType):
            if propertyTypeCode not in usedTableHeaders:
                tableBuilder.addHeader(propertyTypeCode)
                usedTableHeaders[propertyTypeCode] = True

    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    # Add Rows
    samples = list(propertyConverter().convertToElements(property))
    for sample in samples:
        row = tableBuilder.addRow()
        for annotation in sample.getAttributes():
            if annotation != "permId":
                row.setCell(annotation, sample.getAttribute(annotation))
            
    # Add Create buttons
    for sampleTypeCode in getAllAnnotableSampleTypesForType(annotableType):
        title = "Add " + sampleTypeCode;
        addAction = uiDescription.addTableAction(title).setDescription(title)
        widgets = getWidgetForAdd(sampleTypeCode, annotableType)
        addAction.addInputWidgets(widgets)
    
    # Add Delete button
    deleteAction = uiDescription.addTableAction("Delete")\
                                .setDescription('Are you sure you want to delete selected annotation?')
    deleteAction.setRowSelectionRequired() # Delete is enabled when at least 1 row is selected.

def updateFromUI(action):
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
    
    if action.name.startswith("Add"):
        sampleTypeCode = action.name[4:]
        identifier = action.getInputValue("identifier")
        annotations = { }
        for propertyTypeCode in getPropertyTypesForSampleTypeFromAnnotableType(sampleTypeCode, annotableType):
            propertyType = getPropertyType(propertyTypeCode)
            propertyTypeValue = action.getInputValue(propertyType.label)
            if not isValid(propertyType.dataType, propertyTypeValue):
                raise ValidationException("Property " + str(propertyType.label) + " with invalid value " + str(propertyTypeValue))
            if propertyTypeValue is None:
                propertyTypeValue = ""
            annotations[propertyTypeCode] = propertyTypeValue
        newAnnotation = createAnnotationsFor(identifier, annotations, sampleTypeCode)
        elements.append(newAnnotation)
    
    if action.name.startswith("Delete"):
        rowIds = list(action.getSelectedRows())
        rowIds.reverse()
        for rowId in rowIds:
            elements.pop(rowId) 
    
    property.value = converter.convertToString(elements)

##
## Batch Import Methods
##
def batchColumnNames():
    allTypes = []
    for sampleTypeCode in getAllAnnotableSampleTypesForType(annotableType):
        allTypes.append(sampleTypeCode)
    return allTypes

def updateFromBatchInput(bindings):
    print "--------> UPDATE FROM BATCH INPUT: "
    elements = []
    
    #Links Configuration
    typesToCopyFrom = []
    if annotableType in configurationCopyParents:
        typesToCopyFrom = configurationCopyParents[annotableType]
                
    for annotableSampleType in getAllAnnotableSampleTypesForType(annotableType):
        annotatedSamples = bindings.get(annotableSampleType)
        print "-----> TYPE: " + str(annotableSampleType) + " BINDINGS: " + str(annotatedSamples)
        if (annotatedSamples != None and annotatedSamples != ""):
            #Annotations
            print "-----> Annotations"
            if annotableSampleType not in typesToCopyFrom:
                for sampleLine in annotatedSamples.split("\\"):
                    propertyTypes = getPropertyTypesForSampleTypeFromAnnotableType(annotableSampleType, annotableType)
                    identifier = None
                    permId = None
                    sampleLink = None
                    for sampleProperty in sampleLine.split(";"):
                        propertyName = sampleProperty.split(":")[0]
                        if propertyName == "identifier":
                            propertyValue = sampleProperty.split(":")[1]
                            identifier = propertyValue
                            permId = entityInformationProvider().getSamplePermId(propertyValue)
                            sampleLink = elementFactory().createSampleLink(permId)
                            sampleLink.addAttribute(propertyName, propertyValue)
                        elif propertyName in propertyTypes:
                            sampleLink.addAttribute(propertyName, propertyValue)
                        else:
                            raise NameError('Found invalid property: ' + propertyName + " on type " + annotableSampleType)
                    if identifier is not None:
                        elements.append(sampleLink)
            #Links
            print "-----> links"
            for typeToCopyFrom in typesToCopyFrom:
                typeToCopy = typesToCopyFrom[typeToCopyFrom]
                print "-----> TYPE TO COPY FROM: " + str(typeToCopyFrom)
                print "-----> TYPE TO COPY: " + str(typeToCopy)
                identifiersToCopyFrom = bindings.get(typeToCopyFrom)
                if identifiersToCopyFrom is not None:
                    print "-----> IDENTIFIERS TO COPY FROM: " + str(identifiersToCopyFrom)
                    identifiersForCopy = identifiersToCopyFrom.split(',')
                    for identifierToCopyFrom in identifiersForCopy:
                        print "-----> IDENTIFIER TO COPY FROM: " + str(identifierToCopyFrom)
                        permIdFromIdentifier = entityInformationProvider().getSamplePermId(identifierToCopyFrom)
                        print "-----> PERMID TO COPY FROM: " + str(permIdFromIdentifier)
                        parentsToCopyFromPermId = entityInformationProvider().getSamplePropertyValue(permIdFromIdentifier, "ANNOTATIONS_STATE")
                        print "-----> ELEMENTS TO COPY: " + str(parentsToCopyFromPermId)
                        parentElements = list(propertyConverter().convertStringToElements(parentsToCopyFromPermId))
                        for parentAnnotation in parentElements:
                            if parentAnnotation.getAttribute("sampleType") == typeToCopy:
                                parentAnnotation.addAttribute("CONTAINED", identifierToCopyFrom)
                                elements.append(parentAnnotation)
    property.value = propertyConverter().convertToString(elements)