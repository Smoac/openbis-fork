from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider

# Configuration
# This needs to be edited for each sample type to match the Profile.js ANNOTATION_PROPERTIES map.
configuration = {}
configuration["PROTEIN"] = { "PHOSPHO" : True, "ISOTYPE" : False }

#Global Variables
server = CommonServiceProvider.getCommonServer()
contextOrNull = server.tryToAuthenticateAsSystem()
propertyTypes = server.listPropertyTypes(contextOrNull.getSessionToken(), False)
    
def getPropertyType(propertyTypeCode):
    print "Searching property type: " + propertyTypeCode
    for propertyType in propertyTypes:
        if propertyType.code == propertyTypeCode:
            return propertyType
    return None;

def createAnnotationsFor(permId, annotations):
    newAnnotation = elementFactory().createSampleLink(permId)
    for annotation in annotations:
        newAnnotation.addAttribute(annotation, annotations[annotation])
    return newAnnotation

def getWidgetForAdd(sampleTypeCode):
    widgets = []
    widget = inputWidgetFactory().createTextInputField("permId")\
                            .setMandatory(True)\
                            .setValue("")\
                            .setDescription("")
    widgets.append(widget)
    for propertyTypeCode in configuration[sampleTypeCode].keys():
        propertyType = getPropertyType(propertyTypeCode)
        widget = inputWidgetFactory().createTextInputField(propertyType.label)\
                        .setMandatory(configuration[sampleTypeCode][propertyTypeCode])\
                        .setValue("")\
                        .setDescription(propertyType.description)
        widgets.append(widget)
    return widgets;

def configureUI():
    print "All property types: " + str(propertyTypes)
    # Add Headers
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader("permId")
    for sampleTypeCode in configuration.keys():
        for propertyTypeCode in configuration[sampleTypeCode].keys():
            tableBuilder.addHeader(propertyTypeCode)

    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    # Add Rows
    samples = list(propertyConverter().convertToElements(property))
    for sample in samples:
        row = tableBuilder.addRow()
        for annotation in sample.getAttributes():
            row.setCell(annotation, sample.getAttribute(annotation))
            
    # Add Create buttons
    for sampleTypeCode in configuration.keys():
        title = "Add " + sampleTypeCode;
        addAction = uiDescription.addTableAction(title).setDescription(title)
        widgets = getWidgetForAdd(sampleTypeCode)
        addAction.addInputWidgets(widgets)
    
    # Add Delete button
    deleteAction = uiDescription.addTableAction("Delete")\
                                .setDescription('Are you sure you want to delete selected annotation?')
    deleteAction.setRowSelectionRequired() # Delete is enabled when at least 1 row is selected.

def isValid(dataType, value):
    print "isValid"
    return True

def updateFromUI(action):
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
    
    if action.name.startswith("Add"):
        sampleTypeCode = action.name[4:]
        permId = action.getInputValue("permId")
        annotations = {}
        for propertyTypeCode in configuration[sampleTypeCode]:
            propertyType = getPropertyType(propertyTypeCode)
            propertyTypeValue = action.getInputValue(propertyType.label)
            if not isValid(propertyType.dataType, propertyTypeValue):
                raise ValidationException("Property " + str(propertyType.label) + " with invalid value " + str(propertyTypeValue))
            annotations[propertyTypeCode] = propertyTypeValue
        newAnnotation = createAnnotationsFor(permId, annotations)
        elements.append(newAnnotation)
    
    if action.name.startswith("Delete"):
        rowIds = list(action.getSelectedRows())
        rowIds.reverse()
        for rowId in rowIds:
            elements.pop(rowId) 
    
    property.value = converter.convertToString(elements)