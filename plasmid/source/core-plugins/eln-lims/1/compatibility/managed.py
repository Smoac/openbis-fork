from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider

##
## Configuration
##

# This needs to be edited for each sample type to match the Profile.js ANNOTATION_PROPERTIES map.
configuration = {}
configuration["MEDIA"] = {
                          "CHEMICAL" : {"QUANTITY" : False, "COMMENTS" : False },
                          "SOLUTION_BUFFER" : {"QUANTITY" : False, "COMMENTS" : False },
                          "MEDIA" : {"QUANTITY" : False, "COMMENTS" : False }
                         };

configuration["SOLUTION_BUFFER"] = {
                          "CHEMICAL" : {"QUANTITY" : False, "COMMENTS" : False },
                          "SOLUTION_BUFFER" : {"QUANTITY" : False, "COMMENTS" : False },
                          "MEDIA" : {"QUANTITY" : False, "COMMENTS" : False }
                         };

configuration["GENERAL_PROTOCOL"] = {
                          "CHEMICAL" : {"QUANTITY" : False, "COMMENTS" : False },
                          "SOLUTION_BUFFER" : {"QUANTITY" : False, "COMMENTS" : False },
                          "MEDIA" : {"QUANTITY" : False, "COMMENTS" : False },
                          "ENZYME" : {"QUANTITY" : False, "COMMENTS" : False },
                          "GENERAL_PROTOCOL" : {"COMMENTS" : False }
                         };

configuration["PCR_PROTOCOL"] = {
                          "CHEMICAL" : {"QUANTITY" : False, "COMMENTS" : False },
                          "SOLUTION_BUFFER" : {"QUANTITY" : False, "COMMENTS" : False },
                          "ENZYME" : {"QUANTITY" : False, "COMMENTS" : False }
                         };

configuration["WESTERN_BLOTTING_PROTOCOL"] = {
                          "CHEMICAL" : {"QUANTITY" : False, "COMMENTS" : False },
                          "SOLUTION_BUFFER" : {"QUANTITY" : False, "COMMENTS" : False },
                          "ANTIBODY" : {"QUANTITY" : False, "COMMENTS" : False }
                         };

configuration["PLASMID"] = {
                          "PLASMID" : {"COMMENTS" : False }
                         };

configuration["BACTERIA"] = {
                          "BACTERIA" : {"COMMENTS" : False },
                          "PLASMID" : {"PLASMID_RELATIONSHIP" : False, "PLASMID_ANNOTATION" : False, "COMMENTS" : False }
                         };

configuration["YEAST"] = {
                          "YEAST" : {"COMMENTS" : False },
                          "PLASMID" : {"PLASMID_RELATIONSHIP" : False, "PLASMID_ANNOTATION" : False, "COMMENTS" : False, "CONTAINED" : False }
                         };

configuration["CELL_LINE"] = {
                          "CELL_LINE" : {"COMMENTS" : False },
                          "PLASMID" : {"PLASMID_RELATIONSHIP" : False, "PLASMID_ANNOTATION" : False, "COMMENTS" : False },
                          "FLY" : {"COMMENTS" : False }
                         };

configuration["FLY"] = {
                          "FLY" : {"COMMENTS" : False },
                          "PLASMID" : {"PLASMID_RELATIONSHIP" : False, "PLASMID_ANNOTATION" : False, "COMMENTS" : False }
                         };

configuration["EXPERIMENTAL_STEP"] = {
                          "ANTIBODY" : {"QUANTITY" : False, "COMMENTS" : False },
                          "BACTERIA" : {"COMMENTS" : False },
                          "CELL_LINE" : {"COMMENTS" : False },
                          "CHEMICAL" : {"QUANTITY" : False, "COMMENTS" : False },
                          "ENZYME" : {"QUANTITY" : False, "COMMENTS" : False },
                          "FLY" : {"COMMENTS" : False },
                          "MEDIA" : {"QUANTITY" : False, "COMMENTS" : False },
                          "OLIGO" : {"COMMENTS" : False },
                          "PLASMID" : {"COMMENTS" : False },
                          "RNA" : {"COMMENTS" : False },
                          "SOLUTION_BUFFER" : {"QUANTITY" : False, "COMMENTS" : False },
                          "YEAST" : {"COMMENTS" : False },
                          "GENERAL_PROTOCOL" : {"COMMENTS" : False },
                          "PCR_PROTOCOL" : {"COMMENTS" : False },
                          "WESTERN_BLOTTING_PROTOCOL" : {"COMMENTS" : False }
                         };

configurationCopyParents = {}
configurationCopyParents["YEAST"] = { "YEAST" : "PLASMID" };

#Global Variables
annotableType = "<REPLACE_WITH_ANNOTABLE_TYPE>"
propertyTypes = None
def getPropertyTypes():
    global propertyTypes
    if propertyTypes is None:
        server = CommonServiceProvider.getCommonServer()
        contextOrNull = server.tryToAuthenticateAsSystem()
        propertyTypes = server.listPropertyTypes(contextOrNull.getSessionToken(), False)
    return propertyTypes;

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
    for propertyType in getPropertyTypes():
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
## Registration form (EXPERIMENTAL)
##

