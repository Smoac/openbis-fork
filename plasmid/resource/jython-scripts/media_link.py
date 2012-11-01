""" 
Managed Property Script for handling MEDIA.


"""

import re

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

"""code attribute name"""
ATR_CODE = "code"
ATR_CONC = "concentration"
ATR_NAME = "name"

"""labels of table columns"""
LINK_LABEL = "link"
CODE_LABEL = "code"
CONC_LABEL = "concentration"
NAME_LABEL = "name"

"""action labels"""

ADD_ACTION_LABEL = "Add"
EDIT_ACTION_LABEL = "Edit"
DELETE_ACTION_LABEL = "Delete"

"""helper functions"""

def _createSampleLink(medias_list, media_concentration_list):
    """
       Creates sample link XML element for sample with specified 'code'. The element will contain
       given code as 'code' attribute apart from standard 'permId' attribute.
       
       If the sample doesn't exist in DB a fake link will be created with the 'code' as permId.
       
       @return: sample link XML element as string, e.g.:
       - '<Sample code="FRP1" permId="20110309154532868-4219"/>'
       - '<Sample code="FAKE_SAMPLE_CODE" permId="FAKE_SAMPLE_CODE"/>
    """
    mediaPath= "/YEAST_LAB/" + medias_list
    permId =entityInformationProvider().getSamplePermId(SPACE, medias_list)
    if not permId:
        permId = medias_list
    name  = entityInformationProvider().getSamplePropertyValue(permId, 'NAME')       
    sampleLink = elementFactory().createSampleLink(permId)
   
    sampleLink.addAttribute(ATR_CODE, medias_list)
    sampleLink.addAttribute(ATR_NAME, name) 
    sampleLink.addAttribute(ATR_CONC, media_concentration_list)
    return sampleLink    


"""
Example input:

FRC1, FRC2, FRC3, FRC4
"""


def updateBufferFromBatchInput(medias_list, media_concentration_list):
    elements = []
    input = medias_list
    input2 = media_concentration_list
    if input is not None:
       for i, j in zip(medias_list,media_concentration_list): #zip is used to iterate over two lists in parallel
            sampleLink = _createSampleLink(i.strip(), j.strip())
            elements.append(sampleLink)
    return propertyConverter.convertToString(elements)




def configureUI():
    """Create table builder and add columns."""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL)
    tableBuilder.addHeader(CONC_LABEL)
    tableBuilder.addHeader(NAME_LABEL)

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for media in elements:
        medias_list = media.getAttribute(ATR_CODE, "")
        name = media.getAttribute(ATR_NAME,"")
        media_concentration_list=media.getAttribute(ATR_CONC, "")
        
        row = tableBuilder.addRow()
        row.setCell(LINK_LABEL, media, medias_list)
        row.setCell(CODE_LABEL, medias_list)
        row.setCell(NAME_LABEL, name)
        row.setCell(CONC_LABEL, media_concentration_list)
        
    """Specify that the property should be shown in a tab and set the table output."""
    property.setOwnTab(True)
    uiDescription = property.getUiDescription()
    uiDescription.useTableOutput(tableBuilder.getTableModel())
    
    """
       Define and add actions with input fields used to:
       1. specify attributes of new media,
    """
    addAction = uiDescription.addTableAction(ADD_ACTION_LABEL)\
                             .setDescription('Add new media relationship:')
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setValue('FRC')\
                            .setDescription('Code of media, e.g. "FRC1"'),
        inputWidgetFactory().createTextInputField(CONC_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Concentration')
    ]
    addAction.addInputWidgets(widgets)
      
    """
       2. modify attributes of a selected yeast parent,
    """
    editAction = uiDescription.addTableAction(EDIT_ACTION_LABEL)\
                              .setDescription('Edit selected media relationship:')
    # Exactly 1 row needs to be selected to enable action.
    editAction.setRowSelectionRequiredSingle()            
    widgets = [
        inputWidgetFactory().createTextInputField(CODE_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Code of media sample, e.g. "FRC1"'),
        inputWidgetFactory().createTextInputField(CONC_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Concentration of the media sample, e.g. "1M"'),
    ]
    editAction.addInputWidgets(widgets)  
  # Bind field name with column name.
    editAction.addBinding(CODE_LABEL, CODE_LABEL)
    editAction.addBinding(CONC_LABEL, CONC_LABEL) 


  
    """
       3. delete selected yeast parents.
    """
    deleteAction = uiDescription.addTableAction(DELETE_ACTION_LABEL)\
                                .setDescription('Are you sure you want to delete the selected media relationships?')
    # Delete is enabled when at least 1 row is selected.
    deleteAction.setRowSelectionRequired()
    
    
def updateFromUI(action):
    """Extract list of elements from old value of the property."""
    converter = propertyConverter()
    elements = list(converter.convertToElements(property))
  
    """Implement behaviour of user actions."""
    if action.name == ADD_ACTION_LABEL:
        """
           For 'add' action create new media element with values from input fields
           and add it to existing elements.
        """
        medias_list = action.getInputValue(CODE_LABEL)
        media_concentration_list = action.getInputValue(CONC_LABEL)
        sampleLink = _createSampleLink(medias_list, media_concentration_list)
        
        elements.append(sampleLink)
    elif action.name == EDIT_ACTION_LABEL:
        """
           For 'edit' action find the media element corresponding to selected row
           and replace it with an element with values from input fields.
        """
        medias_list = action.getInputValue(CODE_LABEL)
        media_concentration_list = action.getInputValue(CONC_LABEL)

        

        sampleLink = _createSampleLink(medias_list, media_concentration_list)
        
        

        selectedRowId = action.getSelectedRows()[0]
        elements[selectedRowId] = sampleLink
    elif action.name == DELETE_ACTION_LABEL:
        """
           For 'delete' action delete medias that correspond to selected rows.
           NOTE: As many rows can be deleted at once it is easier to delete them in reversed order.
        """
        rowIds = list(action.getSelectedRows())
        rowIds.reverse()       
        for rowId in rowIds:
            elements.pop(rowId)      
    else:
        raise ValidationException('action not supported')
      
    """Update value of the managed property to XML string created from modified list of elements."""
    property.value = converter.convertToString(elements)
    
def updateFromBatchInput(bindings):
    elements = []
    input = bindings.get('')
    if input is not None:
        medias = input.split(',')
        for media in medias:
            (code, concentration) = _extractCodeAndConcentration(media)
            sampleLink = _createSampleLink(code, concentration)
            elements.append(sampleLink)
            
    property.value = propertyConverter().convertToString(elements)
    
def _extractCodeAndConcentration(media):
    codeAndConcentration = media.split(':')
    if (len(codeAndConcentration) == 2):
        return (codeAndConcentration[0].strip(), codeAndConcentration[1].strip())
    else:
        return (codeAndConcentration[0].strip(), "n.a.")