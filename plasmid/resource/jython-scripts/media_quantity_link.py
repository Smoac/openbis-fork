""" 
Managed Property Script for handling MEDIA.


"""

import re

""""space that all parents come from (fixed)"""
SPACE = "YEAST_LAB"

"""code attribute name"""
ATR_CODE = "code"
ATR_QUANTITY = "quantity"
ATR_NAME = "name"

"""labels of table columns"""
LINK_LABEL = "link"
CODE_LABEL = "code"
QUANTITY_LABEL = "quantity"
NAME_LABEL = "name"

"""action labels"""

ADD_ACTION_LABEL = "Add"
EDIT_ACTION_LABEL = "Edit"
DELETE_ACTION_LABEL = "Delete"

"""helper functions"""

def _createSampleLink(medias_list, media_quantity_list):
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
    sampleLink.addAttribute(ATR_QUANTITY, media_quantity_list)
    return sampleLink    


"""
Example input:

FRM1:2nM, FRM2, FRM3:3 nM
"""


def showRawValueInForms():
    return False
 
def batchColumnNames():
    return [CODE_LABEL, QUANTITY_LABEL]
 
def updateFromRegistrationForm(bindings):
    elements = []
    for item in bindings:
        medias_list = item.get('CODE')
        media_quantity_list = item.get('QUANTITY')
    if medias_list:
          sampleLink = _createSampleLink(medias_list, media_quantity_list)
          elements.append(sampleLink)
            
    property.value = propertyConverter().convertToString(elements)



def configureUI():
    """Create table builder and add columns."""
    tableBuilder = createTableBuilder()
    tableBuilder.addHeader(LINK_LABEL)
    tableBuilder.addHeader(CODE_LABEL)
    tableBuilder.addHeader(QUANTITY_LABEL)
    tableBuilder.addHeader(NAME_LABEL)

    """The property value should contain XML with list of samples. Add a new row for every sample."""
    elements = list(propertyConverter().convertToElements(property))
    for media in elements:
        medias_list = media.getAttribute(ATR_CODE, "")
        name = media.getAttribute(ATR_NAME,"")
        media_quantity_list=media.getAttribute(ATR_QUANTITY, "")
        
        row = tableBuilder.addRow()
        row.setCell(LINK_LABEL, media, medias_list)
        row.setCell(CODE_LABEL, medias_list)
        row.setCell(NAME_LABEL, name)
        row.setCell(QUANTITY_LABEL, media_quantity_list)
        
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
        inputWidgetFactory().createTextInputField(QUANTITY_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Quantity')
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
        inputWidgetFactory().createTextInputField(QUANTITY_LABEL)\
                            .setMandatory(True)\
                            .setDescription('Quantity of the media sample, e.g. "1M"'),
    ]
    editAction.addInputWidgets(widgets)  
  # Bind field name with column name.
    editAction.addBinding(CODE_LABEL, CODE_LABEL)
    editAction.addBinding(QUANTITY_LABEL, QUANTITY_LABEL) 


  
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
        media_quantity_list = action.getInputValue(QUANTITY_LABEL)
        sampleLink = _createSampleLink(medias_list, media_quantity_list)
        
        elements.append(sampleLink)
    elif action.name == EDIT_ACTION_LABEL:
        """
           For 'edit' action find the media element corresponding to selected row
           and replace it with an element with values from input fields.
        """
        medias_list = action.getInputValue(CODE_LABEL)
        media_quantity_list = action.getInputValue(QUANTITY_LABEL)

        

        sampleLink = _createSampleLink(medias_list, media_quantity_list)
        
        

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
            (code, quantity) = _extractCodeAndQuantity(media)
            sampleLink = _createSampleLink(code, quantity)
            elements.append(sampleLink)
            
    property.value = propertyConverter().convertToString(elements)
    
def _extractCodeAndQuantity(media):
    codeAndQuantity = media.split(':')
    if (len(codeAndQuantity) == 2):
        return (codeAndQuantity[0].strip(), codeAndQuantity[1].strip())
    else:
        return (codeAndQuantity[0].strip(), "n.a.")