# def inputWidgets():
#     print "EXECUTING inputWidgets START"
#     widgetsToCopyFrom = []
#     if annotableType in configurationCopyParents:
#         toCopyFrom = configurationCopyParents[annotableType]
#         if(toCopyFrom is not None):
#             for key in toCopyFrom:
#                 widgetLabel = key + " to copy " + toCopyFrom[key]
#                 print "EXECUTING inputWidgets key: " + widgetLabel
#                 widgetToCopyFrom = inputWidgetFactory().createTextInputField(widgetLabel).setMandatory(False)
#                 widgetsToCopyFrom.append(widgetToCopyFrom)
#     print "EXECUTING inputWidgets " + str(len(widgetsToCopyFrom))
#     return widgetsToCopyFrom
# 
# def updateFromRegistrationForm(bindings):
#     print "EXECUTING updateFromaRegistrationForm"
#     for key in bindings:
#         print "MANAGED WITH KEY: " + str(key)

##
## Main Methods
##
def configureUI():
    # Add Headers
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader("identifier")
    tableBuilder.addHeader("sampleType")
    tableBuilder.addHeader("name")
    
    usedTableHeaders = {"identifier" : True, "sampleType" : True, "name" : True }
    
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
        permId = sample.getAttribute("permId")
        
        nameValue = entityInformationProvider().getSamplePropertyValue(permId, "NAME")
        if nameValue is not None:
            row.setCell("name", nameValue)
        
        for annotation in sample.getAttributes():
            if annotation != "permId":
                if annotation == "identifier":
                    row.setCell(annotation, sample, sample.getAttribute(annotation))
                else:
                    row.setCell(annotation, sample.getAttribute(annotation))
            
    # Add Create buttons
    for sampleTypeCode in getAllAnnotableSampleTypesForType(annotableType):
        title = "Add " + sampleTypeCode;
        addAction = uiDescription.addTableAction(title).setDescription(title)
        widgets = getWidgetForAdd(sampleTypeCode, annotableType)
        addAction.addInputWidgets(widgets)

    # TO-DO Edit button for each type with different hooks to the columns depending on the type
    # Add Edit Button (EXPERIMENTAL)
#     editAction = uiDescription.addTableAction('Edit').setDescription('Edit selected table row')
#     editAction.setRowSelectionRequiredSingle()
#     editWidgets = []
#     for headerKey in usedTableHeaders:
#         editWidgets.append(inputWidgetFactory().createTextInputField(headerKey))
#         editAction.addBinding(headerKey, headerKey)
#     editAction.addInputWidgets(editWidgets)

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
    #print "--------> UPDATE FROM BATCH INPUT: "
    elements = []
    
    #Links Configuration
    typesToCopyFrom = []
    if annotableType in configurationCopyParents:
        typesToCopyFrom = configurationCopyParents[annotableType]
                
    for annotableSampleType in getAllAnnotableSampleTypesForType(annotableType):
        annotatedSamples = bindings.get(annotableSampleType)
        #print "-----> TYPE: " + str(annotableSampleType) + " BINDINGS: " + str(annotatedSamples)
        if (annotatedSamples != None and annotatedSamples != ""):
            #Annotations
            #print "-----> Annotations"
            if annotableSampleType not in typesToCopyFrom:
                for sampleLine in annotatedSamples.split("\\"):
                    propertyTypes = getPropertyTypesForSampleTypeFromAnnotableType(annotableSampleType, annotableType)
                    identifier = None
                    permId = None
                    sampleLink = None
                    for sampleProperty in sampleLine.split(";"):
                        propertyName = sampleProperty.split(":")[0]
                        propertyValue = sampleProperty.split(":")[1]
                        if propertyName == "identifier":
                            identifier = propertyValue
                            permId = entityInformationProvider().getSamplePermId(propertyValue)
                            sampleLink = elementFactory().createSampleLink(permId)
                            sampleLink.addAttribute(propertyName, propertyValue)
                            sampleLink.addAttribute('sampleType', annotableSampleType)
                        elif propertyName in propertyTypes:
                            sampleLink.addAttribute(propertyName, propertyValue)
                        else:
                            raise NameError('Found invalid property: ' + propertyName + " on type " + annotableSampleType)
                    if identifier is not None:
                        elements.append(sampleLink)
    #Links
    #print "-----> links"
    for typeToCopyFrom in typesToCopyFrom:
        typeToCopy = typesToCopyFrom[typeToCopyFrom]
        #print "-----> TYPE TO COPY FROM: " + str(typeToCopyFrom)
        #print "-----> TYPE TO COPY: " + str(typeToCopy)
        identifiersToCopyFrom = bindings.get(typeToCopyFrom)
        if identifiersToCopyFrom is not None:
            #print "-----> IDENTIFIERS TO COPY FROM: " + str(identifiersToCopyFrom)
            identifiersForCopy = identifiersToCopyFrom.split(',')
            for identifierToCopyFrom in identifiersForCopy:
                #print "-----> IDENTIFIER TO COPY FROM: " + str(identifierToCopyFrom)
                permIdFromIdentifier = entityInformationProvider().getSamplePermId(identifierToCopyFrom)
                #print "-----> PERMID TO COPY FROM: " + str(permIdFromIdentifier)
                parentsToCopyFromPermId = entityInformationProvider().getSamplePropertyValue(permIdFromIdentifier, "ANNOTATIONS_STATE")
                #print "-----> ELEMENTS TO COPY: " + str(parentsToCopyFromPermId)
                parentElements = list(propertyConverter().convertStringToElements(parentsToCopyFromPermId))
                for parentAnnotation in parentElements:
                    if parentAnnotation.getAttribute("sampleType") == typeToCopy:
                        parentAnnotation.addAttribute("CONTAINED", identifierToCopyFrom)
                        #print "-----> COPYING: " + str(parentAnnotation.getAttribute('identifier'))
                        elements.append(parentAnnotation)
    property.value = propertyConverter().convertToString(elements)